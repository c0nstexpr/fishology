package org.c0nstexpr.fishology.config

import io.wispforest.owo.config.ConfigWrapper
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.ConfigScreen
import io.wispforest.owo.config.ui.OptionComponentFactory
import io.wispforest.owo.config.ui.OptionComponentFactory.Result
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Identifier
import java.util.function.Predicate
import kotlin.reflect.jvm.javaType

class Screen(modelId: Identifier?, config: ConfigWrapper<*>?, parent: Screen?) :
    ConfigScreen(modelId, config, parent) {
    init {
        extraFactories[Predicate(::optionPredicate)] = OptionComponentFactory { _, option ->
            val layout = FishingLootCollapsible(option)
            Result(layout, layout)
        }
    }

    private fun optionPredicate(option: Option<*>) =
        option.backingField().field.genericType == ConfigModel::chatOnCaught
            .returnType.javaType
}
