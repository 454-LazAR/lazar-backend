package com.lazar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Player implements Serializable {
    private UUID id;
    private String gameId;
    private String username;
    private Integer health;
    private Boolean isAdmin;
    private Instant lastUpdateTime;

    public Player(UUID id, String gameId) {
        this.id = id;
        this.gameId = gameId;
    }

    public Player(String id, String health, String lastUpdateTime) {
        this.id = UUID.fromString(id);
        this.health = Integer.parseInt(health);
        this.lastUpdateTime = Timestamp.valueOf(lastUpdateTime).toInstant();
    }

    public Player(String id, String gameId, String username, String health, String isAdmin) {
        this.id = UUID.fromString(id);
        this.gameId = gameId;
        this.username = username;
        this.health = Integer.parseInt(health);
        this.isAdmin = isAdmin != null && Integer.parseInt(isAdmin) == 1;
    }
}
