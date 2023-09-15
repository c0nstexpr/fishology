package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.notNull
import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.config.Config
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.interact.AutoFishing
import org.c0nstexpr.fishology.interact.CaughtChat
import org.c0nstexpr.fishology.interact.CaughtFish
import org.c0nstexpr.fishology.interact.DiscardLoot
import org.c0nstexpr.fishology.interact.FishingStatTrack
import org.c0nstexpr.fishology.interact.HookChat
import org.c0nstexpr.fishology.interact.Rod
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.addMCWriter
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.removeWriterWhere
import org.c0nstexpr.fishology.utils.observe
import org.c0nstexpr.fishology.utils.propertyOption

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val config: Config by ConfigControl.config

    private val rod by lazy { Rod(client).apply { enable = true }.scope() }

    private val caughtFish by lazy { CaughtFish(rod).apply { enable = true }.scope() }

    private val autoFish by lazy { AutoFishing(rod, caughtFish.caught).scope() }

    private val hookChat by lazy { HookChat(client).apply { enable = true }.scope() }

    private val caughtChat by lazy {
        CaughtChat(client, caughtFish.caught.notNull()).apply { enable = true }.scope()
    }

    private val discardLoot by lazy {
        DiscardLoot(rod, caughtFish.caught.notNull()).apply { enable = true }.scope()
    }

    private val fishingStatTrack =
        FishingStatTrack(rod, caughtFish.caught.notNull()).apply { enable = true }.scope()

    init {
        logger.d<Fishology> { "Initializing Fishology module" }

        config.apply {
            observe(ConfigModel::logLevel) {
                logger.d<Fishology> { "set log level to $it" }
                logger.mutableConfig.minSeverity = it
            }

            observe(ConfigModel::enableAutoFish) { autoFish.enable = it }

            observe(ConfigModel::caughtJudgeThreshold) { caughtFish.judgeThreshold = it }

            propertyOption(ConfigModel::hookNotify).run {
                from(ConfigModel.Notification::level).observe { hookChat.notifyLevel = it }
                from(ConfigModel.Notification::msgFmt).observe { hookChat.fmt = it }
            }

            propertyOption(ConfigModel::caughtNotify).run {
                from(ConfigModel.Notification::level).observe { caughtChat.notifyLevel = it }
                from(ConfigModel.Notification::msgFmt).observe { caughtChat.fmt = it }
            }

            observe(ConfigModel::notifyLoots) { caughtChat.lootsFilter = it }

            observe(ConfigModel::discardLoots) { discardLoot.lootsFilter = it }
        }

        logger.mutableConfig.addMCWriter(client)

        doOnDispose { logger.mutableConfig.removeWriterWhere { w -> w is MCMessageWriter } }

        registerCommand()
    }

    private fun registerCommand() = ClientCommandRegistrationCallback.EVENT.register { d, _ ->
        d.register(
            literal(modId).then(
                literal("stat")
                    .then(
                        literal("print").executes {
                            client.msg(fishingStatTrack.printStat())
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        literal("clear").executes {
                            fishingStatTrack.clear()
                            Command.SINGLE_SUCCESS
                        },
                    ),
            ),
        )
    }
}
