package org.c0nstexpr.fishology

import co.touchlab.kermit.Severity
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.mapNotNull
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import org.c0nstexpr.fishology.config.Config
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.interact.AutoFishing
import org.c0nstexpr.fishology.interact.CaughtChat
import org.c0nstexpr.fishology.interact.CaughtFish
import org.c0nstexpr.fishology.interact.HookChat
import org.c0nstexpr.fishology.interact.Rod
import org.c0nstexpr.fishology.interact.ThrowLoot
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.addMCWriter
import org.c0nstexpr.fishology.log.removeWriterWhere
import org.c0nstexpr.fishology.utils.initObserve

class Fishology(
    val client: MinecraftClient,
    var handler: ClientPlayNetworkHandler,
) : DisposableScope by DisposableScope() {
    val config: Config by ConfigControl.config

    private val playerUUID = handler.profile.id

    private val rod by lazy { Rod(client).apply { enable = true }.scope() }

    private val caughtFish by lazy { CaughtFish(playerUUID).apply { enable = true }.scope() }

    private val caughtChat by lazy {
        CaughtChat(client, caughtFish.caught).apply { enable = true }.scope()
    }

    private val autoFish by lazy { AutoFishing(rod::use, playerUUID, caughtFish.caught).scope() }

    private val hookChat by lazy { HookChat(client).scope() }

    private val throwLoot by lazy {
        ThrowLoot(
            caughtFish.run {
                caught.mapNotNull {
                    (bobber?.playerOwner as? ClientPlayerEntity)?.let { p -> Pair(p, it) }
                }
            },
        ).apply {
            enable = true
        }
            .scope()
    }

    init {
        logger.d("Initializing Fishology module")

        config.initObserve(ConfigModel::logLevel) { onChangeLogLevel(it) }
        config.initObserve(ConfigModel::enableAutoFish) { onEnableAutoFish(it) }
        config.initObserve(ConfigModel::enableChatOnHook) { onEnableChatOnHook(it) }
        config.initObserve(ConfigModel::chatOnCaught) { onChangeChatOnCaught(it) }
        config.initObserve(ConfigModel::discardLoots) { onExcludedLoots(it) }

        logger.mutableConfig.addMCWriter(client)
        doOnDispose { logger.mutableConfig.removeWriterWhere { w -> w is MCMessageWriter } }
    }

    private fun onExcludedLoots(it: Set<FishingLoot>) {
        logger.d("Change excluded loots")
        throwLoot.lootsFilter = it
    }

    private fun onEnableChatOnHook(it: Boolean) {
        logger.d("${if (it) "Enable" else "Disable"} chat on hook")
        hookChat.enable = it
    }

    private fun onChangeLogLevel(it: Severity) {
        logger.d("Change log level to $it")
        logger.mutableConfig.minSeverity = it
    }

    private fun onEnableAutoFish(it: Boolean) {
        logger.d("${if (it) "Enable" else "Disable"} auto fishing")
        autoFish.enable = it
    }

    private fun onChangeChatOnCaught(it: Set<FishingLoot>) {
        logger.d("Change chat on caught")
        caughtChat.lootsFilter = it
    }
}
