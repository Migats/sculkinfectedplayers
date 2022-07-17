package net.migats21.sculkinfected.client;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = SculkInfected.MODID, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void updateTimer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.CLIENT && Objects.requireNonNull(Minecraft.getInstance().gameMode).canHurtPlayer()) SculkTimer.getLocalInstance().tick();
    }
    @SubscribeEvent
    public static void renderSculkOverlay(RenderGuiEvent.Pre event) {
        if (!SculkTimer.getLocalInstance().isCured()) {
            SculkTimer.getLocalInstance().renderOverlay();
        }
    }
    @SubscribeEvent
    public static void renderSculkTimer(RenderGuiEvent.Post event) {
        if (!SculkTimer.getLocalInstance().isCured()) {
            SculkTimer.getLocalInstance().renderTimer(event.getPoseStack());
        }
    }
    @SubscribeEvent
    public static void removeTimer(ClientPlayerNetworkEvent.LoggingOut event) {
        SculkTimer.getLocalInstance().reset(false);
    }
    @SubscribeEvent
    public static void replaceSound(PlaySoundEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.getOriginalSound().getLocation().equals(SoundEvents.ELDER_GUARDIAN_CURSE.getLocation()) && SculkTimer.getLocalInstance() != null && SculkTimer.getLocalInstance().isJustInfected()) {
            event.setSound(new SimpleSoundInstance(ModSoundEvents.PLAYER_INFECTED.get(), SoundSource.PLAYERS, 1f, 1f, RandomSource.create(), player.getX(), player.getY(), player.getZ()));
        } else if (event.getOriginalSound().getLocation().equals(SoundEvents.TOTEM_USE.getLocation()) && SculkTimer.getLocalInstance() != null && SculkTimer.getLocalInstance().isCured()) {
            event.setSound(new SimpleSoundInstance(ModSoundEvents.PLAYER_CURED.get(), SoundSource.PLAYERS, 1f, 1f, RandomSource.create(), player.getX(), player.getY(), player.getZ()));
        }
    }
 }
