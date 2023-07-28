package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.core.chat
import org.c0nstexpr.fishology.core.config.FishologyConfig
import org.c0nstexpr.fishology.core.config.FishologyConfigModel
import org.c0nstexpr.fishology.interact.AutoFishingInteraction
import org.c0nstexpr.fishology.interact.BobberInteraction
import org.c0nstexpr.fishology.interact.RodInteraction
import org.c0nstexpr.fishology.utils.initObserve

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    var rod: RodInteraction? = null
        private set
    var hooked: BobberInteraction? = null
        private set(value) {
            field?.dispose()
            field = value
        }

    var fishing: AutoFishingInteraction? = null
        private set

    val config: FishologyConfig = FishologyConfig.createAndLoad()

    var player: Entity? = client.player
        private set

    init {
        logger.d("Initializing main controller")

        hooked.entity.notNull().subscribeScoped { fishing?.hooked = it }

        config.initObserve(FishologyConfigModel::enabled) { onEnable(it) }
        val chatDisposable = SerialDisposable()

        config.initObserve(FishologyConfigModel::enableChatOnCaught) { onEnableChatOnCaught(it) }
        config.initObserve(FishologyConfigModel::logLevel) { logger.mutableConfig.minSeverity = it }

        hooked.scope()
        rod.scope()

        doOnDispose {
            fishing?.dispose()
        }
    }

    val chatDisposable = SerialDisposable()
    private fun onEnableChatOnCaught(it: Boolean) {
        logger.d("${if (it) "Enable" else "Disable"} auto fishing")

        if (it) {
            val d = run {
                var h = hooked

                if (h == null) {
                    h = BobberInteraction(client)
                    hooked = h
                }

                return@run h
            }.entity.subscribe {
                client.chat(
                        Text.translatable("${org.c0nstexpr.fishology.core.modId}.caught_on_chat")
                                .append(it.displayName)
                                .string,
                        logger)
            }
        }

    }

    private fun onEnable(it: Boolean) {
        if (!it) {
            logger.d("Disable auto fishing")
            fishing?.dispose()
            fishing = null
            rod?.dispose()
            rod = null

            return
        }

        logger.d("Enable auto fishing")

        if (rod == null) rod = RodInteraction(client)
        if (fishing == null) fishing = AutoFishingInteraction(rod!!::use)
    }
}
