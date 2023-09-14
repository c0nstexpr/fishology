package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.retry
import org.c0nstexpr.fishology.logger

abstract class SwitchDisposable : Disposable {
    private var disposable = null as Disposable?

    var enable
        get() = disposable != null
        set(value) {
            disposable = if (value && (disposable == null)) {
                onEnable()
            } else {
                onDisable()
                disposable?.dispose()
                null
            }

            logger.d("${if (value) "Enable" else "Disable"} auto fishing")
        }

    protected abstract fun onEnable(): Disposable

    protected open fun onDisable() {}

    override fun dispose() {
        enable = false
    }

    override val isDisposed get() = !enable

    protected fun <T> Observable<T>.tryOn(
        predicate: (Long, Throwable) -> Boolean = { _, _ -> false },
    ) = retry { i, e ->
        if (!predicate(i, e)) {
            logger.e(e.localizedMessage)
        }

        if (enable) {
            logger.d("Resubscribe events on $i times")
            enable = false
            enable = true

            true
        } else {
            false
        }
    }
}
