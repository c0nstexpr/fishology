package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.map
import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.ClickEvent
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
import org.c0nstexpr.fishology.interact.BobberStatus
import org.c0nstexpr.fishology.interact.CaughtChat
import org.c0nstexpr.fishology.interact.FishingStatTrack
import org.c0nstexpr.fishology.interact.HookChat
import org.c0nstexpr.fishology.interact.LootDetect
import org.c0nstexpr.fishology.interact.Rod
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.spell
import org.c0nstexpr.fishology.utils.observe
import org.c0nstexpr.fishology.utils.propertyOption
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    private val rod = Rod(client).apply { enable = true }.scope()

    private val lootDetect = LootDetect().apply { enable = true }.scope()

    private val hookChat = HookChat(client).apply { enable = true }.scope()

    private val bobberStatus = BobberStatus(client).apply { enable = true }.scope()

    private val autoFish = AutoFishing(rod, lootDetect.loot).scope()

    private val caughtChat = CaughtChat(client, lootDetect.loot).apply { enable = true }.scope()

    private val fishingStatTrack = FishingStatTrack(
        lootDetect.loot.map { it.stack.getLoot() },
        rod.itemObservable
    ).apply { enable = true }.scope()

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
    }

    private fun Map<FishingLoot, UInt>.printStat(duration: Duration): MutableText {
        val durationUnit = DurationUnit.MINUTES
        val split = "--------------------------------\n"
        val txt = Text.empty()
            .append(split)
            .appendStyled(Style.EMPTY.withBold(true), Text.translatable("$MOD_ID.stat_title"))

        if (duration > Duration.ZERO) txt.append("\n").appendStyled(
            Style.EMPTY.withBold(true).withItalic(true),
            String.format(
                Text.translatable("$MOD_ID.stat_duration").string,
                String.format("%.2f", duration.toDouble(durationUnit))
            )
        )

        txt.append("\n")

        if (isEmpty()) return txt

        val typeMap = sortedMapOf<FishingLootType, UInt>()
        val totalCountFormat = Text.translatable("$MOD_ID.total").string
        val itemSpeedFormat = Text.translatable("$MOD_ID.item_speed").string
        val minutes = duration.toDouble(durationUnit)

        txt.append(split)

        forEach { (loot, count) ->
            loot.apply {
                txt.appendStyled(lootColors[lootType]!!, loot.translate()).append(" $count\n")
                typeMap[lootType] = typeMap.getOrDefault(lootType, 0u) + count
            }
        }

        txt.append(split)

        typeMap.forEach { (lootType, count) ->
            txt.appendStyled(lootColors[lootType]!!, lootType.translate())
                .append(String.format(totalCountFormat, count))

            if (minutes > 0) txt.append(
                String.format(itemSpeedFormat, String.format("%.2f", count.toDouble() / minutes))
            )

            txt.append("\n")
        }

        return txt.append(split)
    }

    companion object {
        private val lootColors = FishingLootType.entries.associateWith {
            Style.EMPTY.withColor(
                when (it) {
                    FishingLootType.Treasure -> Formatting.GOLD
                    FishingLootType.Fish -> Formatting.AQUA
                    FishingLootType.Junk -> Formatting.WHITE
                }
            )
                .withBold(true)
                .withItalic(true)
                .withUnderline(true)
        }

        init {
            observeConfig()
            registerCommand()
        }

        private fun observeConfig() {
            config.apply {
                observe(ConfigModel::enableAutoFish) { fishology?.autoFish?.enable = it }

                observe(ConfigModel::caughtJudgeThreshold) {
                    fishology?.lootDetect?.judgeThreshold = it
                }

                propertyOption(ConfigModel::hookNotify).apply {
                    from(ConfigModel.Notification::level).observe {
                        fishology?.hookChat?.notifyLevel = it
                    }
                    from(ConfigModel.Notification::msgFmt).observe { fishology?.hookChat?.fmt = it }
                }

                propertyOption(ConfigModel::caughtNotify).apply {
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

        private fun registerCommand() = ClientCommandRegistrationCallback.EVENT.register { d, _ ->
            val statCmd = literal("stat").then(
                literal("print").then(
                    literal("last").executes {
                        fishology?.apply {
                            fishingStatTrack.apply {
                                client.msg(lastStatMap.printStat(fishingDuration))
                            }
                        }
                        Command.SINGLE_SUCCESS
                    }
                )
                    .executes {
                        fishology?.apply {
                            fishingStatTrack.apply {
                                client.msg(statMap.printStat(Duration.ZERO))
                            }
                        }
                        Command.SINGLE_SUCCESS
                    }
            )
                .then(
                    literal("clear").executes {
                        fishology?.fishingStatTrack?.clear()
                        Command.SINGLE_SUCCESS
                    }
                )

            val magicCmd = literal("magic").then(
                literal("spell").executes {
                    spellMagic()
                    Command.SINGLE_SUCCESS
                }
            )

            literal(MOD_ID).apply {
                d.register(then(statCmd))
                d.register(then(magicCmd))
            }
        }

        private val goldenRate = (sqrt(5.0) - 1) / 2

        private fun randomObfuscate(it: Char) = styledText(
            Style.EMPTY.withColor(Formatting.DARK_PURPLE).withBold(true).run {
                if (Random.nextFloat() >= goldenRate) withObfuscated(true) else this
            },
            "$it"
        )

        private fun spellMagic() = fishology?.client?.apply {
            msg(
                Text.empty().setStyle(
                    Style.EMPTY.withClickEvent(
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/$MOD_ID magic spell")
                    )
                ).apply { spell().map(::randomObfuscate).forEach(::append) }
            )

            val blockPos = player?.blockPos ?: return@apply
            val world = world ?: return@apply

            AreaEffectCloudEntity(
                world,
                blockPos.x.toDouble(),
                blockPos.y.toDouble(),
                blockPos.z.toDouble()
            ).apply {
                particleType = ParticleTypes.DRAGON_BREATH
                dataTracker.set(AreaEffectCloudEntity.RADIUS, 16.0f)
                duration = 200
                radiusGrowth = 0.0f
                world.syncWorldEvent(
                    WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS,
                    blockPos,
                    1
                )
                world.playSoundAtBlockCenter(
                    blockPos,
                    SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
                    SoundCategory.HOSTILE,
                    1.0F,
                    0.0f,
                    false
                )
                world.spawnEntity(this)
            }
        }
    }
}
