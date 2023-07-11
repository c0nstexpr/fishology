package org.c0nstexpr.fishology.config

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu

@Modmenu(modId = "fishology", uiModelId = "fishology::config")
@Config(name = "fishology", wrapperName = "FishologyConfig")
class FishologyConfigModel {
    val enabled = true
}
