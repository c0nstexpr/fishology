package org.c0nstexpr.fishology.core.log

import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import kotlin.reflect.KClass

class LogBuilder {
    var config = mutableLoggerConfigOf(loggerConfigInit())
    var tag = ""

    fun withClass(clazz: KClass<*>) = apply { tag = clazz.simpleName ?: "" }

    inline fun <reified T> withType() = withClass(T::class)

    fun build() = Logger(config, tag)
}
