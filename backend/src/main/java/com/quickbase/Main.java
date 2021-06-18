package com.quickbase;

import com.quickbase.service.PopulationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

@Slf4j
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final PopulationService populationService;

    public Main(PopulationService populationService) {
        this.populationService = populationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Application started");
        populationService.calculatePopulationByCountry()
                .blockOptional(Duration.ofMillis(100))
                .ifPresentOrElse(
                        result -> result.forEach((key, value) -> log.info("{} - {}", key, value)),
                        () -> log.info("Unable to calculate country population."));
    }
}