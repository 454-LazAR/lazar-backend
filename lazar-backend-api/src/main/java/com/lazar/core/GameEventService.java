package com.lazar.core;

import com.lazar.model.Ping;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameEventService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    public Ping ping() {
        return null;
    }

    public void check() {

    }

    private void checkGameOver() {

    }

}
