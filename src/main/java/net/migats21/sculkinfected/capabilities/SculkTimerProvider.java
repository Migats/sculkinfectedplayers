/*
 * Player capability provider from DragonBallZ provided by Zanckor
 */

package net.migats21.sculkinfected.capabilities;

import net.migats21.sculkinfected.SculkInfected;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SculkTimerProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation location = new ResourceLocation(SculkInfected.MODID, "sculkinfection");
    public static final Capability<ISculkTimer> SCULK_TIMER = CapabilityManager.get(new CapabilityToken<>(){});
    private SculkTimer instance = new SculkTimer();
    private LazyOptional<ISculkTimer> lazyOptional = LazyOptional.of(() -> instance);

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
