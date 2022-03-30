package me.xx2bab.koncat.sample.kotlin

import me.xx2bab.koncat.sample.annotation.ExportAPI

@ExportAPI
class ExportedPureKotlinLibraryRunnable : Runnable {
    override fun run() {
        println("ExportedPureKotlinLibraryAPI is running...")
    }
}

