package me.xx2bab.koncat.contract

interface KLogger {
    fun logging(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}