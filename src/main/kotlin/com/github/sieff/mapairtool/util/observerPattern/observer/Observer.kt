package com.github.sieff.mapairtool.util.observerPattern.observer

interface Observer<T> {
    fun notify(data: T)
}