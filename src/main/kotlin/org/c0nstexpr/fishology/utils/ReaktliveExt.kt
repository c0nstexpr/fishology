package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.single.SingleCallbacks
import com.badoo.reaktive.single.single

fun <T> Observable<T>.asSingle() {
    single { emitter ->
        subscribe(
            object : SingleCallbacks<T> by emitter, ObservableObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    emitter.setDisposable(disposable)
                }

                override fun onComplete() {
                }

                override fun onNext(value: T) {
                    TODO("Not yet implemented")
                }
            }
        )
    }
}
