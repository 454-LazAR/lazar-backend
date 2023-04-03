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
        IN_PROGRESS, IN_LOBBY, FINISHED
    }
    private String id;
    private GameStatus gameStatus;

    public Game(String id, String gameStatus) {
        this.id = id;
        this.gameStatus = GameStatus.valueOf(gameStatus);
    }
}
