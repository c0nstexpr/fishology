package org.c0nstexpr.fishology.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting
import org.c0nstexpr.fishology.coloredText
import org.c0nstexpr.fishology.msg

class MCMessageWriter(
    var client: MinecraftClient,
    val levelFmt: MutableMap<Severity, Formatting> = mutableMapOf()
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) log(
            severity,
            "Exception occurred ${throwable.localizedMessage}",
            tag,
            null
        )

        client.msg(
            coloredText(
                levelFmt.getOrElse(severity) { severity.defaultFmt() },
                AttributionFormatter.formatMessage(severity, Tag(tag), Message(message))
            )
        )
    }

    companion object {
        private fun Severity.defaultFmt() = when (this) {
            Severity.Verbose -> Formatting.GRAY
            Severity.Debug -> Formatting.DARK_PURPLE
            Severity.Info -> Formatting.WHITE
            Severity.Warn -> Formatting.YELLOW
            Severity.Error -> Formatting.RED
            else -> Formatting.DARK_GRAY
        }
    }
}
