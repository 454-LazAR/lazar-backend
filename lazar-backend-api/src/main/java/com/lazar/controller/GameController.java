package com.lazar.controller;

import com.lazar.core.GameAdminService;
import com.lazar.core.GameEventService;
import com.lazar.model.GeoData;
import com.lazar.model.Ping;
import com.lazar.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

	@Autowired
	private GameAdminService gameAdminService;

	@Autowired
	private GameEventService gameEventService;

	@PostMapping("/game-ping")
	public Ping gamePing(@RequestBody GeoData geoData) {
		return gameEventService.gamePing(geoData);
	}

	@PostMapping("/lobby-ping")
	public Ping lobbyPing(@RequestBody GeoData geoData) {
		return gameEventService.lobbyPing(geoData);
	}

	@PostMapping("/create")
	public Player createGame(@RequestBody Player player) {
		return gameAdminService.create(player);
	}

	@PostMapping("/start")
	public boolean startGame(@RequestBody GeoData geoData) { // request takes in geoData, but we really only need the player UUID
		return gameAdminService.start(geoData);
	}

	@PostMapping("/join")
	public Player joinGame(@RequestBody Player player) {
		return gameAdminService.join(player);
	}

	@PostMapping("/check-hit")
	public boolean checkHit(@RequestBody GeoData geoData) {
		return gameEventService.checkHit(geoData);
	}

	@GetMapping("/hello-world")
	public String helloWorld() {
		return "Hello world!";
	}
}
