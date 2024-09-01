package com.github.sieff.mapairtool.services

class Logger(val className: String) {

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
        println("${debug}[DEBUG] ${className}:${reset} $msg")
    }

    fun info(msg: Any) {
        println("${info}[INFO] ${className}:${reset} $msg")
    }

    fun warn(msg: Any) {
        println("${warn}[WARN] ${className}:${reset} $msg")
    }

    fun error(msg: Any) {
        println("${error}[ERROR] ${className}:${reset} $msg")
    }

    fun fatal(msg: Any) {
        println("${fatal}[FATAL] ${className}:${reset} $msg")
    }
}