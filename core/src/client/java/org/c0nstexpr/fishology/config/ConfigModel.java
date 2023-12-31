package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.PredicateConstraint;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;
import java.util.Set;
import org.c0nstexpr.fishology.FishologyCoreKt;

@Config(name = FishologyCoreKt.coreModId, wrapperName = "Config")
public class ConfigModel {
    @SectionHeader("general")
    public boolean enableAutoFish = true;

    @RangeConstraint(min = Double.MIN_VALUE, max = 100, decimalPlaces = 5)
    public double caughtJudgeThreshold = 0.1;

    @SectionHeader("hookNotify")
    @Nest
    public Notification hookNotify = new Notification();

    @SectionHeader("caughtNotify")
    @Nest
    public Notification caughtNotify = new Notification();

    public Set<FishingLoot> notifyLoots = Set.of();

    @SectionHeader("discardLoots")
    public Set<FishingLoot> discardLoots = Set.of();

    @SectionHeader("other")
    public Severity logLevel = Severity.Warn;

    public static class Notification {
        public NotifyLevel level = NotifyLevel.None;

        @PredicateConstraint("isFmtValid")
        public String msgFmt = "";

        public static boolean isFmtValid(String str) {
            try {
                FishologyCoreKt.getCoreLogger()
                        .d(
                                "test format: %s".formatted(String.format(str, "test")),
                                null,
                                Notification.class.getName());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
