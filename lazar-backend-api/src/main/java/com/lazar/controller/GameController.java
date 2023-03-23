package com.lazar.controller;

import com.lazar.core.GameAdminService;
import com.lazar.core.HitDetectionService;
import com.lazar.model.Game;
import com.lazar.model.Ping;
import com.lazar.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

	@Autowired
	private GameAdminService gameAdminService;

	@Autowired
	private HitDetectionService hitDetectionService;

	@GetMapping("/")
	public Ping ping() {
		return gameAdminService.ping();
	}

	@PostMapping("/start")
	public Game startGame() {
		return gameAdminService.start();
	}

	@PostMapping("/join")
	public Player joinGame() {
		return gameAdminService.join();
	}

	@GetMapping("/checkhit")
	public void checkHit() {
		hitDetectionService.check();
	}

}
