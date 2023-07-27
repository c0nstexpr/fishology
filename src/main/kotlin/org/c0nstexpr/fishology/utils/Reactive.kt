package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.base.CompleteCallback
import com.badoo.reaktive.base.ValueCallback

fun <T, U> U.onNextComplete(t: T) where U : ValueCallback<T>, U : CompleteCallback {
    onNext(t)
    onComplete()
}
