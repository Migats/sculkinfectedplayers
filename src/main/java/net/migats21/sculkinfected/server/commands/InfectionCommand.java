package net.migats21.sculkinfected.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.migats21.sculkinfected.capabilities.ISculkTimer;
import net.migats21.sculkinfected.capabilities.SculkTimer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class InfectionCommand {

    public static final SimpleCommandExceptionType UNINFECTED_ERROR = new SimpleCommandExceptionType(Component.literal("Players are not infected"));
    public static final SimpleCommandExceptionType NO_INFECTION_ERROR = new SimpleCommandExceptionType(Component.literal("No players are infected"));
    public static final SimpleCommandExceptionType INFECTED_ERROR = new SimpleCommandExceptionType(Component.literal("Players are already infected"));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("infection").requires((sourceStack) ->
            sourceStack.hasPermission(2)).then(
                Commands.literal("add").then(Commands.argument("target", EntityArgument.players()).executes(
                    (commandContext) -> addInfection(commandContext.getSource(), EntityArgument.getPlayers(commandContext,"target"))
                ))
            ).then(
                Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).executes(
                    (commandContext) -> clearInfection(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"))
                ))
            ).then(
                Commands.literal("get").then(Commands.argument("target", EntityArgument.players()).executes(
                    (commandContext) -> getTime(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "target"), false)
                ).then(
                    Commands.literal("days").executes(
                        (commandContext) -> getTime(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "target"), true)
                    )
                ))
            ).then(
                Commands.literal("set").then(Commands.argument("target", EntityArgument.players()).then(
                    Commands.argument("time", IntegerArgumentType.integer(0, 2400000)).executes(
                        (commandContext) -> setTime(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), IntegerArgumentType.getInteger(commandContext, "time"))
                    ).then(
                        Commands.literal("days").executes(
                            (commandContext) -> setTime(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), IntegerArgumentType.getInteger(commandContext, "time") * 24000)
                        )
                    )
                ))
            ).then(
                Commands.literal("list").executes(
                    (commandContext) -> getList(commandContext.getSource())
                )
            )
        );
    }

    private static int getList(CommandSourceStack sourceStack) throws CommandSyntaxException {
        List<ServerPlayer> players = sourceStack.getServer().getPlayerList().getPlayers().stream().filter((player) -> player.getTags().contains("sculk_infected")).toList();
        if (players.isEmpty()) {throw NO_INFECTION_ERROR.create();}
        StringBuilder str = new StringBuilder();
        for (ServerPlayer player : players) {
            str.append(player.getDisplayName().getString()).append(", ");
        }
        sourceStack.sendSuccess(Component.literal(str.toString().replaceAll(", $",(players.size() == 1 ? " is" : " are") + " infected")), false);
        return players.size();
    }

    private static int clearInfection(CommandSourceStack sourceStack, Collection<ServerPlayer> target) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer player : target) {
            if (player.removeTag("sculk_infected")) {
                ISculkTimer sculkTimer = SculkTimer.getFromPlayer(player);
                sculkTimer.cure();
                i++;
            }
        }
        if (i == 0) {
            if (target.size() == 1) {
                throw new SimpleCommandExceptionType(Component.literal(target.iterator().next().getDisplayName().getString() + " is not infected")).create();
            } else {
                throw UNINFECTED_ERROR.create();
            }
        } else if (target.size() == 1) {
            sourceStack.sendSuccess(Component.literal("Cleared sculk infection from " + target.iterator().next().getDisplayName().getString()), false);
        } else {
            sourceStack.sendSuccess(Component.literal("Cleared sculk infection from " + target.size() + " players"), false);
        }
        return i;
    }

    private static int addInfection(CommandSourceStack sourceStack, Collection<ServerPlayer> target) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer player : target) {
            if (player.addTag("sculk_infected")) {
                ISculkTimer sculkTimer = SculkTimer.getFromPlayer(player);
                sculkTimer.infect();
                i++;
            }
        }
        if (i == 0) {
            if (target.size() == 1) {
                throw new SimpleCommandExceptionType(Component.literal(target.iterator().next().getDisplayName().getString() + " is already infected")).create();
            } else {
                throw INFECTED_ERROR.create();
            }
        } else if (target.size() == 1) {
            sourceStack.sendSuccess(Component.literal("Added sculk infection to " + target.iterator().next().getDisplayName().getString()), false);
        } else {
            sourceStack.sendSuccess(Component.literal("Added sculk infection to " + target.size() + " players"), false);
        }
        return i;
    }

    private static int getTime(CommandSourceStack sourceStack, ServerPlayer target, boolean bool) throws CommandSyntaxException {
        if (target.getTags().contains("sculk_infected")) {
            ISculkTimer sculkTimer = SculkTimer.getFromPlayer(target);
            int sculk_time = sculkTimer.get();
            if (bool) sculk_time = sculk_time / 24000;
            sourceStack.sendSuccess(Component.literal(target.getDisplayName().getString() + " has an infection time of " + sculk_time + (bool ? " days" : "")), false);
            return sculk_time;
        }
        throw new SimpleCommandExceptionType(Component.literal(target.getDisplayName().getString() + " is not infected")).create();
    }

    private static int setTime(CommandSourceStack sourceStack, Collection<ServerPlayer> target, int time) throws CommandSyntaxException {
        if (time > 2400000) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().create(time, 2400000);
        }
        int i = 0;
        for (ServerPlayer player : target) {
            if (player.getTags().contains("sculk_infected")) {
                ISculkTimer sculkTimer = SculkTimer.getFromPlayer(player);
                sculkTimer.set(time);
                i++;
            }
        }
        if (target.size() == 1) {
            if (i == 1) {
                sourceStack.sendSuccess(Component.literal("Set the infection time of " + target.iterator().next().getDisplayName().getString() + " to " + time), true);
            } else {
                throw new SimpleCommandExceptionType(Component.literal(target.iterator().next().getDisplayName().getString() + " is not infected")).create();
            }
        } else if (i > 0) {
            sourceStack.sendSuccess(Component.literal("Set the infection time to " + time + " for " + i + " players"), true);
        } else {
            throw UNINFECTED_ERROR.create();
        }
        return i;
    }
}
