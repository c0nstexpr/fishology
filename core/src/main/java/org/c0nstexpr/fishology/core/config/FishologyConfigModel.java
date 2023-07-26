package org.c0nstexpr.fishology.core.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import org.c0nstexpr.fishology.core.FishologyCoreModKt;

@Modmenu(modId = FishologyCoreModKt.modId)
@Config(name = FishologyCoreModKt.modId, wrapperName = "FishologyConfig")
public class FishologyConfigModel {
    public boolean enabled = true;

    public Severity logLevel = Severity.Warn;
}
