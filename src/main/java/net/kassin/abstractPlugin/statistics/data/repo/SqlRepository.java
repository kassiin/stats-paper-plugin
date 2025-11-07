package net.kassin.abstractPlugin.statistics.data.repo;

import net.kassin.abstractPlugin.repo.Repository;
import net.kassin.abstractPlugin.utils.DataBaseSource;
import net.kassin.abstractPlugin.statistics.data.PlayerStats;

import java.sql.*;
import java.util.UUID;

public record SqlRepository(DataBaseSource source) implements Repository<PlayerStats> {
    public SqlRepository(DataBaseSource source) {
        this.source = source;
        initTable(source);
    }

    @Override
    public void save(PlayerStats data) {
        String SQL = """
                INSERT INTO player_stats (player_uuid, kills, deaths)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                kills = VALUES(kills),
                deaths = VALUES(deaths)
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL)) {

            ps.setString(1, data.getPlayer().getUniqueId().toString());
            ps.setInt(2, data.getKills());
            ps.setInt(3, data.getDeaths());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerStats get(UUID id) {
        String SQL = """
                SELECT kills, deaths FROM player_stats WHERE player_uuid = ?
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int kills = rs.getInt("kills");
                int deaths = rs.getInt("deaths");
                return new PlayerStats(id, kills, deaths);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerStats(id, 0, 0);
    }

    @Override
    public void remove(UUID id) {
        String SQL = """
                DELETE FROM player_stats WHERE player_uuid = ?
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initTable(DataBaseSource source) {
        String SQL = """
                    CREATE TABLE IF NOT EXISTS player_stats (
                        player_uuid CHAR(36) NOT NULL PRIMARY KEY,
                        kills INT DEFAULT 0,
                        deaths INT DEFAULT 0
                    );
                """;

        try (Connection connection = source.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate(SQL);
            System.out.println("[SQL] Tabela 'player_stats' verificada/criada com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
