package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import kotlin.collections.CollectionsKt;
import org.c0nstexpr.fishology.FishologyCoreKt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Config(name = FishologyCoreKt.coreModId, wrapperName = "Config", defaultHook = true)
public class ConfigModel {
    public boolean enableAutoFish = true;

    public Severity logLevel = Severity.Warn;

    public boolean enableChatOnHook = true;

    public Set<FishingLoot> enableChatOnCaught = new HashSet<>();
}