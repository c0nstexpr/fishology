package org.c0nstexpr.fishology.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient

suspend fun <R> MinecraftClient.execute(block: suspend CoroutineScope.() -> R) =
    withContext(asCoroutineDispatcher()) { block() }
