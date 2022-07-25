package net.migats21.sculkinfected.network;

import net.migats21.sculkinfected.client.LocalSculkTimer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

// Can't use CliendboundSoundPacket since it requires the sound to be present on both sides
public class ClientboundAmbientVibratePacket {

    private final int x;
    private final int y;
    private final int z;

    public ClientboundAmbientVibratePacket(Vec3 position) {
        this.x = (int) Math.round(position.x);
        this.y = (int) Math.round(position.y);
        this.z = (int) Math.round(position.z);
    }

    public ClientboundAmbientVibratePacket(FriendlyByteBuf buffer) {
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LocalSculkTimer.getInstance().playVibrationSound(new BlockPos(x, y, z));
        }));
        return true;
    }

}
