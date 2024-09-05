package com.github.sieff.mapairtool.util

class Logger<T>(private val clazz: Class<T>) {

    // Debug level - Light Blue or Cyan
    private val debug = "\u001b[36m"

    // Info level - Green
    private val info = "\u001b[32m"

    // Warning level - Yellow
    private val warn = "\u001b[33m"

    // Error level - Red
    private val error = "\u001b[31m"

    // Fatal level - Magenta or Dark Red
    private val fatal = "\u001b[35m"

    // Resets previous color codes
    private val reset = "\u001b[0m"

    fun debug(msg: Any) {
        println("${debug}[DEBUG] ${clazz.simpleName}:${reset} $msg")
    }

    fun info(msg: Any) {
        println("${info}[INFO] ${clazz.simpleName}:${reset} $msg")
    }

    fun warn(msg: Any) {
        println("${warn}[WARN] ${clazz.simpleName}:${reset} $msg")
    }

    fun error(msg: Any) {
        println("${error}[ERROR] ${clazz.simpleName}:${reset} $msg")
    }

    fun fatal(msg: Any) {
        println("${fatal}[FATAL] ${clazz.simpleName}:${reset} $msg")
    }
}