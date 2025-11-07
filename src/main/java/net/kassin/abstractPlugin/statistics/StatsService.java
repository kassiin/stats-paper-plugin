package net.kassin.abstractPlugin.statistics;

import net.kassin.abstractPlugin.repo.AsyncRepository;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StatsService {

    private final AsyncRepository<PlayerStats> repository;
    private final ConcurrentHashMap<UUID, CompletableFuture<Void>> eventQueues = new ConcurrentHashMap<>();

    public StatsService(AsyncRepository<PlayerStats> repository) {
        this.repository = repository;
    }

    public CompletableFuture<PlayerStats> saveKill(Player player) {
        UUID uuid = player.getUniqueId();
        return repository.updateAsync(uuid, currentStats ->
                new PlayerStats(uuid, currentStats.getKills() + 1, currentStats.getDeaths())
        );
    }

    public CompletableFuture<PlayerStats> saveDeath(Player player) {
        UUID uuid = player.getUniqueId();
        return repository.updateAsync(uuid, currentStats ->
                new PlayerStats(uuid, currentStats.getKills(), currentStats.getDeaths() + 1)
        );
    }

    public CompletableFuture<PlayerStats> processKillEvent(Player killer) {
        UUID uuid = killer.getUniqueId();

        CompletableFuture<Void> currentQueueTail = eventQueues.computeIfAbsent(uuid, k -> CompletableFuture.completedFuture(null));

        CompletableFuture<PlayerStats>[] killFutures = new CompletableFuture[1000];
        for (int i = 0; i < 1000; i++) {
            killFutures[i] = repository.updateAsync(uuid, currentStats ->
                    new PlayerStats(uuid, currentStats.getKills() + 1, currentStats.getDeaths())
            );
        }

        CompletableFuture<Void> allKillsDone = CompletableFuture.allOf(killFutures);

        CompletableFuture<Void> newQueueTail = currentQueueTail
                .thenCompose(v -> allKillsDone);

        eventQueues.put(uuid, newQueueTail);

        return newQueueTail
                .thenCompose(v -> repository.getAsync(uuid));
    }

    public CompletableFuture<Void> removePlayerStats(Player player) {
        return repository.removeAsync(player.getUniqueId());
    }

    public CompletableFuture<PlayerStats> getPlayerStats(Player player) {
        return repository.getAsync(player.getUniqueId());
    }

}

