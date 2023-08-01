package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.base.CompleteCallback
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import java.util.function.Supplier

fun <T, U> U.onNextComplete(t: T) where U : ValueCallback<T>, U : CompleteCallback {
    onNext(t)
    onComplete()
}

fun CompositeDisposable.add(disposable1: Disposable, vararg disposable: Disposable): Boolean {
    var result = add(disposable1)
    disposable.forEach { if (!add(it) && result) result = false }
    return result
}

fun CompositeDisposable.addScope(disposable: Disposable): CompositeDisposable {
    val outer = this
    val disposables = CompositeDisposable()

    add(disposables)
    disposables.add(disposable, Disposable { outer.remove(disposables, true) })

    return disposables
}

data class DisposableWrapper(val d: Disposable)

fun CompositeDisposable.addScope(func: (Supplier<Disposable>) -> Disposable): CompositeDisposable {
    lateinit var d: Disposable
    d = addScope(func { d })
    return this
}
