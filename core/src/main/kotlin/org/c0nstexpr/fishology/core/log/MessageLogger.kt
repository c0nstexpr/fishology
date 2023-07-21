package org.c0nstexpr.fishology.core.log

import io.github.oshai.kotlinlogging.*
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

class MessageLogger(
    private val logger: KLogger = KotlinLogging.logger {},
    client: MinecraftClient? = null
) : KLogger by logger {
    constructor(name: String, client: MinecraftClient? = null) : this(KotlinLogging.logger(name))

    val client: MinecraftClient = client ?: MinecraftClient.getInstance()

    override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
        logger.at(level, marker, block)

        if (isLoggingEnabledFor(level, marker))
            sendMessage(level, marker, block)
    }

    /*
    @Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Text text = getColoredText("length", State.ON_GROUND)

			.append(getColoredText("air", State.DEFAULT))
			.append(getColoredText("ground", State.ON_GROUND))
			.append(getColoredText("entity", State.IN_ENTITY))

			.append(getColoredText("small_water", State.IN_SMALL_WATER))
			.append(getColoredText("large_water", State.IN_OPEN_WATER))
			.append(getColoredText("not_exposed", State.NOT_EXPOSED))
			.append(getColoredText("in_rain", State.RAINED_ON))
			.append(getColoredText("fish_on_hook", State.HAS_FISH))

			.append(getColoredText("length_amber", State.NEAR_SNAP))
			.append(getColoredText("length_red", State.VERY_NEAR_SNAP))
			.append(getColoredText("snapped", State.SNAPPED))
			.append(getColoredText("despawn", State.NEAR_DESPAWN));

		FishingRulerClient.client.player.sendMessage(text);
        return 1;
	}

	private MutableText getColoredText(String key, State state) {
		return Text.translatable(FishingRulerClient.MODID+".help."+key).setStyle(FishingRulerClient.getStyle(state));
	}
     */

    private fun sendMessage(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
        val text = Text.EMPTY.

        KLoggingEventBuilder().apply(block).let {
            client.player?.sendMessage(it.message)
        }

        when (level) {
            Level.TRACE -> logger.trace(block)
            Level.DEBUG -> logger.debug(block)
            Level.INFO -> logger.info(block)
            Level.WARN -> logger.warn(block)
            Level.ERROR -> logger.error(block)
            else -> {}
        }
    }

    companion object {
        inline fun <reified T> create(client: MinecraftClient? = null) =
            MessageLogger(T::class.qualifiedName ?: "unknown", client)
    }
}
