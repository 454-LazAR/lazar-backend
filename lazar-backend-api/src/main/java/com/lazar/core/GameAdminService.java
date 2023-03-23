package com.lazar.core;

import com.lazar.model.Game;
import com.lazar.model.Ping;
import com.lazar.model.Player;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameAdminService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    public Ping ping() {
        return null;
    }

    public Game start() {
        return null;
    }
    public Player join() {
        return null;
    }
}
