package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.Disposable

fun Disposed() = Disposable().apply { dispose() }
