package net.migats21.sculkinfected.capabilities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.client.SculkOverlay;
import net.migats21.sculkinfected.network.ClientboundInfectionUpdatePacket;
import net.migats21.sculkinfected.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.OnlyIns;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SculkTimer implements ISculkTimer, INBTSerializable<CompoundTag> {

    @OnlyIn(Dist.CLIENT)
    private static final SculkTimer LOCAL_INSTANCE = new SculkTimer();
    @OnlyIn(Dist.CLIENT)
    private static final SculkOverlay timerOverlay = new SculkOverlay();
    private int time = -1;
    private static final int MAX_TIME = 2400000;

    public SculkTimer() {

    }

    public static ISculkTimer getFromPlayer(Player player) {
        if (player.level.isClientSide) {
            if (player.isLocalPlayer()) {
                return LOCAL_INSTANCE;
            } else {
                return null;
            }
        } else {
            return player.getCapability(SculkTimerProvider.SCULK_TIMER, null).orElse(null);
        }
    }

    public void tick() {
        if (time > -1) {
            time++;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getProgress() {
        return Mth.clamp((float)time / MAX_TIME, 0f, 1f);
    }

    @OnlyIn(Dist.CLIENT)
    public static SculkTimer getLocalInstance() {
        return LOCAL_INSTANCE;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTimer(PoseStack poseStack) {
        timerOverlay.renderSculkbar(poseStack);
    }

    @OnlyIn(Dist.CLIENT)
    public int getDaytime() {
        return (int) Math.min(Math.floor(time / 24000f) + 1, 100);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isWarning() {
        return time >= 2376000;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay() {
        timerOverlay.renderOverlay((float) Math.pow((float)time / MAX_TIME, 4d));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlash() {
        return time > MAX_TIME && time % 20 < 3;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isNearingDeath() {
        return time >= 2304000;
    }

    public float getDamage() {
        if (time > MAX_TIME && time % 100 == 0) {
            return (time - MAX_TIME) / 1000f;
        }
        return 0.0f;
    }
    @OnlyIn(Dist.CLIENT)
    public boolean isCured() {
        return time == -1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isJustInfected() {
        return time == 0;
    }

    @Override
    public int get() {
        return time;
    }

    @Override
    public void set(int i) {
        time = i;
    }

    public void setChanged(ServerPlayer player) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundInfectionUpdatePacket(time));
    }

    @Override
    public void copy(ISculkTimer sculkTimer) {
        this.time = sculkTimer.get();
    }

    @Override
    public void setRelative(int deltaTime) {
        time += deltaTime;
    }

    @Override
    public void reset(boolean onTick) {
        if (time > 0) {
            this.set(onTick ? 0 : -1);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("time", time);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("time")) {
            time = nbt.getInt("time");
        } else if (nbt.contains("infection_time")) {
            time = nbt.getInt("infection_time");
        }
    }
}
