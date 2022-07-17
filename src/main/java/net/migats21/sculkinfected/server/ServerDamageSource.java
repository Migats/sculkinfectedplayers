/*
 * Coded by Migats21
 * License: MIT Licensed
 */
package net.migats21.sculkinfected.server;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ServerDamageSource extends DamageSource {
    public final String GLOBAL_MESSAGE;
    // To apply death messages without the need of a translation key on the client
    public ServerDamageSource(String key, String global_message) {
        super(key);
        this.GLOBAL_MESSAGE = global_message;
    }

    public static final DamageSource CATALYST = new ServerDamageSource("catalyst"," got killed by sculk chain reaction").bypassArmor().bypassEnchantments().bypassMagic();
    public static final DamageSource SCULK = new ServerDamageSource("sculk", " is taken over by sculk infestation").bypassArmor().bypassMagic().bypassEnchantments();

    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        return Component.literal(entity.getDisplayName().getString() + GLOBAL_MESSAGE);
    }
}
