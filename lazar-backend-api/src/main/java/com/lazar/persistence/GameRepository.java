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

    public Optional<Game> selectGame(Integer gameId) {
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("games.get.by.gameId"))
                .bind("id", gameId)
                .map((r, c) -> new Game(r.getString(1), r.getString(2)))
                .findOne());
    }

}
