package org.c0nstexpr.fishology.core

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.core.log.*


const val modId = "fishology-core"
const val modName = "Fishology Core"

internal val logger = LogBuilder().apply { tag = modId }.build()

fun init() = ClientLifecycleEvents.CLIENT_STARTED.register {
    val config = logger.mutableConfig

    config.addMCWriter(it.inGameHud.chatHud)

    logger.greeting()

    ClientLifecycleEvents.CLIENT_STOPPING.register {
        config.removeWriterWhere { writer -> writer is MCMessageWriter }
    }
}

fun MinecraftClient.chat(message: String) {
    networkHandler?.sendChatMessage(message)
    logger.i(message, tag = "${logger.tag}.chat")
}
