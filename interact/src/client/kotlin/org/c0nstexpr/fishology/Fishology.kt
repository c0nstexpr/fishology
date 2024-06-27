package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.c0nstexpr.fishology.config.Config
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLootType
import org.c0nstexpr.fishology.interact.AutoFishing
import org.c0nstexpr.fishology.interact.CaughtChat
import org.c0nstexpr.fishology.interact.CaughtFish
import org.c0nstexpr.fishology.interact.FishingStatTrack
import org.c0nstexpr.fishology.interact.HookChat
import org.c0nstexpr.fishology.interact.LootFilter
import org.c0nstexpr.fishology.interact.Rod
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.addMCWriter
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.removeWriterWhere
import org.c0nstexpr.fishology.utils.observe
import org.c0nstexpr.fishology.utils.propertyOption
import kotlin.time.DurationUnit

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val config: Config by ConfigControl.config

    private val rod by lazy { Rod(client).apply { enable = true }.scope() }

    private val caughtFish by lazy { CaughtFish().apply { enable = true }.scope() }

    private val autoFish by lazy { AutoFishing(rod, lootFilter.loot).scope() }

    private val hookChat by lazy { HookChat(client).apply { enable = true }.scope() }

    private val caughtChat by lazy {
        CaughtChat(client, caughtFish.caught).apply { enable = true }.scope()
    }

    private val lootFilter by lazy {
        LootFilter(rod, caughtFish.caught).apply { enable = true }.scope()
    }

    private val fishingStatTrack by lazy {
        FishingStatTrack(lootFilter.loot, rod.itemObservable).apply { enable = true }.scope()
    }

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

            observe(ConfigModel::discardLoots) { lootFilter.lootSet = it }
        }

        logger.mutableConfig.addMCWriter(client)

        doOnDispose { logger.mutableConfig.removeWriterWhere { w -> w is MCMessageWriter } }

        registerCommand()
    }

    private fun onChangeCaughtJudgeThreshold(it: Double) {
        logger.d("Change caught judge threshold to $it")
        caughtFish.judgeThreshold = it
        lootFilter.judgeThreshold = it
    }

    private fun registerCommand() = ClientCommandRegistrationCallback.EVENT.register { d, _ ->
        val statCmd = fishingStatTrack.run {
            literal("stat").then(
                literal("print").then(
                    literal("last").executes {
                        client.msg(
                            lastStatMap.printStat(fishingDuration.toDouble(DurationUnit.MINUTES))
                        )
                        Command.SINGLE_SUCCESS
                    }
                )
                    .executes {
                        client.msg(statMap.printStat(0.0))
                        Command.SINGLE_SUCCESS
                    }
            )
                .then(
                    literal("clear").executes {
                        clear()
                        Command.SINGLE_SUCCESS
                    }
                )
        }

        d.register(literal(MOD_ID).then(statCmd))
    }

    private fun Map<FishingLoot, UInt>.printStat(duration: Double): MutableText {
        val txt =
            Text.literal("[$MOD_ID]")
                .appendTranslatable("$MOD_ID.stat_title")
                .append("\n")

        if (isEmpty()) return txt

        val typeMap = sortedMapOf<FishingLootType, UInt>()

        forEach { (loot, count) ->
            loot.apply {
                txt.appendStyled(color, loot.translate(), Text.of(" $count\n"))
                typeMap[lootType] = typeMap.getOrDefault(lootType, 0u) + count
            }
        }

        txt.append("--------------------------------\n")

        val totalCountFormat = Text.translatable("$MOD_ID.total").string
        val itemSpeedFormat = Text.translatable("$MOD_ID.item_speed").string

        typeMap.forEach { (lootType, count) ->
            val innerTxt = lootType.translate().append(String.format(totalCountFormat, count))

            if (duration > 0) innerTxt.append(
                String.format(itemSpeedFormat, count.toDouble() / duration)
            )

            txt.appendStyled(lootColors[lootType]!!, innerTxt.append("\n"))
        }

        return txt
    }

    companion object {
        private val lootColors =
            FishingLootType.entries.associateWith {
                Style.EMPTY.withColor(
                    when (it) {
                        FishingLootType.Treasure -> Formatting.GOLD
                        FishingLootType.Fish -> Formatting.AQUA
                        FishingLootType.Junk -> Formatting.WHITE
                    }
                )
                    .withBold(true)
                    .withItalic(true)
            }

        private val FishingLoot.color get() = lootColors[lootType]!!
    }
}
