package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.map
import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.WorldEvents
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.config.FishingLootType
import org.c0nstexpr.fishology.interact.AutoFishing
import org.c0nstexpr.fishology.interact.CaughtChat
import org.c0nstexpr.fishology.interact.FishingStatTrack
import org.c0nstexpr.fishology.interact.HookChat
import org.c0nstexpr.fishology.interact.LootDetect
import org.c0nstexpr.fishology.interact.Rod
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.spell
import org.c0nstexpr.fishology.utils.observe
import org.c0nstexpr.fishology.utils.propertyOption
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    private val rod by lazy { Rod(client).apply { enable = true }.scope() }

    private val lootDetect by lazy { LootDetect().apply { enable = true }.scope() }

    private val autoFish by lazy { AutoFishing(rod, lootDetect.loot).scope() }

    private val hookChat by lazy { HookChat(client).apply { enable = true }.scope() }

    private val caughtChat by lazy {
        CaughtChat(client, lootDetect.loot).apply { enable = true }.scope()
    }
    private val fishingStatTrack by lazy {
        FishingStatTrack(
            lootDetect.loot.map { it.stack.getLoot() },
            rod.itemObservable
        ).apply { enable = true }.scope()
    }

    init {
        logger.d<Fishology> { "Initializing Fishology module" }

        config.apply {
            autoFish.enable = enableAutoFish()

            lootDetect.judgeThreshold = caughtJudgeThreshold()

            hookNotify.apply {
                hookChat.apply {
                    notifyLevel = level()
                    fmt = msgFmt()
                }
            }

            caughtChat.apply {
                caughtNotify.apply {
                    notifyLevel = level()
                    fmt = msgFmt()
                }

                lootsFilter = notifyLoots()
            }

            recastThreshold().let {
                autoFish.recastThreshold = if (it == 0) Duration.INFINITE
                else it.toDuration(DurationUnit.MILLISECONDS)
            }
        }

        registerCommand()
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

        val magicCmd = literal("magic").then(
            literal("spell").executes {
                client.msg(spell())

                val blockPos = client.player?.blockPos ?: return@executes Command.SINGLE_SUCCESS
                val world = client.world ?: return@executes Command.SINGLE_SUCCESS

                AreaEffectCloudEntity(
                    world,
                    blockPos.x.toDouble(),
                    blockPos.y.toDouble(),
                    blockPos.z.toDouble()
                ).run {
                    particleType = ParticleTypes.DRAGON_BREATH
                    dataTracker.set<Float>(AreaEffectCloudEntity.RADIUS, 3.0f)
                    duration = 100
                    addEffect(StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1))
                    world.syncWorldEvent(WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS, blockPos, 1)
                    world.playSoundAtBlockCenter(
                        blockPos,
                        SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
                        SoundCategory.HOSTILE,
                        1.0F,
                        random.nextFloat() * 0.1F + 0.9F,
                        false
                    )
                    world.spawnEntity(this)
                }

                Command.SINGLE_SUCCESS
            }
        )

        literal(MOD_ID).run {
            d.register(then(statCmd))
            d.register(then(magicCmd))
        }
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

        init {
            config.apply {
                observe(ConfigModel::enableAutoFish) { fishology?.autoFish?.enable = it }

                observe(ConfigModel::caughtJudgeThreshold) {
                    fishology?.lootDetect?.judgeThreshold = it
                }

                propertyOption(ConfigModel::hookNotify).run {
                    from(ConfigModel.Notification::level).observe {
                        fishology?.hookChat?.notifyLevel = it
                    }
                    from(ConfigModel.Notification::msgFmt).observe { fishology?.hookChat?.fmt = it }
                }

                propertyOption(ConfigModel::caughtNotify).run {
                    from(ConfigModel.Notification::level).observe {
                        fishology?.caughtChat?.notifyLevel = it
                    }
                    from(ConfigModel.Notification::msgFmt).observe {
                        fishology?.caughtChat?.fmt = it
                    }
                }

                observe(ConfigModel::notifyLoots) { fishology?.caughtChat?.lootsFilter = it }

                observe(ConfigModel::recastThreshold) {
                    fishology?.autoFish?.recastThreshold = if (it == 0) Duration.INFINITE
                    else it.toDuration(DurationUnit.MILLISECONDS)
                }
            }
        }
    }
}
