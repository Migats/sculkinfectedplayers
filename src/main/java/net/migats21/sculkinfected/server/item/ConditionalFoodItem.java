/*
 * Coded by Migats21
 * License: MIT Licensed
 */

package net.migats21.sculkinfected.server.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class ConditionalFoodItem extends Item {

    public final Predicate<Player> condition;
    public ConditionalFoodItem(Properties properties, FoodProperties food, Predicate<Player> condition) {
        super(properties.food(food));
        this.condition = condition;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result;
        if (condition.test(player)) {
            result = super.use(level, player, hand);
        } else {
            result = InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return result;
    }

}
