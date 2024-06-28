package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose

abstract class SwitchDisposable : DisposableScope by DisposableScope() {
    private var disposable: Disposable? = null

    var enable
        get() = disposable != null
        set(value) {
            disposable = if (value && (disposable == null)) onEnable()
            else {
                onDisable()
                disposable?.dispose()
                null
            }
        }

    init {
        doOnDispose { enable = false }
    }

    protected abstract fun onEnable(): Disposable

    protected open fun onDisable() {}
}
