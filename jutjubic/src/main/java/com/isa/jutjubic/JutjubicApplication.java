package com.isa.jutjubic;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // za scheduling preracunavanja tilesa za mapu
@EnableCaching
@EnableRabbit
@SpringBootApplication
public class JutjubicApplication {

	public static void main(String[] args) {
		SpringApplication.run(JutjubicApplication.class, args);
	}

	//SAMO ZA PROVERU D ALI KORISTI DOBROG CACHEMANAGERA
	/*@Bean
	public CommandLineRunner checkCache(CacheManager cacheManager) {
		return args -> {
			System.out.println("------------------------------------------------");
			System.out.println("TRENUTNI CACHE MANAGER: " + cacheManager.getClass().getName());
			System.out.println("------------------------------------------------");
		};
	} */


}
