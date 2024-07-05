package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.switchMapMaybe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.dataDir
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.nio.file.Files
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.TimeSource

class FishingStatTrack(
    private val loot: Observable<FishingLoot>,
    private val rodItem: Observable<RodItem>
) : SwitchDisposable() {
    private val stat = sortedMapOf<FishingLoot, UInt>()

    private val lastStat = sortedMapOf<FishingLoot, UInt>()

    var fishingDuration = ZERO
        private set
        get() = if (field == ZERO) timeMark?.elapsedNow() ?: ZERO else field

    val statMap: Map<FishingLoot, UInt> get() = stat

    val lastStatMap: Map<FishingLoot, UInt> get() = lastStat

    private var timeMark: TimeSource.Monotonic.ValueTimeMark? = null

    init {
        FishingLoot.entries.forEach { stat[it] = 0u }
    }

    override fun onEnable(): Disposable {
        load()

        return disposableScope {
            loot.subscribeScoped {
                stat[it] = stat.getOrDefault(it, 0u) + 1u
                lastStat[it] = lastStat.getOrDefault(it, 0u) + 1u
            }

            rodItem.filter { it.isThrow }
                .switchMapMaybe scope@{
                    lastStat.clear()
                    fishingDuration = ZERO
                    timeMark = TimeSource.Monotonic.markNow()

                    logger.d<FishingStatTrack> {
                        "Restart fishing stats until next user input rod event"
                    }

                    rodItem.filter { !it.isThrow }.firstOrComplete().map {
                        logger.d<FishingStatTrack> {
                            "Detected user input rod event, stop fishing stats"
                        }
                        fishingDuration = timeMark?.elapsedNow() ?: ZERO
                    }
                }
                .subscribeScoped { }
        }
    }

    override fun onDisable() = save()

    private fun load() {
        val dir = dataDir

        if (Files.exists(dir)) {
            val jsonFile = dir.resolve(STAT_JSON).toFile()
            if (jsonFile.exists()) stat.putAll(
                Json.decodeFromString<Map<FishingLoot, UInt>>(
                    jsonFile.readText()
                )
            )
        } else Files.createDirectories(dir)
    }

    fun save() = dataDir.resolve(STAT_JSON).toFile().writeText(json.encodeToString(statMap))

    fun clear() = stat.clear()

    companion object {
        private const val STAT_JSON = "stats.json"

        private val json = Json { prettyPrint = true }
    }
}
