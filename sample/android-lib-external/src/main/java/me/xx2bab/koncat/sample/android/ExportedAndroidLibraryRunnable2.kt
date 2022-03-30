package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.annotation.ExportAPI

@ExportAPI
class ExportedAndroidLibraryRunnable2: Runnable {
    override fun run() {
        println("ExportedAndroidLibraryAPI2 is running...")
    }
}