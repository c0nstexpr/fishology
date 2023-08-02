package org.c0nstexpr.fishology.core.config;

import static org.c0nstexpr.fishology.core.FishologyCoreModKt.getLogger;

import kotlin.Lazy;
import kotlin.LazyKt;

public class ConfigControl {
    public static final Lazy<org.c0nstexpr.fishology.core.config.FishologyCoreConfig> config =
            LazyKt.lazy(org.c0nstexpr.fishology.core.config.FishologyCoreConfig::createAndLoad);

    public static void init() {
        getLogger().d("initialize config %s".formatted(config.getValue()));
    }
}
