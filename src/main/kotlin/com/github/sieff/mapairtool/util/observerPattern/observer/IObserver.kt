package com.github.sieff.mapairtool.util.observerPattern.observer

interface IObserver<T> {
    fun notify(message: T)
}