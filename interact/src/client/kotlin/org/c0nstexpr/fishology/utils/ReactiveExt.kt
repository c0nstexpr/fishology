package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.CompletableObserver
import com.badoo.reaktive.completable.doOnAfterSubscribe
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.MaybeObserver
import com.badoo.reaktive.maybe.doOnAfterSubscribe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.flatMapCompletable
import com.badoo.reaktive.maybe.flatMapObservable
import com.badoo.reaktive.maybe.flatMapSingle
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.concatMap
import com.badoo.reaktive.observable.concatMapMaybe
import com.badoo.reaktive.observable.concatMapSingle
import com.badoo.reaktive.observable.doOnAfterSubscribe
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapCompletable
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.observable.switchMapSingle
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.SingleObserver
import com.badoo.reaktive.single.doOnAfterSubscribe
import com.badoo.reaktive.single.flatMap
import com.badoo.reaktive.single.flatMapCompletable
import com.badoo.reaktive.single.flatMapMaybe
import com.badoo.reaktive.single.flatMapObservable

fun <T> observableStep(src: Observable<T>, next: (T.() -> Unit)? = null) = ObservableStep(src, next)

fun <T> maybeStep(src: Maybe<T>, next: (T.() -> Unit)? = null) = MaybeStep(src, next)

fun <T> singleStep(src: Single<T>, next: (T.() -> Unit)? = null) = SingleStep(src, next)

fun completableStep(src: Completable, next: (() -> Unit)? = null) = CompletableStep(src, next)

class ObservableStep<T>(private val src: Observable<T>, val nextStep: (T.() -> Unit)? = null) :
    Observable<T> by src {
    override fun subscribe(observer: ObservableObserver<T>) = src.subscribe(
        object : ObservableObserver<T> by observer {
            override fun onNext(value: T) {
                observer.onNext(value)

                try {
                    nextStep?.invoke(value)
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    )

    fun <U> switch(nextSrc: T.() -> Observable<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(switchMap { t -> t.nextSrc() }, nextStep)

    fun <U> switch(nextSrc: Observable<U>, nextStep: (U.() -> Unit)? = null) = switch({ nextSrc }, nextStep)

    fun <U> concat(nextSrc: T.() -> Observable<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(concatMap { t -> t.nextSrc() }, nextStep)

    fun <U> concat(nextSrc: Observable<U>, nextStep: (U.() -> Unit)? = null) = concat({ nextSrc }, nextStep)

    fun <U> switchMaybe(nextSrc: T.() -> Maybe<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(switchMapMaybe { t -> t.nextSrc() }, nextStep)

    fun <U> switch(nextSrc: Maybe<U>, nextStep: (U.() -> Unit)? = null) = switchMaybe({ nextSrc }, nextStep)

    fun <U> concatMaybe(nextSrc: T.() -> Maybe<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(concatMapMaybe { t -> t.nextSrc() }, nextStep)

    fun <U> concat(nextSrc: Maybe<U>, nextStep: (U.() -> Unit)? = null) = concatMaybe({ nextSrc }, nextStep)

    fun <U> switchSingle(nextSrc: T.() -> Single<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(switchMapSingle { t -> t.nextSrc() }, nextStep)

    fun <U> switch(nextSrc: Single<U>, nextStep: (U.() -> Unit)? = null) = switchSingle({ nextSrc }, nextStep)

    fun <U> concatSingle(nextSrc: T.() -> Single<U>, nextStep: (U.() -> Unit)? = null) =
        ObservableStep(concatMapSingle { t -> t.nextSrc() }, nextStep)

    fun <U> concat(nextSrc: Single<U>, nextStep: (U.() -> Unit)? = null) = concatSingle({ nextSrc }, nextStep)

    fun switchCompletable(nextSrc: T.() -> Completable, nextStep: () -> Unit) =
        CompletableStep(switchMapCompletable { t -> t.nextSrc() }, nextStep)

    fun switch(nextSrc: Completable, nextStep: () -> Unit) = switchCompletable({ nextSrc }, nextStep)

    fun concatSingle(nextSrc: T.() -> Completable, nextStep: () -> Unit) = CompletableStep(
        flatMapCompletable { t -> t.nextSrc() },
        nextStep
    )

    fun concat(nextSrc: Completable, nextStep: () -> Unit) = concatSingle({ nextSrc }, nextStep)
}

class MaybeStep<T>(private val src: Maybe<T>, val next: (T.() -> Unit)? = null) : Maybe<T> by src {
    override fun subscribe(observer: MaybeObserver<T>) = src.subscribe(
        object : MaybeObserver<T> by observer {
            override fun onSuccess(value: T) {
                observer.onSuccess(value)

                try {
                    next?.invoke(value)
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    )

    fun <U> flatObservable(nextSrc: T.() -> Observable<U>, nextStep: (U.() -> Unit)? = null) = ObservableStep(
        flatMapObservable { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } },
        nextStep
    )

    fun <U> flat(nextSrc: Observable<U>, nextStep: (U.() -> Unit)? = null) = flatObservable({ nextSrc }, nextStep)

    fun <U> flat(nextSrc: T.() -> Maybe<U>, nextStep: (U.() -> Unit)? = null) =
        MaybeStep(flatMap { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } }, nextStep)

    fun <U> flat(nextSrc: Maybe<U>, nextStep: (U.() -> Unit)? = null) = flat({ nextSrc }, nextStep)

    fun <U> flatSingle(nextSrc: T.() -> Single<U>, nextStep: (U.() -> Unit)? = null) = MaybeStep(
        flatMapSingle { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } },
        nextStep
    )

    fun <U> flat(nextSrc: Single<U>, nextStep: (U.() -> Unit)? = null) = flatSingle({ nextSrc }, nextStep)

    fun flatCompletable(nextSrc: T.() -> Completable, nextStep: () -> Unit) = CompletableStep(
        flatMapCompletable { t ->
            t.nextSrc().doOnAfterSubscribe { next?.invoke(t) }
        },
        nextStep
    )

    fun flat(nextSrc: Completable, nextStep: () -> Unit) = flatCompletable({ nextSrc }, nextStep)
}

class SingleStep<T>(private val src: Single<T>, val next: (T.() -> Unit)? = null) : Single<T> by src {
    override fun subscribe(observer: SingleObserver<T>) = src.subscribe(
        object : SingleObserver<T> by observer {
            override fun onSuccess(value: T) {
                observer.onSuccess(value)
                try {
                    next?.invoke(value)
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    )

    fun <U> flatObservable(nextSrc: T.() -> Observable<U>, nextStep: (U.() -> Unit)? = null) = ObservableStep(
        flatMapObservable { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } },
        nextStep
    )

    fun <U> flat(nextSrc: Observable<U>, nextStep: (U.() -> Unit)? = null) = flatObservable({ nextSrc }, nextStep)

    fun <U> flatMaybe(nextSrc: T.() -> Maybe<U>, nextStep: (U.() -> Unit)? = null) = MaybeStep(
        flatMapMaybe { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } },
        nextStep
    )

    fun <U> flat(nextSrc: Maybe<U>, nextStep: (U.() -> Unit)? = null) = flatMaybe({ nextSrc }, nextStep)

    fun <U> flatSingle(nextSrc: T.() -> Single<U>, nextStep: (U.() -> Unit)? = null) =
        SingleStep(flatMap { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } }, nextStep)

    fun <U> flat(nextSrc: Single<U>, nextStep: (U.() -> Unit)? = null) = flatSingle({ nextSrc }, nextStep)

    fun flatCompletable(nextSrc: T.() -> Completable, nextStep: () -> Unit) = CompletableStep(
        flatMapCompletable { t -> t.nextSrc().doOnAfterSubscribe { next?.invoke(t) } },
        nextStep
    )

    fun flat(nextSrc: Completable, nextStep: () -> Unit) = flatCompletable({ nextSrc }, nextStep)
}

class CompletableStep(private val src: Completable, val next: (() -> Unit)? = null) : Completable by src {
    override fun subscribe(observer: CompletableObserver) = src.subscribe(
        object : CompletableObserver by observer {
            override fun onComplete() {
                try {
                    next?.invoke()
                } catch (e: Throwable) {
                    onError(e)
                    return
                }
                observer.onComplete()
            }
        }
    )
}
