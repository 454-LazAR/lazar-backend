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

    public Optional<Integer> getPlayerHealth(UUID playerId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("players.get.health"))
            .bind("id", playerId)
            .mapTo(Integer.class)
            .findOne()
        );
    }
}
