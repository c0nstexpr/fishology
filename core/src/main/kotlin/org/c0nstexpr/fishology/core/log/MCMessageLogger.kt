package org.c0nstexpr.fishology.core.log

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.spi.ExtendedLogger

class MCMessageLogger(
    val client: MinecraftClient,
    val levelColor: MutableMap<Level, TextColor>,
    delegate: ExtendedLogger
) : KotlinLogger(delegate) {
    override fun append(event: LogEvent) {
        val color = levelColor.getOrDefault(event.level, defaultColor(event.level))

        client.player?.sendMessage(
            Text.literal(event.message.formattedMessage)
                .setStyle(Style.EMPTY.withColor(color))
        )
    }

    companion object {
        private fun defaultColor(level: Level) = when (level) {
            Level.TRACE, Level.ALL -> TextColor.fromRgb(0x795548)
            Level.DEBUG -> TextColor.fromRgb(0x9C27B0)
            Level.INFO -> TextColor.fromRgb(0xFFFFFF)
            Level.WARN -> TextColor.fromRgb(0xFFEB3B)
            Level.ERROR, Level.FATAL -> TextColor.fromRgb(0xD32F2F)
            else -> TextColor.fromRgb(0x2196F3)
        }
    }


    class Builder<B : Builder<B>> : AbstractAppender.Builder<B>(),
        org.apache.logging.log4j.core.util.Builder<MCMessageLogger> {
        @PluginBuilderAttribute
        var client: MinecraftClient = MinecraftClient.getInstance()

        @PluginBuilderAttribute
        var ignoreExceptions: Boolean = false

        @PluginBuilderAttribute
        val levelColor = mutableMapOf<Level, TextColor>()

        override fun build(): MCMessageLogger {
            if (!this.isValid) throw IllegalArgumentException()

            return MCMessageLogger(
                client,
                levelColor,
                name,
                filter,
                layout,
                ignoreExceptions,
                propertyArray
            )
        }

        override fun getErrorPrefix(): String = super<AbstractAppender.Builder>.getErrorPrefix()
    }
}
