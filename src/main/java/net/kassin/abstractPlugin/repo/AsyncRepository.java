package net.kassin.abstractPlugin.repo;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface AsyncRepository<T> extends Repository<T> {

    default CompletableFuture<Void> saveAsync(T data) {
        return CompletableFuture.runAsync(() -> save(data));
    }

    default CompletableFuture<Void> removeAsync(UUID uuid) {
       return CompletableFuture.runAsync(() -> remove(uuid));
    }

    default CompletableFuture<T> getAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> get(uuid));
    }

    default CompletableFuture<T> updateAsync(UUID id, Function<T, T> updateFunction) {
        return CompletableFuture.supplyAsync(() -> update(id, updateFunction));
    }

}
