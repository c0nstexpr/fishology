package org.c0nstexpr.fishology

import co.touchlab.kermit.Logger
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

fun MinecraftClient.chat(message: String, loggerIn: Logger) {
    networkHandler?.sendChatMessage(message)
    loggerIn.i(message, tag = "${loggerIn.tag}.chat")
}

fun MinecraftClient.msg(txt: Text) = inGameHud.chatHud.addMessage(txt)

fun MinecraftClient.msg(str: String) = msg(Text.of(str))
