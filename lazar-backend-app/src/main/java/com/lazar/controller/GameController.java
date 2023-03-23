package com.lazar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

	@GetMapping("/")
	public String ping() {
		return "Greetings from Spring Boot!";
	}

	@PostMapping("/start")
	public void startGame() {

	}

	@PostMapping("/join")
	public void joinGame() {

	}

	@GetMapping("/checkhit")
	public void checkHit() {

	}

}
