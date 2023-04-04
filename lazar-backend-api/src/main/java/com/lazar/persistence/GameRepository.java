package com.lazar.persistence;

import com.lazar.model.Game;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Properties;

@Repository
public class GameRepository {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private Properties queries;

    public Optional<Game> getGame(String gameId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("games.get.by.gameId"))
                .bind("id", gameId)
                .map((r, c) -> new Game(r.getString(1), r.getString(2)))
                .findOne());
    }

    public boolean insertGame(String gameId) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("games.insert"))
                .bind("id", gameId)
                .execute());
        return status == 1;
    }

    public boolean updateGameStatus(String gameId, Game.GameStatus gameStatus) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("games.update"))
            .bind("id", gameId)
            .bind("gameStatus", gameStatus.toString())
            .execute());
        return status == 1;
    }

}
