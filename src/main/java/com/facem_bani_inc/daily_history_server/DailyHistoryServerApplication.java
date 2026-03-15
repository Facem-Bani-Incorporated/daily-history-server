package com.facem_bani_inc.daily_history_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DailyHistoryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyHistoryServerApplication.class, args);
	}

}
