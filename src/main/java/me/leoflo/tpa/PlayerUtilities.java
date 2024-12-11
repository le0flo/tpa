package me.leoflo.tpa;

import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;

public class PlayerUtilities {
    private static PlayerUtilities instance = null;

    public ServerPlayerEntity getPlayer(ServerCommandSource source, String playerName) {
        PlayerManager playerManager = source.getServer().getPlayerManager();

        return playerManager.getPlayer(playerName);
    }

    public void teleport(MinecraftServer server, ServerPlayerEntity player, ServerPlayerEntity target) {
        server.executeSync(() -> {
            player.teleport(
                    target.getServerWorld(),
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    Collections.<PositionFlag>emptySet(),
                    player.getYaw(),
                    player.getPitch(),
                    false
            );
        });
    }

    public static PlayerUtilities getInstance() {
        if (instance == null) {
            instance = new PlayerUtilities();
        }

        return instance;
    }
}
