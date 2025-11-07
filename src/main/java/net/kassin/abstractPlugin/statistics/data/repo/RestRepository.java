package net.kassin.abstractPlugin.statistics.data.repo;

import com.fasterxml.jackson.databind.ObjectMapper; // Importar o Jackson
import net.kassin.abstractPlugin.AbstractPlugin;
import net.kassin.abstractPlugin.repo.Repository;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.function.Function;

public class RestRepository implements Repository<PlayerStats> {

    private static final String BASE_URL = "http://localhost:8080/api/stats/";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(PlayerStats data) {
        String playerIdentifier = data.getUuid().toString();
        try {
            String json = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + playerIdentifier))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                UUID playerUUID = data.getUuid();
                AbstractPlugin.getInstance().getLogger().warning("Erro ao salvar stats para " + playerUUID + ". Código: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerStats get(UUID id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + id.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AbstractPlugin.getInstance().getLogger().info("Stats carregados para " + id);
                return objectMapper.readValue(response.body(), PlayerStats.class);
            }

            if (response.statusCode() != 404) {
                AbstractPlugin.getInstance().getLogger().warning("Erro ao carregar stats para " + id + ". Código: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PlayerStats(id, 0, 0);
    }


    @Override
    public void remove(UUID id) {
        // Implementar DELETE aqui, se necessário
    }

    @Override
    public PlayerStats update(UUID id, Function<PlayerStats, PlayerStats> updateFunction) {
        PlayerStats currentStats = get(id);
        PlayerStats updated = updateFunction.apply(currentStats);
        save(updated);
        return updated;
    }

}