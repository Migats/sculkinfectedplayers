/*
 * Copyright(c) Migats21
 * Reading permissions must be reserved
 */

package net.migats21.sculkinfected.server;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.capabilities.ISculkTimer;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.migats21.sculkinfected.capabilities.SculkTimerProvider;
import net.migats21.sculkinfected.network.ClientboundAmbientVibratePacket;
import net.migats21.sculkinfected.network.PacketHandler;
import net.migats21.sculkinfected.server.commands.InfectionCommand;
import net.migats21.sculkinfected.server.item.Items;
import net.migats21.sculkinfected.server.util.ModGameEventTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = SculkInfected.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void InfectedOrCured(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity entity = event.getSource().getEntity();
            if (entity != null && entity.getType() == EntityType.WARDEN) {
                if (player.addTag("sculk_infected")) {
                    SculkTimer.getFromPlayer(player).infect();
                    return;
                }
            } else if (event.getSource() == DamageSource.WITHER && player.getTags().contains("sculk_infected") && event.getAmount() >= player.getHealth()) {
                player.removeTag("sculk_infected");
                SculkTimer.getFromPlayer(player).cure();
                return;
            }
        }
        // Sculk infected players can one hit friendly creatures with his fist
        if (event.getSource().getDirectEntity() instanceof ServerPlayer player && player.getTags().contains("sculk_infected") && player.getMainHandItem().isEmpty() && !(event.getEntity() instanceof Enemy || event.getEntity().getTags().contains("sculk_infected"))) {
            event.setAmount(event.getEntity().getHealth() + event.getEntity().getAbsorptionAmount());
            event.getSource().bypassArmor().bypassEnchantments().bypassMagic().bypassInvul();
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void HealedBySculk(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        Entity entity = event.getEntity();
        // Sculk infected players can heal themself by killing mobs
        if (source instanceof ServerPlayer player && player.getTags().contains("sculk_infected") && entity instanceof LivingEntity livingEntity) {
            player.heal(Math.min(livingEntity.getExperienceReward() / 2f, 4f));
            return;
        }
        // Sculk infected players will drop all their levels
        if (entity instanceof ServerPlayer player && player.getTags().contains("sculk_infected")) {
            // They will not be able to respawn if the time is up
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
        // Sculk infected players can place an echo shard in a shrieker to give it the ability to spawn wardens
        if (blockState.is(Blocks.SCULK_SHRIEKER) && event.getItemStack().is(Items.ECHO_SHARD.get()) && event.getEntity().getTags().contains("sculk_infected") && !blockState.getValue(SculkShriekerBlock.CAN_SUMMON)) {
            level.setBlock(event.getPos(), blockState.setValue(SculkShriekerBlock.CAN_SUMMON, true), 1);
            event.setUseItem(Event.Result.DENY);
            if (((ServerPlayer) event.getEntity()).gameMode.isSurvival()) event.getItemStack().shrink(1);
        }
    }
    @SubscribeEvent
    public static void CatalystBroken(BlockEvent.BreakEvent event) {
        // If a catalyst gets broken, all sculk infected player will get damaged by the curse
        if (event.getState().is(Blocks.SCULK_CATALYST)) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            if (player != null && player.gameMode.isCreative()) return;
            Objects.requireNonNull(event.getPlayer().getServer()).getPlayerList().getPlayers().forEach((player1) -> {
                if (player1.getTags().contains("sculk_infected")) {
                    player1.hurt(ServerDamageSource.CATALYST, 10f);
                    if (player == player1) {
                        event.setExpToDrop(0);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void UpdateTime(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.player.getTags().contains("sculk_infected") && event.phase == TickEvent.Phase.START) {
            // Update the timer for sculk infection
            if (((ServerPlayer) event.player).gameMode.isSurvival()) {
                ISculkTimer sculkTimer = SculkTimer.getFromPlayer(event.player);
                if (sculkTimer == null) return;
                sculkTimer.tick();
                float damage = sculkTimer.getDamage();
                if (damage > 0.0f && !event.player.hasEffect(MobEffects.WITHER)) {
                    event.player.hurt(ServerDamageSource.SCULK, damage);
                }
            }
            long catalysts_count = event.player.getLevel().getBlockStates(event.player.getBoundingBox().inflate(15d)).filter((state) -> state.is(Blocks.SCULK_CATALYST)).count();
            // If a sculk infected player is nearby a catalyst, he will get strength and haste
            if (catalysts_count > 0) {
                MobEffectInstance damage_boost = event.player.getEffect(MobEffects.DAMAGE_BOOST);
                MobEffectInstance dig_speed = event.player.getEffect(MobEffects.DIG_SPEED);
                if (catalysts_count > 7) {
                    if (damage_boost == null || damage_boost.getDuration() < 160 || dig_speed == null || dig_speed.getDuration() < 160) {
                        event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, true, true));
                        event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 1, true, true));
                    }
                } else {
                    if (damage_boost == null || damage_boost.getDuration() < 160 || dig_speed == null || dig_speed.getDuration() < 160) {
                        event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0, true, true));
                        event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 0, true, true));
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void HandleVibrations(VanillaGameEvent event) {
        // Sculk infected players will not make sculk vibrations
        if (event.getCause() instanceof Player player && player.getTags().contains("sculk_infected")) {
            event.setCanceled(event.getVanillaEvent().is(ModGameEventTags.VIBRATION_CANCELABLE) && !player.hasEffect(MobEffects.DARKNESS) || player.isCreative());
        }
        // Sculk infected players will hear weird noises when a sculk vibration is nearby
        if (!event.isCanceled() && event.getVanillaEvent().is(ModGameEventTags.VIBRATION_DETECTABLE) && RandomSource.create().nextBoolean() && (event.getCause() == null || !event.getCause().isSilent())) {
            Level level = event.getLevel();
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(event.getEventPosition(), 16d, 16d, 16d), (player) -> player.getTags().contains("sculk_infected"));
            for (ServerPlayer player : players) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundAmbientVibratePacket(event.getEventPosition()));
            }
        }
    }
    @SubscribeEvent
    public static void ProgressTimerOnSleep(SleepFinishedTimeEvent event) {
        Level level = (Level) event.getLevel();
        if (!event.getLevel().isClientSide()) {
            int deltaTime = (int) (event.getNewTime() - level.getDayTime());
            if (event.getLevel().getServer() == null) return;
            // The timer for sculk infection progresses with the time when sleeping
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
