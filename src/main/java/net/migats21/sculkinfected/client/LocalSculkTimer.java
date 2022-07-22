package net.migats21.sculkinfected.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LocalSculkTimer extends SculkTimer {

    private static final LocalSculkTimer INSTANCE = new LocalSculkTimer();
    private static final SculkOverlay OVERLAY = new SculkOverlay();
    public boolean playedInfectionSound = false;
    private boolean playedCureSound = false;

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
}
