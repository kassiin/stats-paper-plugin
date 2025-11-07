package net.kassin.abstractPlugin.statistics.data.repo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kassin.abstractPlugin.repo.Repository;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

public class CachedRepository implements Repository<PlayerStats> {

    private final Repository<PlayerStats> delegate;
    private final Cache<UUID, PlayerStats> cache;

    public CachedRepository(Repository<PlayerStats> delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    @Override
    public void save(PlayerStats data) {
        delegate.save(data);
        cache.put(data.getPlayer().getUniqueId(), data);
    }

    @Override
    public PlayerStats get(UUID id) {
        return cache.get(id, delegate::get);
    }

    @Override
    public void remove(UUID id) {
        delegate.remove(id);
        cache.invalidate(id);
    }

    @Override
    public PlayerStats update(UUID id, Function<PlayerStats, PlayerStats> updateFunction) {
        return cache.asMap().compute(id, (key, currentStats) -> {
            if (currentStats == null) {
                currentStats = delegate.get(key);
                if (currentStats == null) {
                    currentStats = new PlayerStats(key, 0, 0);
                }
            }
            PlayerStats updated = updateFunction.apply(currentStats);
            delegate.save(updated);
            return updated;
        });
    }

    public Cache<UUID, PlayerStats> getInternalCache() {
        return cache;
    }

}


