/*
 * Player capability provider from DragonBallZ provided by Zanckor
 */

package net.migats21.sculkinfected.capabilities;

import net.migats21.sculkinfected.SculkInfected;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkTimerProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation location = new ResourceLocation(SculkInfected.MODID, "sculkinfection");
    public static final Capability<ISculkTimer> SCULK_TIMER = CapabilityManager.get(new CapabilityToken<>(){});
    private SculkTimer instance;
    private LazyOptional<ISculkTimer> lazyOptional = LazyOptional.of(() -> instance);

    public SculkTimerProvider(ServerPlayer player) {
        instance = new SculkTimer(() -> player);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return SCULK_TIMER.orEmpty(cap, lazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    // Not in use, but still important
    public void invalidateCaps() {
        lazyOptional.invalidate();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}
