package com.lazar.controller;

import com.lazar.core.GameAdminService;
import com.lazar.core.GameEventService;
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

	@GetMapping("/")
	public Ping ping() {
		return gameEventService.ping();
	}

	@PostMapping("/create")
	public Player createGame(@RequestBody Player player) {
		return gameAdminService.create(player);
	}

	@PostMapping("/start")
	public Player startGame() {
		return gameAdminService.start();
	}

	@PostMapping("/join")
	public Player joinGame(@RequestBody Player player) {
		return gameAdminService.join(player);
	}

	@GetMapping("/check-hit")
	public void checkHit() {
		gameEventService.checkHit();
	}

	@GetMapping("/hello-world")
	public String helloWorld() {
		return "Hello world!";
	}
}
