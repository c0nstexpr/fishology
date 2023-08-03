package org.c0nstexpr.fishology.core.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import org.c0nstexpr.fishology.core.FishologyCoreKt;

@Modmenu(modId = FishologyCoreKt.modId)
@Config(name = FishologyCoreKt.modId, wrapperName = "FishologyCoreConfig")
public class FishologyCoreConfigModel {
    public boolean enableAutoFish = true;
    public Severity logLevel = Severity.Warn;
    public boolean enableChatOnCaught = true;
}
