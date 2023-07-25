package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.c0nstexpr.fishology.core.log.*
import org.c0nstexpr.fishology.core.modId

const val modId = "fishology"
const val modName = "Fishology"

internal val logger = LogBuilder().apply { tag = modId }.build()

var fishology: Fishology? = null
    private set

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    ClientLifecycleEvents.CLIENT_STARTED.register {
        logger.greeting()
        fishology = Fishology(it)

        ClientLifecycleEvents.CLIENT_STOPPING.register { fishology?.dispose() }
    }
}
