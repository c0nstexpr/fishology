package org.c0nstexpr.fishology.core.log

import org.apache.logging.log4j.kotlin.KotlinLogger

fun KotlinLogger.greeting(who: String = delegate.name) = info("Hello, this is $who")
