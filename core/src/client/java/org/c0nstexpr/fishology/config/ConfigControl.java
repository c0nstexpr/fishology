package org.c0nstexpr.fishology.config;

import static org.c0nstexpr.fishology.FishologyCoreKt.getCoreLogger;

import kotlin.Lazy;
import kotlin.LazyKt;

public class ConfigControl {
    public static final Lazy<org.c0nstexpr.fishology.config.Config> config =
            LazyKt.lazy(org.c0nstexpr.fishology.config.Config::createAndLoad);

    public static void init() {
        getCoreLogger().d("initialize config %s".formatted(config.getValue()));
    }
}
