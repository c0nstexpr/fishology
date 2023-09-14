package org.c0nstexpr.fishology.interact

import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.config.NotifyLevel
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable

abstract class ChatInteraction(val client: MinecraftClient, val defaultTranslateKey: String) :
    SwitchDisposable() {
    var notifyLevel = NotifyLevel.None
    var fmt = ""
        set(value) {
            field = value.takeUnless { it.isBlank() }?.run {
                try {
                    format("test")
                } catch (e: Exception) {
                    client.msg("invalid msg format: ${e.message}")
                    null
                }
            } ?: defaultMsg.string
        }

    protected fun notify(txt: Text) = when (notifyLevel) {
        NotifyLevel.HUD -> client.msg(defaultMsg.append(txt))
        NotifyLevel.Chat -> client.chat(fmt.format(txt.string), logger)
        NotifyLevel.None -> {}
    }

    protected val defaultMsg: MutableText get() = Text.translatable("$modId.$defaultTranslateKey")
}
