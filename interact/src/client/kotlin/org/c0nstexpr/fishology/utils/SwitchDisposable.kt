package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.Disposable

abstract class SwitchDisposable : Disposable {
    private var disposable = null as Disposable?

    var enable
        get() = disposable != null
        set(value) {
            disposable =
                if (value && (disposable == null)) {
                    onEnable()
                } else {
                    onDisable()
                    disposable?.dispose()
                    null
                }
        }

    protected abstract fun onEnable(): Disposable

    protected open fun onDisable() {}

    override fun dispose() {
        enable = false
    }

    override val isDisposed get() = !enable
}
