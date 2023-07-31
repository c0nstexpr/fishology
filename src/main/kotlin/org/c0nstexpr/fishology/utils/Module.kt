package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose

internal abstract class Module : DisposableScope by DisposableScope() {
    private val dependencies = mutableSetOf<Module>()
    private val dependents = mutableSetOf<Module>()

    init {
        doOnDispose(::onDestroy)
    }

    fun add(vararg dependency: Module) = dependency.forEach {
        dependencies.add(it)
        it.onAdd(this)
    }

    fun remove(vararg dependency: Module) = dependency.forEach {
        dependencies.remove(it)
        it.onRemove(this)
    }

    private fun onAdd(dependency: Module) =
            dependents.run {
                if (isEmpty()) onCreate()
                add(dependency)
            }

    private fun onRemove(dependency: Module) =
            dependents.run {
                if (count() == 1) onDestroy()
                remove(dependency)
            }

    abstract fun onCreate()
    open fun onDestroy() {
        dependencies.forEach { it.onRemove(this) }
        dependencies.clear()
    }
}
