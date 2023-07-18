package org.c0nstexpr.fishology.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient

val MinecraftClient.dispatcher get() = asCoroutineDispatcher()

val MinecraftClient.coroutineScope get() = CoroutineScope(dispatcher)
