package org.c0nstexpr.fishology.config;

import static org.c0nstexpr.fishology.FishologyCoreKt.*;

import io.wispforest.owo.config.ui.ConfigScreen;
import kotlin.Lazy;
import kotlin.LazyKt;
import net.minecraft.util.Identifier;

public class ConfigControl {
    public static final Lazy<org.c0nstexpr.fishology.config.Config> config =
            LazyKt.lazy(org.c0nstexpr.fishology.config.Config::createAndLoad);

    public static void init() {
        getCoreLogger()
                .d(
                        "initialize config %s".formatted(config.getValue()),
                        null,
                        ConfigControl.class.getName());
        ConfigScreen.registerProvider(
                coreModId,
                screen -> new Screen(new Identifier("owo:config"), config.getValue(), screen));
    }
}
