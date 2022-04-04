package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.annotation.ExportAPI

@ExportAPI
class ExportedAndroidLibraryExternalRunnable: Runnable {
    override fun run() {
        println("ExportedAndroidLibraryExternalRunnable is running...")
    }
}