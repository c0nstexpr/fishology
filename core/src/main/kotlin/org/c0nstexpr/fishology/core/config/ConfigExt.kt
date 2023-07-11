package org.c0nstexpr.fishology.core.config

import io.wispforest.owo.config.ConfigWrapper
import io.wispforest.owo.config.Option
import kotlin.reflect.KProperty1

fun <Param, T : Param> Option<T>.initObserve(block: (Param) -> Unit) {
    observe(block)
    block(value())
}

fun <Model, Wrapper : ConfigWrapper<Model>, Type> Wrapper.get(
    prop: KProperty1<Model, Type>,
    key: Option.Key = Option.Key(prop.name)
) = optionForKey<Type>(key)

fun <Param, Model, Wrapper : ConfigWrapper<Model>, Type : Param> Wrapper.initObserve(
    prop: KProperty1<Model, Type>,
    key: Option.Key = Option.Key(prop.name),
    block: (Param) -> Unit
) {
    get(prop, key)?.initObserve(block)
}
