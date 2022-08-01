package net.migats21.sculkinfected.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LocalSculkTimer extends SculkTimer {

    private static final LocalSculkTimer INSTANCE = new LocalSculkTimer();
    private static final SculkOverlay OVERLAY = new SculkOverlay();
    public boolean playedInfectionSound = false;
    private boolean playedCureSound = false;
    private int vibrationCooldown = 0;

    public LocalSculkTimer() {
        super(() -> Minecraft.getInstance().player);
    }

    public static void renderOverlay() {
        if (!INSTANCE.isCured()) {
            OVERLAY.renderOverlay(INSTANCE.getOverlayDarkness());
        }
    }

    public static void renderSculkbar(PoseStack poseStack) {
        if (!INSTANCE.isCured()) {
            OVERLAY.renderSculkbar(poseStack);
        }
    }

    @Override
    public void setChanged(boolean transform) {

    }

    @Override
    public void tick() {
        super.tick();
        if (vibrationCooldown > 0) vibrationCooldown--;
    }

    public static LocalSculkTimer getInstance() {
        return INSTANCE;
    }

    public int getDaytime() {
        return (int) Math.min(Math.floor(time / 24000f) + 1, 100);
    }

    public int getDaytimeColor() {
        if (time > MAX_TIME && time % 20 < 3) {
            return 16777215;
        }
        if (time >= 2376000) {
            return 11141120;
        }
        if (time >= 2304000) {
            return 16777045;
        }
        return 43690;
    }

    public float getOverlayDarkness() {
        return (float) Math.pow((float)time / MAX_TIME, 4d);
    }

    public boolean isCured() {
        return time == -1;
    }

    public float getProgress() {
        return Mth.clamp((float)time / MAX_TIME, 0f, 1f);
    }

    public boolean shouldPlayInfectionSound() {
        if (!playedInfectionSound) {
            playedInfectionSound = true;
            return true;
        }
        return false;
    }

    public boolean playedCureSound() {
        if (!playedCureSound) {
            playedCureSound = true;
            return true;
        }
        return false;
    }

    public void turnOnSound() {
        if (isCured()) {
            playedCureSound = false;
        } else {
            playedInfectionSound = false;
        }
    }

    public void playVibrationSound(BlockPos pos) {
        if (vibrationCooldown <= 0) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            RandomSource random = RandomSource.create();
            player.level.playSound(player, pos, ModSoundEvents.SCULK_VIBRATE.get(), SoundSource.AMBIENT, getProgress() * 0.8f, (random.nextFloat() - random.nextFloat()) * 0.4f + 1.0f);
        }
        vibrationCooldown = 20;
    }
}
