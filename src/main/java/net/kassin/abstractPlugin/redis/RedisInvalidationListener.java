package net.kassin.abstractPlugin.redis;

import com.github.benmanes.caffeine.cache.Cache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisInvalidationListener extends JedisPubSub implements Runnable {

    private final static String CHANNEL = "stats-invalidation-channel";
    private final Cache<UUID, ?> cache;
    private final String redisHost;
    private final int redisPort;

    public RedisInvalidationListener(Cache<UUID, ?> cache, String redisHost, int redisPort) {
        this.cache = cache;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("[Cache-SYNC] Recebido UUID: " + message);
        if (CHANNEL.equalsIgnoreCase(channel)) {
            try {
                String cleanedMessage = message.trim().replace("\"", "");
                UUID uuid = UUID.fromString(cleanedMessage);
                cache.invalidate(uuid);
                System.out.println("[Cache-SYNC] Cache Caffeine invalidado para: " + uuid);
            } catch (IllegalArgumentException e) {
                System.out.println("[Cache-SYNC] UUID inválido: " + message);
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try (Jedis jedis = new Jedis(redisHost, redisPort)) {
                System.out.println("[Cache-SYNC] Conectado e ouvindo o canal Redis: " + CHANNEL);
                jedis.subscribe(this, CHANNEL);
            } catch (Exception e) {
                System.err.println("[Cache-SYNC] Erro de conexão Redis, tentando reconectar em 5s...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

}
