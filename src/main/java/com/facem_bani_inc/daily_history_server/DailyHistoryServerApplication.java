package com.facem_bani_inc.daily_history_server;

import com.facem_bani_inc.daily_history_server.config.ResendProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(ResendProperties.class)
@EnableScheduling
public class DailyHistoryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyHistoryServerApplication.class, args);
	}

}
