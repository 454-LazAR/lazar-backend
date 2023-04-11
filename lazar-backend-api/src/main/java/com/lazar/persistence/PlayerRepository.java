package com.lazar.persistence;

import com.lazar.model.Player;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Properties;
import java.util.List;
import java.util.UUID;

@Repository
public class PlayerRepository {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private Properties queries;

    public boolean insertPlayer(Player player) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("players.insert"))
                .bindBean(player)
                .execute());
        return status == 1;
    }

    public List<String> getUsernamesByGame(String gameId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("players.get.usernames"))
                .bind("id", gameId)
                .mapTo(String.class)
                .list());
    }

    public Optional<Player> getPlayerById(UUID playerId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("players.get.by.playerId"))
                .bind("id", playerId)
                .map((r, c) -> new Player(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5), r.getString(6)))
                .findOne());
    }

    public Optional<Player> getRecentPlayerById(UUID playerId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("players.get.recent.by.id"))
                .bind("id", playerId)
                .map((r, c) -> new Player(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)))
                .findOne());
    }

    public boolean updateHealth(UUID id, Integer decrementBy) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("players.update.health"))
                .bind("id", id)
                .bind("decrementBy", decrementBy)
                .execute());
        return status == 1;
    }
    public Optional<Integer> getPlayerHealth(UUID playerId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("players.get.health"))
            .bind("id", playerId)
            .mapTo(Integer.class)
            .findOne()
        );
    }

    public boolean updateInactive(UUID id) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("players.update.inactive"))
                .bind("id", id)
                .execute());
        return status == 1;
    }

}
