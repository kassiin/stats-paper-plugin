package net.kassin.abstractPlugin.statistics.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Data
public class PlayerStats {

    private UUID uuid;
    private int kills;
    private int deaths;

    @JsonCreator
    public PlayerStats(
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("kills") int kills,
            @JsonProperty("deaths") int deaths) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
    }

    @JsonIgnore
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
