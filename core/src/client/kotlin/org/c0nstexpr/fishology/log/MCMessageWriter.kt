package org.c0nstexpr.fishology.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import org.c0nstexpr.fishology.msg

class MCMessageWriter(
    var client: MinecraftClient,
    val levelColor: MutableMap<Severity, TextColor> = mutableMapOf(),
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) =
        client.msg(
            Text.literal(AttributionFormatter.formatMessage(severity, Tag(tag), Message(message)))
                .setStyle(
                    Style.EMPTY.withColor(
                        levelColor.getOrElse(severity) {
                            TextColor.fromFormatting(defaultColor(severity))
                        }
                    )
                )
        )

    companion object {
        private fun defaultColor(level: Severity) = when (level) {
            Severity.Verbose -> Formatting.GRAY
            Severity.Debug -> Formatting.DARK_PURPLE
            Severity.Info -> Formatting.WHITE
            Severity.Warn -> Formatting.YELLOW
            Severity.Error -> Formatting.RED
            else -> Formatting.DARK_GRAY
        }
    }
}
