package com.trackmint.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnablingCashinf
public class TrackMintApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackMintApplication.class, args);
	}

}
