package org.sandboxpowered.silica

import com.artemis.*
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.utils.IntBag
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.sandboxpowered.api.util.text.Text
import org.sandboxpowered.silica.component.HitboxComponent
import org.sandboxpowered.silica.component.PlayerComponent
import org.sandboxpowered.silica.component.PositionComponent
import org.sandboxpowered.silica.component.VanillaPlayerInput
import java.net.SocketAddress
import java.util.*

@All(PlayerComponent::class, PositionComponent::class)
class SilicaPlayerManager(var maxPlayers: Int) : BaseEntitySystem() {
    private val uuidToEntityId: Object2IntFunction<UUID> = Object2IntOpenHashMap<UUID>()
        .apply { defaultReturnValue(UNKNOWN_ID) }
    private val entityToUuid: Int2ObjectFunction<UUID> = Int2ObjectOpenHashMap()
    val onlinePlayers: ObjectSet<UUID> = ObjectOpenHashSet()
    val onlinePlayerProfiles: Object2ObjectMap<UUID, GameProfile> = Object2ObjectOpenHashMap()
    private val entitiesToDelete = IntBag()

    @Wire
    private lateinit var playerMapper: ComponentMapper<PlayerComponent>

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var playerInputMapper: ComponentMapper<VanillaPlayerInput>

    fun checkDisconnectReason(address: SocketAddress, profile: GameProfile): Text? {
        if (profile.isLegacy)
            return Text.translatable("multiplayer.disconnect.not_whitelisted")
        return null
    }

    override fun processSystem() {

    }

    fun getPlayerId(uuid: UUID): Int {
        return uuidToEntityId.getInt(uuid)
    }

    fun getPlayerId(profile: GameProfile): Int {
        return getPlayerId(profile.id)
    }

    private lateinit var playerArchetype: Archetype

    override fun initialize() {
        super.initialize()

        val builder = ArchetypeBuilder()

        builder.add<PlayerComponent>()
        builder.add<PositionComponent>()
        builder.add<HitboxComponent>()
        builder.add<VanillaPlayerInput>()

        playerArchetype = builder.build(world, "player")
    }

    fun disconnect(profile: GameProfile) {
        onlinePlayers.remove(profile.id)
        onlinePlayerProfiles.remove(profile.id)

        entityToUuid.remove(uuidToEntityId.removeInt(profile.id))
    }

    fun create(profile: GameProfile): VanillaPlayerInput {
        val existing = uuidToEntityId.getInt(profile.id)
        if (existing != UNKNOWN_ID) return playerInputMapper[existing]

        val id = world.create(playerArchetype)
        val player = playerMapper.get(id)!!
        player.profile = profile

        onlinePlayers.add(profile.id)
        onlinePlayerProfiles[profile.id] = profile

        uuidToEntityId[profile.id] = id
        entityToUuid[id] = profile.id

        val playerPosition = positionMapper[id]
        playerPosition.pos.set(8.0, 8.0, 8.0)

        hitboxMapper[id].hitbox.set(0.6, 1.8, 0.6)

        val playerInput = playerInputMapper[id]
        playerInput.initialize(id, profile)
        playerInput.wantedPosition.set(playerPosition.pos)

        return playerInput
    }

    fun getVanillaInput(ent: Int): VanillaPlayerInput {
        return playerInputMapper.get(ent)
    }

    fun getOnlinePlayers(): Array<UUID> {
        return onlinePlayers.toTypedArray()
    }

    fun getOnlinePlayerProfiles(): Array<GameProfile> {
        return onlinePlayerProfiles.values.toTypedArray()
    }

    private companion object {
        private const val UNKNOWN_ID = -1
    }
}

private inline fun <reified T : Component> ArchetypeBuilder.add() {
    add(T::class.java)
}

private operator fun <V> Int2ObjectFunction<V>.set(key: Int, value: V) {
    put(key, value)
}

private operator fun <K> Object2IntFunction<K>.set(key: K, value: Int) {
    put(key, value)
}
