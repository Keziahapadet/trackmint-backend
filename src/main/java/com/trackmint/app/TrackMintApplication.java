package com.trackmint.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TrackMintApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackMintApplication.class, args);
	}

}
