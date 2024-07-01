package org.c0nstexpr.fishology

import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.scheduler.overrideSchedulers
import kotlinx.coroutines.asCoroutineDispatcher
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.util.Util
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.log.LogBuilder
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.addWriter
import org.c0nstexpr.fishology.log.greeting
import org.c0nstexpr.fishology.log.removeWriterWhere

const val CORE_MOD_ID = "fishology-core"
const val CORE_MOD_NAME = "Fishology Core"

internal val CoreLogger = LogBuilder().apply {
    config.addWriter()
    tag = CORE_MOD_ID
}.build()

internal fun init() = ClientLifecycleEvents.CLIENT_STARTED.register {
    val loggerConfig = CoreLogger.mutableConfig
    CoreLogger.greeting()
    ConfigControl.init()

    overrideSchedulers(
        { it.asCoroutineDispatcher().asScheduler() },
        { Util.getMainWorkerExecutor().asCoroutineDispatcher().asScheduler() },
        { Util.getIoWorkerExecutor().asCoroutineDispatcher().asScheduler() }
    )

    ClientLifecycleEvents.CLIENT_STOPPING.register {
        loggerConfig.removeWriterWhere { writer -> writer is MCMessageWriter }
    }
}
