package com.github.sieff.mapairtool.util.observerPattern.publisher

import com.github.sieff.mapairtool.util.observerPattern.observer.IObserver

abstract class APublisher<T> {
    private val observers: MutableList<IObserver<T>> = mutableListOf()

    fun subscribe(observer: IObserver<T>) {
        observers.add(observer)
    }
    fun unsubscribe(observer: IObserver<T>) {
        observers.remove(observer)
    }

    protected fun publish(message: T) {
        observers.forEach { observer ->
            observer.notify(message)
        }
    }
}