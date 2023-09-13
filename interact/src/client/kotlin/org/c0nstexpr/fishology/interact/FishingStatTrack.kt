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
            if (jsonFile.exists())
                stat.putAll(Json.decodeFromString<Map<FishingLoot, UInt>>(jsonFile.readText()))
        } else {
            Files.createDirectories(dir)
        }
    }

    fun save() = dataDir.resolve(STAT_JSON).toFile().writeText(json.encodeToString(stat))

    fun clear() = stat.clear()

    fun printStat(): MutableText {
        val txt = Text.translatable("$modId.stat_title\n")
        statMap.forEach { (loot, count) ->
            txt.append(
                Text.empty()
                    .setStyle(Style.EMPTY.withColor(loot.color()))
                    .append(loot.translate())
                    .append(" $count\n")
            )
        }

        return txt
    }

    companion object {
        private const val STAT_JSON = "stats.json"

        private fun FishingLoot.color() =
            when (lootType) {
                FishingLootType.Treasure -> Formatting.GOLD
                FishingLootType.Fish -> Formatting.WHITE
                FishingLootType.Junk -> Formatting.GRAY
                else -> Formatting.DARK_GRAY
            }

        private val json = Json { prettyPrint = true }
    }
}