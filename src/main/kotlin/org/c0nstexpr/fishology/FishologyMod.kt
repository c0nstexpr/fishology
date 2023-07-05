package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

var fishology: Fishology? = null
    private set

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    ClientLifecycleEvents.CLIENT_STARTED.register { fishology = Fishology(it) }
    ClientLifecycleEvents.CLIENT_STOPPING.register {
        fishology?.dispose()
        fishology = null
    }
}
