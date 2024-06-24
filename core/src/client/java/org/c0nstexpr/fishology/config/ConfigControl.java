package org.c0nstexpr.fishology.config;

import io.wispforest.owo.config.ui.*;
import kotlin.Lazy;
import kotlin.*;
import net.minecraft.util.*;

import static org.c0nstexpr.fishology.FishologyCoreKt.*;

public class ConfigControl {
    public static final Lazy<org.c0nstexpr.fishology.config.Config> config =
        LazyKt.lazy(org.c0nstexpr.fishology.config.Config::createAndLoad);

    public static void init() {
        getCoreLogger().d(
            "initialize config %s".formatted(config.getValue()),
            null,
            ConfigControl.class.getName());
        ConfigScreen.registerProvider(
            CORE_MOD_ID,
            screen -> new Screen(Identifier.of("owo:config"), config.getValue(), screen));
    }
}
