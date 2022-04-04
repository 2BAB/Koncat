package me.xx2bab.koncat.sample.kotlin

import me.xx2bab.koncat.sample.annotation.ExportAPI

@ExportAPI
class ExportedPureKotlinLibraryRunnable2 : Runnable {
    override fun run() {
        println("ExportedPureKotlinLibraryAPI2 is running...")
    }
}

