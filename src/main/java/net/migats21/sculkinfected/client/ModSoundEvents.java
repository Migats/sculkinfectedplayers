package net.migats21.sculkinfected.client;

import net.migats21.sculkinfected.SculkInfected;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public class ModSoundEvents {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS.getRegistryName(), SculkInfected.MODID);
    public static final RegistryObject<SoundEvent> PLAYER_INFECTED = register("entity.player.sculkinfection.infected");
    public static final RegistryObject<SoundEvent> PLAYER_CURED = register("entity.player.sculkinfection.cured");
    // Work in progress. Not used yet
    public static final RegistryObject<SoundEvent> PLAYER_HURT = register("entity.player.sculkinfection.hurt");
    public static final RegistryObject<SoundEvent> PLAYER_DEATH = register("entity.player.sculkinfection.death");

    private static RegistryObject<SoundEvent> register(String key) {
        return SOUND_EVENTS.register(key, () -> new SoundEvent(new ResourceLocation(SculkInfected.MODID, key)));
    }
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
