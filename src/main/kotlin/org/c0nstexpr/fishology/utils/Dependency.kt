package org.c0nstexpr.fishology.utils

internal class Dependency {
    private var refCount = 0u
        set(value) {
            if (value == 0u) onDestroy() else onCreate()

            field = value
        }

    abstract fun onCreate()
    abstract fun onDestroy()
}
