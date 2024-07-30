package com.github.sieff.mapairtool.util.observerPattern.publisher

import com.github.sieff.mapairtool.util.observerPattern.observer.Observer

abstract class APublisher<T> : Publisher<T> {
    private val observers: MutableList<Observer<T>> = mutableListOf()

    override fun subscribe(observer: Observer<T>) {
        observers.add(observer)
    }
    override fun unsubscribe(observer: Observer<T>) {
        observers.remove(observer)
    }

    override fun publish(data: T) {
        observers.forEach { observer ->
            observer.notify(data)
        }
    }
}