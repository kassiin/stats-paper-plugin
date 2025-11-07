package net.kassin.abstractPlugin.statistics;

import com.github.benmanes.caffeine.cache.Cache;
import net.kassin.abstractPlugin.redis.RedisInvalidationListener;
import net.kassin.abstractPlugin.repo.AsyncRepository;
import net.kassin.abstractPlugin.repo.Repository;
import net.kassin.abstractPlugin.statistics.commands.StatsCommand;
import net.kassin.abstractPlugin.utils.DataBaseSource;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;
import net.kassin.abstractPlugin.statistics.data.repo.*;
import net.kassin.abstractPlugin.statistics.listeners.StatsListener;
import net.kassin.abstractPlugin.utils.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class StatsLoader {

    private final JavaPlugin plugin;
    private final StatsService service;
    private final DataBaseSource statsSource;

    public StatsLoader(JavaPlugin plugin) {
        this.plugin = plugin;

        Config config = new Config(plugin, "stats_db.yml");

        statsSource = null;
        // Repository<PlayerStats> dbRepository = new SqlRepository(statsSource);


        Repository<PlayerStats> restRepository = new RestRepository();
        Repository<PlayerStats> cacheRepository = new CachedRepository(restRepository);

        Cache<UUID, PlayerStats> caffeineCache = ((CachedRepository) cacheRepository).getInternalCache();
        final String REDIS_HOST = "localhost";
        final int REDIS_PORT = 6379;

        Thread listenerThread = new Thread(
                new RedisInvalidationListener(caffeineCache, REDIS_HOST, REDIS_PORT)
        );
        listenerThread.setName("Redis-PubSub-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        AsyncRepository<PlayerStats> asyncStatsRepository = new AsyncStatsRepository<>(cacheRepository);

        service = new StatsService(asyncStatsRepository);
    }

    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(new StatsListener(service), plugin);
        plugin.getCommand("stats").setExecutor(new StatsCommand(service));
    }

    public void disable() {
        statsSource.close();
    }

}
