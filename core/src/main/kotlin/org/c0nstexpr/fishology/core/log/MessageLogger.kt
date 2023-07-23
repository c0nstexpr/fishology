package org.c0nstexpr.fishology.core.log

import io.github.oshai.kotlinlogging.*
import io.github.oshai.kotlinlogging.slf4j.toSlf4j
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import org.slf4j.event.KeyValuePair
import org.slf4j.event.LoggingEvent
import org.slf4j.event.SubstituteLoggingEvent

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
        KLoggingEventBuilder().run {
            block()

            val str = StringBuilder("[$level]${marker?.run { "[${getName()}]" }}")

            client.player?.sendMessage(it.message)
        }
    }

    private fun mergeMarkersAndKeyValuePairs(event: LoggingEvent, msg: String): String {
        var sb: StringBuilder? = null
        if (event.markers != null) {
            sb = StringBuilder()
            for (marker in event.markers) {
                sb.append(marker)
                sb.append(' ')
            }
        }
        if (event.keyValuePairs != null) {
            if (sb == null) {
                sb = StringBuilder()
            }
            for (kvp in event.keyValuePairs) {
                sb.append(kvp.key)
                sb.append('=')
                sb.append(kvp.value)
                sb.append(' ')
            }
        }
        return if (sb != null) {
            sb.append(msg)
            sb.toString()
        } else {
            msg
        }
    }

    private fun getColoredText(str: String, level: Level) {
        Text.translatable(str).setStyle(Style.EMPTY.withBold())
    }

    companion object {
        inline fun <reified T> create(client: MinecraftClient? = null) =
            MessageLogger(T::class.qualifiedName ?: "unknown", client)

        private fun levelColor(level: Level) = when (level) {
            Level.TRACE -> TextColor.fromRgb(0x795548)
            Level.DEBUG -> TextColor.fromRgb(0x9C27B0)
            Level.INFO -> TextColor.fromRgb(0xFFFFFF)
            Level.WARN -> TextColor.fromRgb(0xFFEB3B)
            Level.ERROR -> TextColor.fromRgb(0xD32F2F)
            else -> TextColor.fromRgb(0x2196F3)
        }
    }
}
