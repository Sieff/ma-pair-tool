package com.github.sieff.mapairtool.util.observerPattern.publisher

import com.github.sieff.mapairtool.util.observerPattern.observer.Observer

interface Publisher<T> {
    fun subscribe(observer: Observer<T>)
    fun unsubscribe(observer: Observer<T>)
    fun publish(data: T)
}