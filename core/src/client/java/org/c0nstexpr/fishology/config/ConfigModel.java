package org.c0nstexpr.fishology.config;

import co.touchlab.kermit.Severity;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import org.c0nstexpr.fishology.FishologyCoreKt;

@Modmenu(modId = FishologyCoreKt.coreModId)
@Config(name = FishologyCoreKt.coreModId, wrapperName = "Config", defaultHook = true)
public class ConfigModel {
    public boolean enableAutoFish = true;
    public Severity logLevel = Severity.Warn;
    public boolean enableChatOnCaught = true;

    public static class Loot {
        public boolean nameTag = false;
        public boolean bow = false;
        public boolean saddle = false;
        public boolean enchantedFishingRod = false;
        public boolean book = false;
        public boolean nautilusShell = false;

        public boolean cod = false;
        public boolean salmon = false;
        public boolean tropical = false;
        public boolean puffer = false;

        public boolean lilyPad = false;
        public boolean leatherBoots = false;
        public boolean leather = false;
        public boolean bone = false;
        public boolean potion = false;
        public boolean string = false;
        public boolean fishingRod = false;
        public boolean bowl = false;
        public boolean stick = false;
        public boolean inkSac = false;
        public boolean tripwireHook = false;
        public boolean rottenFlesh = false;
        public boolean bamboo = false;
    }
}
