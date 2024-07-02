package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.*;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.*;
import org.c0nstexpr.fishology.*;

import java.util.*;

@Config(name = FishologyCoreKt.CORE_MOD_ID, wrapperName = "Config", defaultHook = true)
public class ConfigModel {
    @SectionHeader("general")
    public boolean enableAutoFish = true;

    @RangeConstraint(min = 0, max = 100, decimalPlaces = 5)
    public double caughtJudgeThreshold = 0.1;

    @RangeConstraint(min = 0, max = 60000)
    public int recastThreshold = 3000;

    @SectionHeader("hookNotify")
    @Nest
    public Notification hookNotify = new Notification();

    @SectionHeader("caughtNotify")
    @Nest
    public Notification caughtNotify = new Notification();

    public Set<FishingLoot> notifyLoots = Set.of();

    @SectionHeader("other")
    public Severity logLevel = Severity.Warn;

    public static class Notification {
        public NotifyLevel level = NotifyLevel.None;
        @PredicateConstraint("isFmtValid") public String msgFmt = "";

        public static boolean isFmtValid(String str) {
            try {
                return !String.format(str, "test").isBlank();
            } catch (Exception e) { return false; }
        }
    }
}
