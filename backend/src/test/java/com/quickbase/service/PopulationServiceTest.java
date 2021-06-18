package com.quickbase.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.quickbase.db.CountryStatsRepository;
import com.quickbase.model.CountryStat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopulationServiceTest {

    @Mock
    private CountryStatsRepository dbCountryStatService;

    @Mock
    private RemoteCountryStatService remoteCountryStatService;

    @InjectMocks
    private PopulationService populationService;

    private final List<CountryStat> dbDataset = sampleDbDataset();
    private final List<CountryStat> remoteDataset = sampleRemoteDataset();

    @Test
    void emptySources() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(ImmutableList.of()));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(ImmutableList.of()));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .expectNext(ImmutableMap.of())
                .verifyComplete();
    }

    @Test
    void emptyDbSource() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(ImmutableList.of()));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(remoteDataset));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .assertNext(x -> assertEquals(x, toMap(remoteDataset)))
                .verifyComplete();
    }

    @Test
    void emptyRemoteSource() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(ImmutableList.of()));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .assertNext(x -> assertEquals(x, toMap(dbDataset)))
                .verifyComplete();
    }

    @Test
    void remoteDuplicateCountryStatEntry() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations())
                .thenReturn(Mono.just(ImmutableList.of(CountryStat.of("India", 100))));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .assertNext(x -> assertEquals(x, toMap(dbDataset)))
                .verifyComplete();
    }

    @Test
    void remoteNewCountryStatEntry() {
        final var expectedDataset = ImmutableList.<CountryStat>builder()
                .addAll(dbDataset)
                .add(CountryStat.of("Atlantis", 2200100))
                .build();

        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations())
                .thenReturn(Mono.just(ImmutableList.of(CountryStat.of("Atlantis", 2200100))));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .assertNext(x -> assertEquals(x, toMap(expectedDataset)))
                .verifyComplete();
    }

    @Test
    void remoteNewAndDuplicateCountryStatEntries() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(remoteDataset));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .assertNext(x -> assertEquals(x, sampleMergedDataset()))
                .verifyComplete();
    }

    @Test
    void failingRemoteStatService() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations())
                .thenReturn(Mono.error(new RuntimeException("Service call failed")));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .expectErrorMessage("Service call failed")
                .verify();
    }

    @Test
    void failingDbStatRepository() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.error(new SQLException("Failed to retrieve data")));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(remoteDataset));

        StepVerifier.create(populationService.calculatePopulationByCountry())
                .expectErrorMessage("Failed to retrieve data")
                .verify();
    }

    @Test
    void emptyMono() {
        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.just(dbDataset));
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.empty());
        StepVerifier.create(populationService.calculatePopulationByCountry()).expectComplete().verify();

        when(dbCountryStatService.getCountryPopulations()).thenReturn(Mono.empty());
        when(remoteCountryStatService.getCountryPopulations()).thenReturn(Mono.just(remoteDataset));
        StepVerifier.create(populationService.calculatePopulationByCountry()).expectComplete().verify();
    }

    private Map<String, Integer> toMap(List<CountryStat> input) {
        return input.stream().collect(toImmutableMap(CountryStat::getCountryName, CountryStat::getPopulation));
    }

    private List<CountryStat> sampleDbDataset() {
        return ImmutableList.<CountryStat>builder()
                .add(CountryStat.of("India", 1182105000))
                .add(CountryStat.of("United Kingdom", 62026962))
                .add(CountryStat.of("Chile", 17094270))
                .add(CountryStat.of("Mali", 15370000))
                .add(CountryStat.of("Greece", 11305118))
                .build();
    }

    private List<CountryStat> sampleRemoteDataset() {
        return ImmutableList.<CountryStat>builder()
                .add(CountryStat.of("India", 100))
                .add(CountryStat.of("United Kingdom", 100))
                .add(CountryStat.of("Chile", 100))
                .add(CountryStat.of("Mali", 15370000))
                .add(CountryStat.of("Greece", 11305118))
                .add(CountryStat.of("Vietnam", 86932500))
                .add(CountryStat.of("Germany", 81802257))
                .build();
    }

    private Map<String, Integer> sampleMergedDataset() {
        return ImmutableMap.<String, Integer>builder()
                .put("India", 1182105000)
                .put("United Kingdom", 62026962)
                .put("Chile", 17094270)
                .put("Mali", 15370000)
                .put("Greece", 11305118)
                .put("Vietnam", 86932500)
                .put("Germany", 81802257)
                .build();
    }
}