package org.c0nstexpr.fishology.disposable

import com.badoo.reaktive.disposable.Disposable
import com.mojang.datafixers.types.Func
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.event.Event

fun <T, U> eventScopedDisposable(
    block: () -> Disposable,
    begin: Pair<Event<T>, (() -> Unit) -> T>,
    end: Pair<Event<U>, (() -> Unit) -> U>
) {
    var disposable: Disposable? = null
    begin.first.register(begin.second { disposable = block() })
    end.first.register(end.second { disposable?.dispose() })
}

interface MyInterface {
    fun doSomething()
}

fun f() {
    val lambda = { println("Doing something!") }
    val methodRef = MyInterface::doSomething

    val myInstance = MyInterface::class.java.getMethod("invoke", Any::class.java)
        .invoke(lambda, MyInterface::class.java)

    myInstance.doSomething() // prints "Doing something!"

    val myInstance2 = MyInterface::class.java.getMethod("invoke", Any::class.java)
        .invoke(methodRef, MyInterface::class.java)

    myInstance2.doSomething() // prints "Doing something!"
}
