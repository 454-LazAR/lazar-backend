package com.lazar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Ping {
    private Game.GameStatus gameStatus;
    private Boolean isInactive;
    private Integer health;
    private List<String> usernames;
}
