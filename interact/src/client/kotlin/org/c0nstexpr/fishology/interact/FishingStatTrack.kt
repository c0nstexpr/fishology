package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.entity.ItemEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.c0nstexpr.fishology.appendStyled
import org.c0nstexpr.fishology.appendTranslatable
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.config.FishingLootType
import org.c0nstexpr.fishology.dataDir
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.nio.file.Files

class FishingStatTrack(val rod: Rod, val caughtItem: Observable<ItemEntity>) : SwitchDisposable() {
    private val stat = sortedMapOf<FishingLoot, UInt>()

    val statMap: Map<FishingLoot, UInt> get() = stat

    init {
        FishingLoot.entries.forEach { stat[it] = 0u }
    }

    override fun onEnable(): Disposable {
        load()

        return caughtItem.subscribe {
            val loot = it.stack.getLoot()
            stat[loot] = stat.getOrDefault(loot, 0u) + 1u
        }
    }

    override fun onDisable() = save()

    private fun load() {
        val dir = dataDir

        if (Files.exists(dir)) {
            val jsonFile = dir.resolve(STAT_JSON).toFile()
            if (jsonFile.exists()) {
                stat.putAll(Json.decodeFromString<Map<FishingLoot, UInt>>(jsonFile.readText()))
            }
        } else {
            Files.createDirectories(dir)
        }
    }

    fun save() = dataDir.resolve(STAT_JSON).toFile().writeText(json.encodeToString(statMap))

    fun clear() = stat.clear()

    fun printStat(): MutableText {
        val txt = Text.literal("[$modId]")
            .appendTranslatable("$modId.stat_title")
            .append("\n")

        statMap.forEach { (loot, count) ->
            txt.appendStyled(loot.color, loot.translate(), Text.of(" $count\n"))
        }

        return txt
    }

    companion object {
        private const val STAT_JSON = "stats.json"

        private val fishingLootColors = FishingLootType.entries.associateWith {
            Style.EMPTY.withColor(
                when (it) {
                    FishingLootType.Treasure -> Formatting.GOLD
                    FishingLootType.Fish -> Formatting.AQUA
                    FishingLootType.Junk -> Formatting.WHITE
                },
            )
                .withBold(true)
                .withItalic(true)
        }

        private val FishingLoot.color get() = fishingLootColors[lootType]!!

        private val json = Json { prettyPrint = true }
    }
}
