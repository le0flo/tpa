package me.leoflo.tpa;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private static PlayerSuggestionProvider instance = null;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        for (String name : commandContext.getSource().getPlayerNames()) {
            suggestionsBuilder.suggest(name);
        }

        return suggestionsBuilder.buildFuture();
    }

    public static PlayerSuggestionProvider getInstance() {
        if (instance == null) {
            instance = new PlayerSuggestionProvider();
        }

        return instance;
    }
}
