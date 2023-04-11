package com.lazar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Game implements Serializable {
    public enum GameStatus {
        IN_PROGRESS, IN_LOBBY, FINISHED, ABANDONED
    }
    private String id;
    private GameStatus gameStatus;
    private Instant latestGameStatusUpdate;

    public Game(String id, String gameStatus, String latestGameStatusUpdate) {
        this.id = id;
        this.gameStatus = GameStatus.valueOf(gameStatus);
        this.latestGameStatusUpdate = Timestamp.valueOf(latestGameStatusUpdate).toInstant();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime localDateTime = LocalDateTime.parse(latestGameStatusUpdate, formatter).atZone(ZoneId.systemDefault());

        this.latestGameStatusUpdate = localDateTime.withZoneSameInstant(ZoneId.of("GMT")).toInstant();
    }
}
