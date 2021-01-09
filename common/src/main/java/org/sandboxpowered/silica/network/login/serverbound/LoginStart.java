package org.sandboxpowered.silica.network.login.serverbound;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.command.CommandSource;
import org.sandboxpowered.silica.nbt.CompoundTag;
import org.sandboxpowered.silica.network.*;
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess;
import org.sandboxpowered.silica.network.play.clientbound.*;
import org.sandboxpowered.silica.server.SilicaServer;

import java.util.Collections;
import java.util.Map;

public class LoginStart implements Packet {
    private String username;

    @Override
    public void read(PacketByteBuf buf) {
        username = buf.readString(16);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {
        SilicaServer server = connection.getServer();
        connection.handleLoginStart(username);
//        packetHandler.sendPacket(new EncryptionRequest("", server.getKeyPair().getPublic().getEncoded(), server.getVerificationArray()));
        packetHandler.sendPacket(new LoginSuccess(connection.getProfile().getId(), username));
        packetHandler.setProtocol(Protocol.PLAY);

        Identity overworld = Identity.of("minecraft", "overworld");

        CompoundTag codec = new CompoundTag();
        CompoundTag dimReg = new CompoundTag();

        dimReg.setString("type", "minecraft:dimension_type");
        CompoundTag overworldTypeEntry = new CompoundTag();

        overworldTypeEntry.setString("name", overworld.toString());
        overworldTypeEntry.setInt("id", 0);
        CompoundTag overworldType = new CompoundTag();

        overworldType.setBoolean("piglin_safe", false);
        overworldType.setBoolean("natural", true);
        overworldType.setFloat("ambient_light", 1);
        overworldType.setString("infiniburn", "");
        overworldType.setBoolean("respawn_anchor_works", false);
        overworldType.setBoolean("has_skylight", true);
        overworldType.setBoolean("bed_works", true);
        overworldType.setString("effects", "minecraft:overworld");
        overworldType.setBoolean("has_raids", true);
        overworldType.setInt("logical_height", 256);
        overworldType.setFloat("coordinate_scale", 1);
        overworldType.setBoolean("ultrawarm", false);
        overworldType.setBoolean("has_ceiling", false);

        overworldTypeEntry.setTag("element", overworldType);

        dimReg.setList("value", Collections.singletonList(overworldTypeEntry));

        CompoundTag biomeReg = new CompoundTag();

        biomeReg.setString("type", "minecraft:worldgen/biome");

        CompoundTag plainsBiomeEntry = new CompoundTag();
        plainsBiomeEntry.setString("name", "minecraft:plains");
        plainsBiomeEntry.setInt("id", 0);
        CompoundTag plains = new CompoundTag();

        plains.setString("precipitation", "rain");
        plains.setFloat("depth", 0);
        plains.setFloat("temperature", 0);
        plains.setFloat("scale", 1);
        plains.setFloat("downfall", 1);
        plains.setString("category", "plains");
        CompoundTag effects = new CompoundTag();

        effects.setInt("sky_color", 8364543);
        effects.setInt("water_fog_color", 8364543);
        effects.setInt("fog_color", 8364543);
        effects.setInt("water_color", 8364543);


        plains.setTag("effects", effects);


        plainsBiomeEntry.setTag("element", plains);
        biomeReg.setList("value", Collections.singletonList(plainsBiomeEntry));

        codec.setTag("minecraft:dimension_type", dimReg);
        codec.setTag("minecraft:worldgen/biome", biomeReg);


        packetHandler.sendPacket(new JoinGame(
                0,
                false,
                (short) 1,
                (short) -1,
                1,
                new Identity[]{overworld},
                codec,
                overworldType,
                overworld,
                0,
                20,
                10,
                false,
                true,
                false,
                true
        ));

        packetHandler.sendPacket(new HeldItemChange((byte) 0));
        packetHandler.sendPacket(new DeclareRecipes());
        packetHandler.sendPacket(new DeclareTags());
        packetHandler.sendPacket(new EntityStatus());
        RootCommandNode<CommandSource> rootcommandnode = new RootCommandNode<>();
        packetHandler.sendPacket(new DeclareCommands(rootcommandnode));
        packetHandler.sendPacket(new UnlockRecipes());

    }
}