package com.isa.jutjubic;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry //3.11 cluster
@EnableScheduling // za scheduling preracunavanja tilesa za mapu
@EnableCaching
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
