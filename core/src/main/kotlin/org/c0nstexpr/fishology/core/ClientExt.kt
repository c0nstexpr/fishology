package org.c0nstexpr.fishology.core

import co.touchlab.kermit.Logger
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

fun MinecraftClient.chat(message: String, loggerIn: Logger) {
    networkHandler?.sendChatMessage(message)
    loggerIn.i(message, tag = "${loggerIn.tag}.chat")
}
