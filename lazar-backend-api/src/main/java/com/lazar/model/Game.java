package com.lazar.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Game {
    public enum GameStatus {
        IN_PROGRESS, WAITING, COMPLETE
    }
    private int id;
    private GameStatus gameStatus;
}
