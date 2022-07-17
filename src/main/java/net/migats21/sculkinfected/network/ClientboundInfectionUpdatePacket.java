package net.migats21.sculkinfected.network;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ClientboundInfectionUpdatePacket {
    public final int sculk_time;

    public ClientboundInfectionUpdatePacket(int sculk_time) {
        this.sculk_time = sculk_time;
    }

    public ClientboundInfectionUpdatePacket(FriendlyByteBuf buffer) {
        this.sculk_time = buffer.readInt();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(sculk_time);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        final AtomicBoolean status = new AtomicBoolean(false);
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            SculkInfected.LOGGER.info("The sculktimer has been synchronized with the server");
            SculkTimer.getLocalInstance().set(sculk_time);
            status.set(true);
        }));
        context.get().setPacketHandled(true);
        return status.get();
    }
}
