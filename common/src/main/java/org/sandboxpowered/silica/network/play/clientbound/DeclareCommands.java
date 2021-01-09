package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class DeclareCommands implements Packet {
    public DeclareCommands() {
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(1);

        buf.writeByte(0);
        buf.writeVarInt(0);

        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}