package net.kassin.abstractPlugin.repo;

import net.kassin.abstractPlugin.utils.DataBaseSource;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface Repository<T> {
    void save(T data);
    T get(UUID id);
    void remove(UUID id);

    default void initTable(DataBaseSource source) {}

    default T update(UUID id, Function<T, T> updateFunction) {
        Optional<T> data = Optional.ofNullable(get(id));
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data not found for UUID: " + id);
        }
        T updatedData = updateFunction.apply(data.get());
        save(updatedData);
        return updatedData;
    }

}
