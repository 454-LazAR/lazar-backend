package com.lazar.persistence;

import com.lazar.model.Player;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Properties;

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

}
