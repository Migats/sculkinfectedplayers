package net.migats21.sculkinfected.network;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.client.LocalSculkTimer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ClientboundInfectionUpdatePacket {
    public final int sculk_time;
    public final boolean transformed;

    public ClientboundInfectionUpdatePacket(int sculk_time, boolean transformed) {
        this.sculk_time = sculk_time;
        this.transformed = transformed;
    }

    public ClientboundInfectionUpdatePacket(FriendlyByteBuf buffer) {
        this.sculk_time = buffer.readInt();
        this.transformed = buffer.readBoolean();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(sculk_time);
        buffer.writeBoolean(transformed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        final AtomicBoolean status = new AtomicBoolean(false);
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            SculkInfected.LOGGER.info("The sculktimer has been synchronized with the server");
            LocalSculkTimer.getInstance().set(sculk_time);
            if (transformed) {
                LocalSculkTimer.getInstance().turnOnSound();
            }
            status.set(true);
        }));
        context.get().setPacketHandled(true);
        return status.get();
    }
}
