package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.coroutinesinterop.asScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.Executors

val currentScheduler get() = Dispatchers.Unconfined
Thread.currentThread()
