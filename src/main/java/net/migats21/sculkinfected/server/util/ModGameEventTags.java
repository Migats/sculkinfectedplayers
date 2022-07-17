package net.migats21.sculkinfected.server.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class ModGameEventTags {
    public static final TagKey<GameEvent> VIBRATION_CANCELABLE = create("vibration_cancelables");

    private static TagKey<GameEvent> create(String key) {
        return TagKey.create(Registry.GAME_EVENT_REGISTRY, new ResourceLocation(key));
    }

}
