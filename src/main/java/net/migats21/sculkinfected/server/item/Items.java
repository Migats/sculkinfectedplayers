package net.migats21.sculkinfected.server.item;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Items {

    // Registering a deferred register for vanilla items
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");

    // This will not be called in a vanilla client, in that case there will be no eating animation and no sound
    public static final RegistryObject<Item> ECHO_SHARD = ITEMS.register("echo_shard", () -> new ConditionalFoodItem(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS), Foods.APPLE, (player) -> player.getTags().contains("sculk_infected")));

    // Adding the mod event bus to the registry
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
