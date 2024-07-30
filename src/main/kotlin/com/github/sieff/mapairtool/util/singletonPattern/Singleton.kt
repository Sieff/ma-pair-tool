package com.github.sieff.mapairtool.util.singletonPattern

open class Singleton<out T : Any>(private val creator: () -> T) {
    @Volatile
    private var instance: T? = null

    fun getInstance(): T {
        return instance ?: synchronized(this) {
            instance ?: creator().also { instance = it }
        }
    }
}
