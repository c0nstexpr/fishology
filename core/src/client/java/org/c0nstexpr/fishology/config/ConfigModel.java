package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import org.c0nstexpr.fishology.FishologyCoreKt;

@Modmenu(modId = FishologyCoreKt.coreModId)
@Config(name = FishologyCoreKt.coreModId, wrapperName = "Config")
public class ConfigModel {
    public boolean enableAutoFish = true;
    public Severity logLevel = Severity.Warn;
    public boolean enableChatOnCaught = true;
}