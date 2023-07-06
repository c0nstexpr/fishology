@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent

class Fishology(val client: MinecraftClient) : Disposable {
    private var action: FishologyAction? = null
    private var subscription: Disposable? = null

//    val config: FishologyConfig = FishologyConfig.createAndLoad()
//
//    init {
//        config.initObserve(FishologyConfigModel::enabled) {
//            if (!it) {
//                dispose()
//                return@initObserve
//            }
//
//            if (subscription == null) {
//                subscription = UseRodEvent.observable.subscribe(onNext = ::onUseRod)
//            }
//        }
//    }

    private fun onUseRod(arg: UseRodEvent.Arg) {
        action.run {
            // no existed action found, start fishing
            if (this == null) {
                action = FishologyAction(client, arg)
                return@onUseRod
            }

            // filtered out auto-fishing reuse action
            if (reusingRod) return@onUseRod

            // interrupt by outside input
            dispose()
        }

        action = null
    }

    override val isDisposed: Boolean get() = subscription?.isDisposed ?: true

    override fun dispose() {
        subscription?.dispose()
        action?.dispose()

        subscription = null
        action = null
    }
}
