package me.leoflo.tpa;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;

public class TpaCommands {
    private static TpaCommands instance = null;

    private HashMap<ServerPlayerEntity, ServerPlayerEntity> tptable = new HashMap<>();

    private int tprequest(CommandContext<ServerCommandSource> ctx, String playerName) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerPlayerEntity target = PlayerUtilities.getInstance().getPlayer(ctx.getSource(), playerName);

        if (target == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] il player richiesto non esiste.").withColor(0xAA0000), false);
            return 2;
        }

        if (tptable.get(player) != null) {
            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] ha già una richiesta in sospeso.").withColor(0xAA0000), false);
            return 3;
        }

        tptable.put(player, target);

        ctx.getSource().sendFeedback(() -> Text.literal("[TPA] tp richiesto, aspetta.").withColor(0xFFAA00), false);
        target.sendMessage(Text.literal("[TPA] " + player.getName().getString() + " vuole teletrasportarsi da te negro/a.").withColor(0xFFAA00));

        return 1;
    }

    private int tpcancel(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerPlayerEntity target = tptable.get(player);

        if (target == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] non hai richieste in sospeso.").withColor(0xAA0000), false);
            return 2;
        }

        tptable.remove(player);

        ctx.getSource().sendFeedback(() -> Text.literal("[TPA] tp cancellato.").withColor(0xFF5555), false);
        target.sendMessage(Text.literal("[TPA] " + player.getName().getString() + " ha cancellato la richiesta.").withColor(0xFF5555));

        return 1;
    }

    private int tpaccept(CommandContext<ServerCommandSource> ctx, String playerName, boolean accept) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerPlayerEntity target = PlayerUtilities.getInstance().getPlayer(ctx.getSource(), playerName);
        MinecraftServer server = ctx.getSource().getServer();

        if (target == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] il player richiesto non esiste.").withColor(0xAA0000), false);
            return 2;
        }

        if (!player.equals(tptable.get(target))) {
            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] il player non ha richiesto il tp.").withColor(0xAA0000), false);
            return 3;
        }

        if (accept) {
            PlayerUtilities.getInstance().teleport(server, target, player);
            tptable.remove(target);

            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] tp accettato.").withColor(0x55FF55), false);
            target.sendMessage(Text.literal("[TPA] ti sei teletrasportato da " + player.getName().getString() + ".").withColor(0x55FF55));
        } else {
            tptable.remove(target);

            ctx.getSource().sendFeedback(() -> Text.literal("[TPA] tp rifiutato.").withColor(0xFF5555), false);
            target.sendMessage(Text.literal("[TPA] " + player.getName().getString() + " ha rifiutato la tua richiesta.").withColor(0xFF5555));
        }

        return 1;
    }

    public void register(Logger LOGGER) {
        LOGGER.info("Registrazione dei comandi ...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("tpa")
                            .requires(source -> source.hasPermissionLevel(0))
                            .then(CommandManager.argument("player", StringArgumentType.string())
                                    .suggests(PlayerSuggestionProvider.getInstance())
                                    .executes(ctx -> tprequest(
                                            ctx,
                                            StringArgumentType.getString(ctx, "player")
                                    )))
            );

            dispatcher.register(
                    CommandManager.literal("tpcancel")
                            .requires(source -> source.hasPermissionLevel(0))
                            .executes(ctx -> tpcancel(ctx))
            );

            dispatcher.register(
                    CommandManager.literal("tpaccept")
                            .requires(source -> source.hasPermissionLevel(0))
                            .then(CommandManager.argument("player", StringArgumentType.string())
                                    .suggests(PlayerSuggestionProvider.getInstance())
                                    .executes(ctx -> tpaccept(
                                            ctx,
                                            StringArgumentType.getString(ctx, "player"),
                                            true
                                    )))
            );

            dispatcher.register(
                    CommandManager.literal("tpreject")
                            .requires(source -> source.hasPermissionLevel(0))
                            .then(CommandManager.argument("player", StringArgumentType.string())
                                    .suggests(PlayerSuggestionProvider.getInstance())
                                    .executes(ctx -> tpaccept(
                                            ctx,
                                            StringArgumentType.getString(ctx, "player"),
                                            false
                                    )))
            );
        });

        LOGGER.info("Comandi registrati.");
    }

    public static TpaCommands getInstance() {
        if (instance == null) {
            instance = new TpaCommands();
        }

        return instance;
    }
}
