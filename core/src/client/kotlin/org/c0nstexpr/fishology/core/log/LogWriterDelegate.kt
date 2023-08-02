package org.c0nstexpr.fishology.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

class LogWriterDelegate(val logger: Logger) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) =
        logger.log(severity, tag, throwable, message)
}
