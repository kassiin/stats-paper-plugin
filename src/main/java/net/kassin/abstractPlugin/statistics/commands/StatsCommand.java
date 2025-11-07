package net.kassin.abstractPlugin.statistics.commands;

import net.kassin.abstractPlugin.statistics.StatsService;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record StatsCommand(StatsService service) implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            return true;
        }

        CompletableFuture<PlayerStats> statsFuture = service.getPlayerStats(player);

        switch (args[0].toLowerCase()) {
            case "kill" -> statsFuture.thenAccept(stats ->
                    player.sendMessage("§aSeus abates: §f" + stats.getKills()));
            case "death" -> statsFuture.thenAccept(stats ->
                    player.sendMessage("§cSuas mortes: §f" + stats.getDeaths()));
            default -> player.sendMessage("§eUso: /" + label + " <kill|death>");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("kill", "death")
                    .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

}
