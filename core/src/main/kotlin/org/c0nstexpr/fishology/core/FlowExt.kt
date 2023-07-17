package org.c0nstexpr.fishology.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

fun <T> MutableSharedFlow<T>.blockEmit(
    value: T
) = runBlocking { emit(value) }
