package org.c0nstexpr.fishology.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val modID = "fishology-core"
const val modName = "Fishology Core"

@JvmField
val logger: Logger = LoggerFactory.getLogger(modName)

@Suppress("unused")
fun init() = logger.info("Initializing $modName")
