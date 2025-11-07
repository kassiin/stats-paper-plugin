package net.kassin.abstractPlugin.statistics.data.repo;

import net.kassin.abstractPlugin.repo.AsyncRepository;
import net.kassin.abstractPlugin.repo.Repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

public record AsyncStatsRepository<T>(Repository<T> delegate,
                                      Executor executor) implements AsyncRepository<T> {

    public AsyncStatsRepository(Repository<T> delegate) {
        this(delegate, Executors.newFixedThreadPool(4));
    }

    @Override
    public void save(T data) {
        delegate.save(data);
    }

    @Override
    public T get(UUID id) {
        return delegate.get(id);
    }

    @Override
    public void remove(UUID id) {
        delegate.remove(id);
    }

    @Override
    public CompletableFuture<Void> saveAsync(T data) {
        return CompletableFuture.runAsync(() -> delegate.save(data), executor);
    }

    @Override
    public CompletableFuture<T> getAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> delegate.get(id), executor);
    }

    @Override
    public CompletableFuture<Void> removeAsync(UUID id) {
        return CompletableFuture.runAsync(() -> delegate.remove(id), executor);
    }

    @Override
    public CompletableFuture<T> updateAsync(UUID id, Function<T, T> updateFunction) {
        return CompletableFuture.supplyAsync(() -> delegate.update(id, updateFunction), executor);
    }

}

