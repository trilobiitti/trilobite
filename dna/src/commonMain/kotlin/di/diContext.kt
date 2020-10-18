package com.github.trilobiitti.trilobite.dna.di

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

data class DIContextElement(
    val di: DI,
    private val restContext: CoroutineContext
) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        var intercepted = Continuation<T>(continuation.context) { result ->
            withDI(di) { continuation.resumeWith(result) }
        }

        restContext[ContinuationInterceptor]?.let {
            intercepted = it.interceptContinuation(intercepted)
        }

        return intercepted
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        restContext[ContinuationInterceptor]?.releaseInterceptedContinuation(continuation)
    }

    override fun plus(context: CoroutineContext): CoroutineContext =
        DIContextElement(di, restContext + context)

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext =
        DIContextElement(di, restContext.minusKey(key))

    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
        if (key === ContinuationInterceptor.Key) {
            @Suppress("UNCHECKED_CAST")
            return this as E
        }

        return restContext[key]
    }

    override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R =
        restContext.fold(operation(initial, this), operation)
}

fun <T> CoroutineScope.withDI(di: DI, block: CoroutineScope.() -> T): T =
    CoroutineScope(DIContextElement(di, this.coroutineContext)).run {
        com.github.trilobiitti.trilobite.dna.di.withDI(di) { block() }
    }
