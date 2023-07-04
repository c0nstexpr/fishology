package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.disposable.eventScopedDisposable

var fishology: Fishology? = null

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    eventScopedDisposable(
        {
            val f = Fishology()
            f.also { fishology = it }
        },
        ClientLifecycleEvents.CLIENT_STARTED to { block -> ClientLifecycleEvents.ClientStarted { block() } },
        ClientLifecycleEvents.CLIENT_STOPPING to { block -> ClientLifecycleEvents.ClientStopping { block() } }
    )
}
