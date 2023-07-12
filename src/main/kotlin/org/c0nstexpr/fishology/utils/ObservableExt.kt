package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.base.ValueCallback

private val valueCallbackInvoked = object : ThreadLocal<MutableSet<ValueCallback<*>>>() {
    override fun initialValue(): MutableSet<ValueCallback<*>> {
        return mutableSetOf()
    }
}

private var <T> ValueCallback<T>.isOnNextInvoked: Boolean
    get() = valueCallbackInvoked.get().contains(this)
    set(value) {
        if (value) valueCallbackInvoked.get().add(this)
        else valueCallbackInvoked.get().remove(this)
    }

fun <T> ValueCallback<T>.onNextOnce(t: T) {
    if(isOnNextInvoked)  throw IllegalStateException("onUseRod is already called")

    isOnNextInvoked = true
    onNext(t)
    isOnNextInvoked = false
}
