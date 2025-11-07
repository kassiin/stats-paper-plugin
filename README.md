# ⛏️ abstract-plugin (Minecraft/Paper Statistics Client)

## Overview

This project is the **Game Client Component** of our distributed statistics architecture. Built as a Paper/Spigot Plugin, its primary goal is to **collect data** and **consume data** from the `stats-api-rest` service in a completely **non-blocking manner**, ensuring the server's **TPS (Ticks Per Second)** remains high.

---

## ⚙️ Optimization Architecture (The Senior Focus)

The design of this plugin prioritizes the safety of the Bukkit Main Thread by utilizing robust design patterns and asynchronous programming techniques.

### 1. Persistence Decoupling and TPS Protection

All I/O operations (network, cache, persistence) are deliberately offloaded from the Main Thread to dedicated threads.

#### Design Pattern: Repository Chain (Chain of Responsibility/Delegation)

Data access follows a strict hierarchy of responsibility to maximize performance:

| Repository | Technology | Purpose |
| :--- | :--- | :--- |
| **`AsyncStatsRepository`** | **CompletableFuture** | Guarantees that the underlying network/cache call executes in a dedicated thread pool (Executor), returning an immediate Promise (`CompletableFuture`). |
| **`CachedRepository`** | **Caffeine (L1 In-Memory Cache)** | Serves data from memory (near-zero latency), acting as the first defense against network latency. |
| **`RestRepository`** | **HttpClient (REST)** | Handles the final HTTP call (GET/PUT) to the `stats-api-rest`. |

### 2. Cache Coherence Guarantee (Active Invalidation)

The plugin acts as the **Subscriber** in the Redis Pub/Sub system:

* **`RedisInvalidationListener`**: Runs on a **separate, permanent Thread** and listens for update messages from the backend.
* **Instant Invalidation:** Upon receiving a message, it invokes `cache.invalidate(uuid)` directly on the Caffeine cache, ensuring the data is instantly stale and forcing a fresh load on the next request.

---

## 🧩 Asynchronous Implementation (CompletableFuture)

The code uses `CompletableFuture` to sequence asynchronous I/O and safely return control to the Bukkit API.

### Event Handling Flow (`StatsListener`)

The listener demonstrates how to handle I/O without blocking the game thread:

```java
// 1. Asynchronously initiate the I/O operation
CompletableFuture<PlayerStats> future = service.saveKill(killer.getPlayer());

// 2. Schedule the Bukkit API interaction (Main Thread) AFTER the I/O completes
future.thenAccept(stats -> {
    // Safely jump back to the Main Thread using the Scheduler
    Bukkit.getScheduler().runTask(AbstractPlugin.getInstance(), () ->
        killer.sendMessage("§aYour total kills: " + stats.getKills())
    );
});
