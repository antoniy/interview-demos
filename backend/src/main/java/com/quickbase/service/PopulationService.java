package com.quickbase.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.quickbase.db.CountryStatsRepository;
import com.quickbase.model.CountryStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * A service for obtaining country population statistical data.
 */
@Service
public class PopulationService {

    private final CountryStatsRepository dbCountryStatService;
    private final RemoteCountryStatService remoteCountryStatService;

    @Autowired
    public PopulationService(CountryStatsRepository dbCountryStatService,
                             RemoteCountryStatService remoteCountryStatService) {
        this.dbCountryStatService = dbCountryStatService;
        this.remoteCountryStatService = remoteCountryStatService;
    }

    /**
     * Calculate country population stats using multiple sources:
     * <ul>
     *     <li>local SQLite database</li>
     *     <li>remote webservice call (mocked)</li>
     * </ul>
     * Local database takes precedence over remote webservice call for the same country records.
     * @return a {@link Mono} containing the country population stat mapping
     */
    public Mono<Map<String, Integer>> calculatePopulationByCountry() {
        final var dbStats = dbCountryStatService.getCountryPopulations().transform(this::transformToMap);
        final var remoteStats = remoteCountryStatService.getCountryPopulations().transform(this::transformToMap);
        return Mono.zip(dbStats, remoteStats).map(TupleUtils.function(this::mergeMapsWithPrecedence));
    }

    private Mono<Map<String, Integer>> transformToMap(Mono<List<CountryStat>> input) {
        return input.map(l -> l.stream().collect(toImmutableMap(CountryStat::getCountryName, CountryStat::getPopulation)));
    }

    /**
     * Merge two maps into one while treating elements from one map as having high precedence.
     * This is used to resolve duplicate keys - use high precedence map entries in such cases.
     * @param primary high precedence {@link Map}
     * @param secondary secondary {@link Map}
     * @param <K> Map key type parameter
     * @param <V> Map value type parameter
     * @return merged immutable {@link Map} containing all elements from <i>high precedence</i> map with all new entries
     * from <i>secondary</i> map
     */
    private @NonNull <K, V> Map<K, V> mergeMapsWithPrecedence(@NonNull Map<K, V> primary, @NonNull Map<K, V> secondary) {
        // Merge high precedence map with new entries from the secondary map
        return ImmutableMap.<K, V>builder()
                .putAll(primary)
                // Remove all entries from "secondary" dataset that are already present in high precedence dataset
                .putAll(Maps.filterKeys(secondary, k -> !primary.containsKey(k)))
                .build();
    }

}
