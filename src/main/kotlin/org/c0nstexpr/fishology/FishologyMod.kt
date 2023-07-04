package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.events.UseRodEvent

var fishology: Fishology? = null

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    var scope: Disposable? = null

    ClientLifecycleEvents.CLIENT_STARTED.register {

        fishology.
    }

    ClientLifecycleEvents.CLIENT_STOPPING.register { scope?.dispose() }
}

fun onUseRod(action: FishologyAction?, it: UseRodEvent.Arg)
{
    if (!config.enabled() || action?.arg?.equals(it) == true) return

    action?.dispose()
    FishologyAction(it)
}
