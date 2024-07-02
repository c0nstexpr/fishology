package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.log.getLogger

const val CORE_MOD_ID = "fishology-core"
const val CORE_MOD_NAME = "Fishology Core"

val CoreLogger = getLogger(CORE_MOD_NAME)

internal fun init() = ClientLifecycleEvents.CLIENT_STARTED.register { ConfigControl.init() }
