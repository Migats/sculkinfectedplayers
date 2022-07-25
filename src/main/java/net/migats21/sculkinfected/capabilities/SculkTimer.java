package net.migats21.sculkinfected.capabilities;

import net.migats21.sculkinfected.SculkInfected;
import net.migats21.sculkinfected.client.LocalSculkTimer;
import net.migats21.sculkinfected.network.ClientboundInfectionUpdatePacket;
import net.migats21.sculkinfected.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SculkTimer implements ISculkTimer, INBTSerializable<CompoundTag> {
    protected int time = -1;
    private final Supplier<Player> OWNER;
    protected static final int MAX_TIME = 2400000;

    public SculkTimer(Supplier<Player> player) {
        OWNER = player;
    }

    public static ISculkTimer getFromPlayer(Player player) {
        if (player.level.isClientSide) {
            if (player.isLocalPlayer()) {
                return LocalSculkTimer.getInstance();
            } else {
                SculkInfected.LOGGER.error("Can't access remote player {} from client", player.getDisplayName().getString());
                return null;
            }
        } else {
            return player.getCapability(SculkTimerProvider.SCULK_TIMER, null).orElse(null);
        }
    }

    @Override
    public void tick() {
        if (time > -1) {
            time++;
        }
    }


    @Override
    public int get() {
        return time;
    }

    @Override
    public float getDamage() {
        if (time > MAX_TIME && time % 100 == 0) {
            return (time - MAX_TIME) / 1000f;
        }
        return 0.0f;
    }

    @Override
    public void set(int i) {
        time = i;
        setChanged(false);
    }

    @Override
    public void setChanged(boolean transform) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) OWNER.get()), new ClientboundInfectionUpdatePacket(time, transform));
    }

    @Override
    public void copy(ISculkTimer sculkTimer) {
        this.time = sculkTimer.get();
    }

    @Override
    public void setRelative(int deltaTime) {
        if (time < MAX_TIME) {
            time = Math.min(time + deltaTime, MAX_TIME);
            setChanged(false);
        }
    }

    @Override
    public void reset() {
        time = -1;
        setChanged(false);
    }

    @Override
    public void infect() {
        if (time > -1) {
            SculkInfected.LOGGER.error("Cannot run 'infect' since player is already infected");
            return;
        }
        time = 0;
        setChanged(true);
        Player player = OWNER.get();
        // If the player is moving while the sound is playing, it still must be present to it's current position.
        player.playNotifySound(SoundEvents.ELDER_GUARDIAN_CURSE, player.getSoundSource(), 1f, 1f);
        player.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1f, 1f);
        Component message = Component.literal(player.getDisplayName().getString() + " got sculk infected");
        player.getServer().getPlayerList().broadcastSystemMessage(message, ChatType.SYSTEM);
    }

    @Override
    public void cure() {
        if (time == -1) {
            SculkInfected.LOGGER.error("Cannot run 'cure' since player is not infected");
            return;
        }
        time = -1;
        setChanged(true);
        Player player = OWNER.get();
        player.heal(20f);
        // If the player is moving while the sound is playing, it still must be present to it's current position.
        player.playNotifySound(SoundEvents.TOTEM_USE, player.getSoundSource(), 1f, 1f);
        player.playSound(SoundEvents.TOTEM_USE, 1f, 1f);
        Component message = Component.literal(player.getDisplayName().getString() + " got cured from sculk infection");
        player.getServer().getPlayerList().broadcastSystemMessage(message, ChatType.SYSTEM);
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
