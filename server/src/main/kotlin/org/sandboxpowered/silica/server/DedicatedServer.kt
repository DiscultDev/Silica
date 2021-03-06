package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
import com.google.inject.Guice
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import mu.toKLogger
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.internal.AddonSpec
import org.sandboxpowered.silica.StateManager
import org.sandboxpowered.silica.inject.SilicaImplementationModule
import org.sandboxpowered.silica.loading.SandboxLoader
import org.sandboxpowered.silica.resources.DirectoryResourceLoader
import org.sandboxpowered.silica.resources.ResourceLoader
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import org.sandboxpowered.silica.util.join
import org.sandboxpowered.silica.util.messageAdapter
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.util.onSignal
import org.sandboxpowered.silica.world.SilicaWorld
import java.io.File
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.Duration

class DedicatedServer : SilicaServer() {
    private var log: Logger = LogManager.getLogger()
    private var loader: SandboxLoader? = null
    private val stateManager = StateManager()
    private val acceptVanillaConnections: Boolean
    private lateinit var world: ActorRef<SilicaWorld.Command>
    private lateinit var network: ActorRef<Network>
    private val stateManagerErrors: Map<StateManager.ErrorType, Set<String>>

    init {
        Guice.createInjector(SilicaImplementationModule())
        properties = ServerProperties.fromFile(Paths.get("server.properties"))
        loader = SandboxLoader()
        loader!!.load()
        stateManagerErrors = stateManager.load()
        acceptVanillaConnections = stateManagerErrors.isEmpty()
    }

    private fun createAddonPack(spec: AddonSpec, file: File): ResourceLoader {
        return if (file.isDirectory) DirectoryResourceLoader(file) else ZIPResourceLoader(file)
    }

    fun run() {
        if (acceptVanillaConnections) {
            log.info("Accepting vanilla connections")
        } else {
            val unknown = stateManagerErrors[StateManager.ErrorType.UNKNOWN]
            if (unknown != null && unknown.isNotEmpty()) {
                log.info("Found ${unknown.size} custom BlockStates")
                unknown.forEach {
//                    log.info("   $it")
                }
            }
            val missing = stateManagerErrors[StateManager.ErrorType.MISSING]
            if (missing != null && missing.isNotEmpty()) {
                log.info("Missing ${missing.size} vanilla BlockStates. Exported to missing.txt")
                val builder = StringBuilder()
                missing.forEach {
                    builder.append(it).append("\n")
                }
                FileUtils.writeStringToFile(File("missing.txt"), builder.toString(), StandardCharsets.UTF_8)
            }
            log.info("Rejecting vanilla connections")
        }
        loader!!.allAddons.keys.forEach {
            dataManager.add(createAddonPack(it, File(it.path.toURI())))
        }
        log.debug("Loaded namespaces: [${dataManager.getNamespaces().join(",")}]")
        val system = ActorSystem.create(DedicatedServerGuardian.create(this, this::world::set, this::network::set), "dedicatedServerGuardian")
//        system.terminate()
    }

    override fun getWorld() = this.world

    override fun getNetwork() = this.network

    sealed class Command {
        class Tick(val delta: Float) : Command()
        class Tock(val done: ActorRef<*>) : Command()
    }

    private class DedicatedServerGuardian private constructor(
        val server: SilicaServer, // don't like this
        context: ActorContext<Command>,
        timerScheduler: TimerScheduler<Command>,
        worldInit: (ActorRef<SilicaWorld.Command>) -> Unit,
        networkInit: (ActorRef<Network>) -> Unit
    ) : AbstractBehavior<Command>(context) {
        companion object {
            fun create(server: SilicaServer, worldInit: (ActorRef<SilicaWorld.Command>) -> Unit, networkInit: (ActorRef<Network>) -> Unit): Behavior<Command> = Behaviors.withTimers { timerScheduler ->
                Behaviors.setup {
                    DedicatedServerGuardian(server, it, timerScheduler, worldInit, networkInit)
                }
            }
        }

        private val logger = context.log.toKLogger()
        private var skippedTicks = 0
        private var lastTickTime: Long = -1
        private val world: ActorRef<in SilicaWorld.Command> = context.spawn(SilicaWorld.actor(Side.SERVER), "world").apply(worldInit)
        private val network: ActorRef<in Network> = context.spawn(Network.actor(server), "network").apply(networkInit)
        private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

        init {
            context.watch(world)
            context.watch(network)
            // TODO: compare to startTimerAtFixedRate
            timerScheduler.startTimerWithFixedDelay("serverTick", Command.Tick(50f), Duration.ofMillis(50))

            // TODO: wait for everything to be ready
            network.tell(Network.Start(context.system.ignoreRef()))
        }

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleTock)
            .onSignal(this::terminated)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<Command> {
            if (currentlyTicking.isNotEmpty()) {
                val lastTickOffset = System.currentTimeMillis() - lastTickTime
                if (server.properties.maxTickTime != -1 && lastTickOffset >= server.properties.maxTickTime) {
                    TODO("Terminate server after taking too long")
                }
                ++skippedTicks
            } else {
                if (skippedTicks > 0) {
                    val lastTickOffset = System.currentTimeMillis() - lastTickTime
                    logger.warn("Skipped $skippedTicks ticks! took {}ms", lastTickOffset)
                    skippedTicks = 0
                }

                @Suppress("ReplacePutWithAssignment") // boxing
                currentlyTicking.put(world, System.nanoTime())
                currentlyTicking.put(network, System.nanoTime())
                lastTickTime = System.currentTimeMillis()
                world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
                network.tell(Network.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
            }

            return Behaviors.same()
        }

        private fun handleTock(tock: Command.Tock): Behavior<Command> {
            val startTime = currentlyTicking.removeLong(tock.done)
            if (startTime == 0L) logger.warn { "Received tock for actor which shouldn't be ticking : ${tock.done}" }
            // TODO: profiling ?

            return Behaviors.same()
        }

        private fun terminated(terminated: Terminated): Behavior<Command> {
            logger.warn { "${terminated.ref.path()} terminated" }
            return Behaviors.stopped()
        }
    }

    override fun getStateManager() = stateManager
}