package com.lazar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Game implements Serializable {
    public enum GameStatus {
        IN_PROGRESS, IN_LOBBY, COMPLETE
    }
    private int id;
    private GameStatus gameStatus;

    public Game(String id, String gameStatus) {
        this.id = Integer.parseInt(id);
        this.gameStatus = GameStatus.valueOf(gameStatus);
    }
}
