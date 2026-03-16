package com.moveit.championship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChampionshipServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChampionshipServiceApplication.class, args);
	}
}