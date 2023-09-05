package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import java.util.Set;
import org.c0nstexpr.fishology.FishologyCoreKt;

@Config(name = FishologyCoreKt.coreModId, wrapperName = "Config", defaultHook = true)
public class ConfigModel {
    public boolean enableAutoFish = true;

    public double posError = 0.1;

    public Severity logLevel = Severity.Warn;

    public boolean enableChatOnHook = true;

    public NotifyLevel notifyOnCaught = NotifyLevel.None;

    public String caughtMsgFmt = "";

    public Set<FishingLoot> notifyLoots = Set.of();

    public Set<FishingLoot> discardLoots = Set.of();
}
