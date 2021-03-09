package org.sandboxpowered.silica.network.play.serverbound;

import kotlin.jvm.functions.Function1;
import org.sandboxpowered.api.ecs.ComponentMapper;
import org.sandboxpowered.api.world.World;
import org.sandboxpowered.silica.component.PositionComponent;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;
import org.sandboxpowered.silica.world.SilicaWorld;

public class PlayerPosition implements PacketPlay {
    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public PlayerPosition() {
    }

    public PlayerPosition(double x, double y, double z, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    @Override
    public void read(PacketByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        onGround = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(onGround);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {
        connection.getServer().getWorld().tell(new SilicaWorld.Command.PerformSilica(
                silicaWorld -> {
                    Integer id = silicaWorld.getPlayerMap().get(packetHandler.connection.getProfile().getId());
                    if(id!=null) {
                        PositionComponent component = silicaWorld.getArtemisWorld().getEntity(id).getComponent(PositionComponent.class);
                        component.getPos().set(x,y,z);
                    }
                    return null;
                }
        ));
    }
}
