package com.quickbase.db;

import com.quickbase.model.CountryStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.List;

/**
 * Provides data access functions for country stats database.
 */
@Repository
public class CountryStatsRepository {

    /** Query and calculate population of each country */
    private static final String COUNTRY_POPULATION_QUERY =
            "SELECT country.CountryName AS country, sum(city.population) AS population\n" +
            "FROM Country country\n" +
            "         INNER JOIN State state ON country.CountryId = state.CountryId\n" +
            "         INNER JOIN City city ON city.StateId = state.StateId\n" +
            "GROUP BY country.CountryId";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CountryStatsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Calculate and retrieve population by country
     * @return a {@link Mono} with a list containing {@link CountryStat} instances
     */
    public Mono<List<CountryStat>> getCountryPopulations() {
        return queryCountriesStat()
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel());
    }

    /**
     * Query the database for country population stat
     * @return a {@link Mono} containing the list of country stats, represented by a {@link CountryStat} instances
     * (country name -> population)
     */
    private Mono<List<CountryStat>> queryCountriesStat() {
        return Mono.fromCallable(() -> jdbcTemplate.query(COUNTRY_POPULATION_QUERY, resultMapper()));
    }

    /**
     * Map {@link java.sql.ResultSet} record to a country name and population
     * @return a {@link RowMapper} instance that converts {@link java.sql.ResultSet} row into {@link Tuple2}
     */
    private RowMapper<CountryStat> resultMapper() {
        return (rs, num) -> CountryStat.of(rs.getString("country"), rs.getInt("population"));
    }

}
