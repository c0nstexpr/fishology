package org.c0nstexpr.fishology.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.MutableLoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.ChatHud

fun Logger.greeting(who: String = tag) = i("Hello, this is $who")

object AttributionFormatter : MessageStringFormatter {
    override fun formatMessage(severity: Severity?, tag: Tag?, message: Message) =
        "[${severity?.run(::formatSeverity)}] [${tag?.run(::formatTag)}]: ${message.message}"
}

fun mutableLoggerConfigOf(c: LoggerConfig? = null) =
    object : MutableLoggerConfig {
        override var logWriterList: List<LogWriter> = c?.logWriterList ?: listOf()
        override var minSeverity: Severity = c?.minSeverity ?: Severity.Warn
    }

fun MutableLoggerConfig.addWriter(w: LogWriter): MutableLoggerConfig {
    logWriterList = logWriterList + w
    return this
}

fun MutableLoggerConfig.addWriter(l: Logger): MutableLoggerConfig {
    logWriterList = logWriterList + logWriterOf(l)

    return this
}

fun logWriterOf(l: Logger) = LogWriterDelegate(l)

fun MutableLoggerConfig.addMCWriter(
    h: ChatHud,
): MutableLoggerConfig {
    for (writer in logWriterList) if (writer is MCMessageWriter) {
        writer.hud = h
        return this
    }

    addWriter(MCMessageWriter(h))
    return this
}

inline fun MutableLoggerConfig.removeWriterWhere(p: (LogWriter) -> Boolean): MutableLoggerConfig {
    logWriterList = logWriterList.filter(p)
    return this
}

fun LogBuilder.forMC(
    modId: String,
    hud: ChatHud = MinecraftClient.getInstance().inGameHud.chatHud,
): LogBuilder {
    tag = modId
    config.addMCWriter(hud)
    return this
}
