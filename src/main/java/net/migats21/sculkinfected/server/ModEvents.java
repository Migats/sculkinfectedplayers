/*
 * Copyright(c) Migats21
 * Reading permissions must be reserved
 */

package net.migats21.sculkinfected.server;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.capabilities.ISculkTimer;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.migats21.sculkinfected.capabilities.SculkTimerProvider;
import net.migats21.sculkinfected.server.commands.InfectionCommand;
import net.migats21.sculkinfected.server.item.Items;
import net.migats21.sculkinfected.server.util.ModGameEventTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SculkInfected.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void InfectedOrCured(LivingHurtEvent event) {
        Entity entity = event.getSource().getEntity();
        if (event.getEntity() instanceof ServerPlayer player) {
            if (entity != null && entity.getType() == EntityType.WARDEN) {
                if (player.addTag("sculk_infected")) {
                    SculkTimer.getFromPlayer(player).infect();
                }
            } else if (event.getSource() == DamageSource.WITHER && player.getTags().contains("sculk_infected") && event.getAmount() >= player.getHealth()) {
                player.removeTag("sculk_infected");
                SculkTimer.getFromPlayer(player).cure();
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void HealedBySculk(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        Entity entity = event.getEntity();
        if (source instanceof ServerPlayer player && player.getTags().contains("sculk_infected") && entity instanceof LivingEntity livingEntity) {
            player.heal(Math.min(livingEntity.getExperienceReward() / 2f, 4f));
            return;
        }
        if (entity instanceof ServerPlayer player && player.getTags().contains("sculk_infected")) {
            if (event.getSource() == ServerDamageSource.SCULK) {
                player.setGameMode(GameType.SPECTATOR);
                SculkTimer.getFromPlayer(player).set(-1);
                player.removeTag("sculk_infected");
            }
            ExperienceOrb.award((ServerLevel) player.level, player.position(), player.totalExperience);
            player.experienceLevel = 0;
        }
    }
    @SubscribeEvent
    public static void ActivateShrieker(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;
        BlockState blockState = level.getBlockState(event.getPos());
        if (blockState.is(Blocks.SCULK_SHRIEKER) && event.getItemStack().is(Items.ECHO_SHARD.get()) && event.getEntity().getTags().contains("sculk_infected") && !blockState.getValue(SculkShriekerBlock.CAN_SUMMON)) {
            level.setBlock(event.getPos(), blockState.setValue(SculkShriekerBlock.CAN_SUMMON, true), 1);
            event.setUseItem(Event.Result.DENY);
            if (((ServerPlayer) event.getEntity()).gameMode.isSurvival()) event.getItemStack().shrink(1);
        }
    }
    @SubscribeEvent
    public static void CatalystBroken(BlockEvent.BreakEvent event) {
        if (event.getState().is(Blocks.SCULK_CATALYST)) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            if (player != null && player.gameMode.isCreative()) return;
            List<? extends Player> playerList = event.getPlayer().getLevel().players();
            for(Player player1 : playerList) {
                if (player1.getTags().contains("sculk_infected")) {
                    player1.hurt(ServerDamageSource.CATALYST, 10);
                    if (player == player1) {
                        event.setExpToDrop(0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void UpdateTime(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.player.getTags().contains("sculk_infected") && event.phase == TickEvent.Phase.START) {
            if (((ServerPlayer) event.player).gameMode.isSurvival()) {
                ISculkTimer sculkTimer = SculkTimer.getFromPlayer(event.player);
                if (sculkTimer == null) return;
                sculkTimer.tick();
                float damage = sculkTimer.getDamage();
                if (damage > 0.0f && !event.player.hasEffect(MobEffects.WITHER)) {
                    event.player.hurt(ServerDamageSource.SCULK, damage);
                }
            }
            MobEffectInstance damage_boost = event.player.getEffect(MobEffects.DAMAGE_BOOST);
            MobEffectInstance dig_speed = event.player.getEffect(MobEffects.DIG_SPEED);
            long catalysts_count = event.player.getLevel().getBlockStates(event.player.getBoundingBox().inflate(15d)).filter((state) -> state.is(Blocks.SCULK_CATALYST)).count();
            if (catalysts_count > 7) {
                if (damage_boost == null || damage_boost.getDuration() < 160) event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, true, true));
                if (dig_speed == null || dig_speed.getDuration() < 160) event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 1, true, true));
            } else if (catalysts_count > 0) {
                if (damage_boost == null || damage_boost.getDuration() < 160) event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0, true, true));
                if (dig_speed == null || dig_speed.getDuration() < 160) event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 0, true, true));
            }
        }
    }
    @SubscribeEvent
    public static void CancelVibrations(VanillaGameEvent event) {
        if (event.getVanillaEvent().is(ModGameEventTags.VIBRATION_CANCELABLE) && event.getCause() instanceof Player player && player.getTags().contains("sculk_infected") && !player.hasEffect(MobEffects.DARKNESS)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void ProgressTimerOnSleep(SleepFinishedTimeEvent event) {
        Level level = (Level) event.getLevel();
        if (!event.getLevel().isClientSide()) {
            int deltaTime = (int) (event.getNewTime() - level.getDayTime());
            event.getLevel().getServer().getPlayerList().getPlayers().forEach((player) -> {
                if (player.getTags().contains("sculk_infected")) {
                    ISculkTimer sculkTimer = SculkTimer.getFromPlayer(player);
                    sculkTimer.setRelative(deltaTime);
                }
            });
        }
    }
    @SubscribeEvent
    public static void CloneCapability(PlayerEvent.Clone event) {
        Player player_old = event.getOriginal();
        Player player_new = event.getEntity();
        if (event.isWasDeath() && player_old.getTags().contains("sculk_infected")) {
            player_old.reviveCaps();
            player_old.getCapability(SculkTimerProvider.SCULK_TIMER).ifPresent((sculkTimer) -> {
                player_new.getCapability(SculkTimerProvider.SCULK_TIMER).ifPresent((sculkTimer1) -> {
                    sculkTimer1.copy(sculkTimer);
                });
            });
            player_old.invalidateCaps();
        }
    }
    @SubscribeEvent
    public static void RegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ISculkTimer.class);
    }
    @SubscribeEvent
    public static void AttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer player) {
            event.addCapability(SculkTimerProvider.location, new SculkTimerProvider(player));
        }
    }
    @SubscribeEvent
    public static void RegisterPlayerInfection(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getTags().contains("sculk_infected")) {
            SculkTimer.getFromPlayer(player).setChanged(false);
        }
    }
    @SubscribeEvent
    public static void RegisterInfectionCommand(RegisterCommandsEvent event) {
        InfectionCommand.register(event.getDispatcher());
    }
}
