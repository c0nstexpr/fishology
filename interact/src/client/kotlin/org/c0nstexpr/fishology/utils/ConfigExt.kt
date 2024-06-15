package org.c0nstexpr.fishology.utils

import io.wispforest.owo.config.ConfigWrapper
import io.wispforest.owo.config.Option
import kotlin.reflect.KProperty1

abstract class PropertyOption<Model, Type> {
    abstract val config: ConfigWrapper<Model>

    open val path: String get() = ""

    val option get() = config.optionForKey<Type>(Option.Key(path))!!

    fun <NextType> from(prop: KProperty1<Type, NextType>): PropertyOption<Model, NextType> {
        val c = { config }
        val p = { path }
        return object : PropertyOption<Model, NextType>() {
            override val config get() = c()
            override val path: String
                get() {
                    val baseP = p()
                    return if (baseP.isEmpty()) prop.name else "$baseP.${prop.name}"
                }
        }
    }

    fun observe(
        init: Boolean = true,
        block: (Type) -> Unit,
    ) = option.run {
        observe(block)
        if (init) block(value())
    }
}

fun <Model> ConfigWrapper<Model>.propertyOption() =
    object : PropertyOption<Model, Model>() {
        override val config: ConfigWrapper<Model> get() = this@propertyOption
    }

fun <Model, Type> ConfigWrapper<Model>.propertyOption(prop: KProperty1<Model, Type>) = propertyOption().from(prop)

fun <Model, Type> ConfigWrapper<Model>.observe(
    prop: KProperty1<Model, Type>,
    init: Boolean = true,
    block: (Type) -> Unit,
) = propertyOption(prop).observe(init, block)
