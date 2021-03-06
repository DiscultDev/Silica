package org.sandboxpowered.silica.world.gen

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.api.block.Blocks
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube
import kotlin.system.measureTimeMillis

sealed class TerrainGenerator {
    data class Generate(val x: Int, val y: Int, val z: Int, val chunk: BlocTree, val replyTo: ActorRef<Generate>) :
        TerrainGenerator()

    companion object {
        fun actor(): Behavior<TerrainGenerator> = Behaviors.setup {
            TerrainGeneratorActor(SimpleFiller(), it)
        }
    }
}

private class TerrainGeneratorActor(private val filler: ChunkFiller, context: ActorContext<TerrainGenerator>?) :
    AbstractBehavior<TerrainGenerator>(context) {

    override fun createReceive(): Receive<TerrainGenerator> = newReceiveBuilder()
        .onMessage(this::handleGenerate)
        .build()

    private fun handleGenerate(generate: TerrainGenerator.Generate): Behavior<TerrainGenerator> {
        val (x, y, z, chunk, replyTo) = generate
        filler.fill(x, y, z, chunk)
        replyTo.tell(generate)

        return Behaviors.same()
    }
}

interface ChunkFiller {
    fun fill(sx: Int, sy: Int, sz: Int, chunk: BlocTree)
}

private class SimpleFiller : ChunkFiller {
    private val air by lazy { Blocks.AIR.get().baseState }
    private val bedrock by lazy { Blocks.BEDROCK.get().baseState }
    private val stone by lazy { Blocks.STONE.get().baseState }
    private val dirt by lazy { Blocks.DIRT.get().baseState }
    private val grass by lazy { Blocks.GRASS_BLOCK.get().baseState }

    override fun fill(sx: Int, sy: Int, sz: Int, chunk: BlocTree) {
        if (sy > 0) return
        val time = measureTimeMillis {
            iterateCube(sx, sy, sz, w = 16, h = 7) { x, y, z ->
                chunk[x, y, z] = when (y) {
                    0 -> bedrock
                    1, 2, 3 -> stone
                    4, 5 -> dirt
                    6 -> grass
                    else -> air
                }
            }
        }
        println("Generated $sx, $sy, $sz in $time millis")
    }
}
