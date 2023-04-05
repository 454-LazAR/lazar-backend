package com.lazar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameInfo {

    private Game game;
    private int numAlivePlayers;
    private Game.GameStatus status;
    private int focusPlayerHealth;
}
