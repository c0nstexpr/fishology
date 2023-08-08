package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.base.CompleteCallback
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable

fun <T, U> U.onNextComplete(t: T) where U : ValueCallback<T>, U : CompleteCallback {
    onNext(t)
    onComplete()
}

fun CompositeDisposable.add(disposable1: Disposable, vararg disposable: Disposable): Boolean {
    var result = add(disposable1)
    disposable.forEach { if (!add(it) && result) result = false }
    return result
}
