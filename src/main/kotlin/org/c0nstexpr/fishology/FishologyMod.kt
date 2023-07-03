package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.events.UseRodEvent

val config: FishologyConfig = FishologyConfig.createAndLoad()

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    var scope: Disposable? = null

    ClientLifecycleEvents.CLIENT_STARTED.register {
        scope = disposableScope {
            var action: FishologyAction? = null

            UseRodEvent.observable.subscribeScoped {
                if (action?.arg?.equals(it) == true) return@subscribeScoped

                action?.dispose()
                action = FishologyAction(it)
            }

            doOnDispose { action?.dispose() }
        }
    }

    ClientLifecycleEvents.CLIENT_STOPPING.register { scope?.dispose() }
}
