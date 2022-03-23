package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.annotation.ExportAPI

@ExportAPI
class ExportedAndroidLibraryRunnable: Runnable {
    override fun run() {
        println("ExportedAndroidLibraryAPI is running")
    }
}