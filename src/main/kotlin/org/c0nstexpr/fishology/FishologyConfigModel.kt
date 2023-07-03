package org.c0nstexpr.fishology

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu

@Modmenu(modId = "fishology")
@Config(name = "fishology-config", wrapperName = "FishologyConfig")
class FishologyConfigModel {
    var enabled = true
}
