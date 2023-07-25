package org.c0nstexpr.fishology.core.log

import co.touchlab.kermit.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor

class MCMessageWriter(
    var hud: ChatHud = MinecraftClient.getInstance().inGameHud.chatHud,
    val levelColor: MutableMap<Severity, TextColor> = mutableMapOf()
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        hud.addMessage(
            Text.literal(AttributionFormatter.formatMessage(severity, Tag(tag), Message(message)))
                .setStyle(Style.EMPTY.withColor(levelColor[severity] ?: defaultColor(severity)))
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
