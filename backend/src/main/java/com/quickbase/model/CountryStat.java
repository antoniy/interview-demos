package com.quickbase.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class CountryStat {
    String countryName;
    int population;
}
