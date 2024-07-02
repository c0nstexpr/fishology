package org.c0nstexpr.fishology.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.LoggerConfig

inline fun <reified T> Logger.v(throwable: Throwable? = null, message: () -> String) =
    v(throwable, T::class.simpleName!!, message)

inline fun <reified T> Logger.d(throwable: Throwable? = null, message: () -> String) =
    d(throwable, T::class.simpleName!!, message)

inline fun <reified T> Logger.i(throwable: Throwable? = null, message: () -> String) =
    i(throwable, T::class.simpleName!!, message)

inline fun <reified T> Logger.w(throwable: Throwable? = null, message: () -> String) =
    w(throwable, T::class.simpleName!!, message)

inline fun <reified T> Logger.e(throwable: Throwable? = null, message: () -> String) =
    e(throwable, T::class.simpleName!!, message)

inline fun <reified T> Logger.a(throwable: Throwable? = null, message: () -> String) =
    a(throwable, T::class.simpleName!!, message)

private val severityToLvlArray = Array<Level>(Severity.entries.size) {
    Level.OFF
}.also {
    it[Severity.Verbose.ordinal] = Level.TRACE
    it[Severity.Debug.ordinal] = Level.DEBUG
    it[Severity.Info.ordinal] = Level.INFO
    it[Severity.Warn.ordinal] = Level.WARN
    it[Severity.Error.ordinal] = Level.ERROR
    it[Severity.Assert.ordinal] = Level.FATAL
}

private val Severity.Lvl get() = severityToLvlArray[ordinal]

fun getLogger(modName: String): Logger {
    val writer = object : LogWriter() {
        val logger: org.apache.logging.log4j.Logger = LogManager.getLogger(modName)

        init {
            (LogManager.getContext(false) as LoggerContext).apply {
                LoggerConfig(modName, Level.ALL, false).apply {
                    configuration.addLogger(modName, this)
                    var parent: LoggerConfig? = parent
                    while (parent != null) {
                        parent.appenders.values.forEach { addAppender(it, Level.ALL, null) }

                        if (parent.isAdditive) parent = parent.parent
                        else break
                    }
                }

                updateLoggers()
            }
        }

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) =
            logger.log(severity.Lvl, "$tag: $message", throwable)
    }

    return Logger(mutableLoggerConfigInit(writer), modName)
}
