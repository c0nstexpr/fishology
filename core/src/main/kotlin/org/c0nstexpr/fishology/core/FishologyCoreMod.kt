package org.c0nstexpr.fishology.core

import io.github.oshai.kotlinlogging.KLogger
import org.c0nstexpr.fishology.core.log.MessageLogger

const val modID = "fishology-core"
const val modName = "Fishology Core"

@JvmField
val logger : KLogger = MessageLogger(modName)

@Suppress("unused")
fun init() = logger.info { "Initializing $modName" }
