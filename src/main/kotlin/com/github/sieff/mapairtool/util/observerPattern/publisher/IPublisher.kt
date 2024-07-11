package com.github.sieff.mapairtool.util.observerPattern.publisher

import com.github.sieff.mapairtool.util.observerPattern.observer.IObserver

interface IPublisher<T> {
    fun subscribe(observer: IObserver<T>)
    fun unsubscribe(observer: IObserver<T>)
}