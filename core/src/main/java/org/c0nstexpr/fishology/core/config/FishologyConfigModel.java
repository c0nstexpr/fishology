package org.c0nstexpr.fishology.core.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "fishology-core")
@Config(name = "fishology-core", wrapperName = "FishologyConfig")
public class FishologyConfigModel {
    public boolean enabled = true;
}
