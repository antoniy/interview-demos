package com.quickbase.service;

import com.google.common.collect.ImmutableList;
import com.quickbase.model.CountryStat;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Service which fetches country stat data from remote location.
 */
@Service
public class RemoteCountryStatService {

    /**
     * Calculate and retrieve population by country
     * @return a {@link Mono} with a list containing {@link CountryStat} instances
     */
    public Mono<List<CountryStat>> getCountryPopulations() {
        return fetchCountryStat()
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel());
    }

    /**
     * This method pretends to fetch country stats from a remote service
     * @return a {@link Mono} containing list of country stat instances
     */
    private Mono<List<CountryStat>> fetchCountryStat() {
        return Mono.just(ImmutableList.<CountryStat>builder()
                .add(CountryStat.of("India", 1182105000))
                .add(CountryStat.of("United Kingdom", 62026962))
                .add(CountryStat.of("Chile", 17094270))
                .add(CountryStat.of("Mali", 15370000))
                .add(CountryStat.of("Greece", 11305118))
                .add(CountryStat.of("Armenia", 3249482))
                .add(CountryStat.of("Slovenia", 2046976))
                .add(CountryStat.of("Saint Vincent and the Grenadines", 109284))
                .add(CountryStat.of("Bhutan", 695822))
                .add(CountryStat.of("Aruba (Netherlands)", 101484))
                .add(CountryStat.of("Maldives", 319738))
                .add(CountryStat.of("Mayotte (France)", 202000))
                .add(CountryStat.of("Vietnam", 86932500))
                .add(CountryStat.of("Germany", 81802257))
                .add(CountryStat.of("Botswana", 2029307))
                .add(CountryStat.of("Togo", 6191155))
                .add(CountryStat.of("Luxembourg", 502066))
                .add(CountryStat.of("U.S. Virgin Islands (US)", 106267))
                .add(CountryStat.of("Belarus", 9480178))
                .add(CountryStat.of("Myanmar", 59780000))
                .add(CountryStat.of("Mauritania", 3217383))
                .add(CountryStat.of("Malaysia", 28334135))
                .add(CountryStat.of("Dominican Republic", 9884371))
                .add(CountryStat.of("New Caledonia (France)", 248000))
                .add(CountryStat.of("Slovakia", 5424925))
                .add(CountryStat.of("Kyrgyzstan", 5418300))
                .add(CountryStat.of("Lithuania", 3329039))
                .add(CountryStat.of("United States of America", 309349689))
                .build());
    }

}
