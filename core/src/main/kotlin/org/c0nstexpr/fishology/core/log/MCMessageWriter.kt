package org.c0nstexpr.fishology.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor

class MCMessageWriter(
    var client: MinecraftClient,
    val levelColor: MutableMap<Severity, TextColor> = mutableMapOf(),
) : LogWriter() {
    val hud: ChatHud get() = client.inGameHud.chatHud

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        hud.addMessage(
            Text.literal(
                AttributionFormatter.formatMessage(severity, Tag(tag), Message(message)),
            )
                .setStyle(
                    Style.EMPTY.withColor(
                        levelColor[severity]
                            ?: defaultColor(severity),
                    ),
                ),
        )
    }

    companion object {
        private fun defaultColor(level: Severity) = when (level) {
            Severity.Verbose -> TextColor.fromRgb(0x795548)
            Severity.Debug -> TextColor.fromRgb(0x9C27B0)
            Severity.Info -> TextColor.fromRgb(0xFFFFFF)
            Severity.Warn -> TextColor.fromRgb(0xFFEB3B)
            Severity.Error, Severity.Assert -> TextColor.fromRgb(0xD32F2F)
            else -> TextColor.fromRgb(0x2196F3)
        }
    }
}
