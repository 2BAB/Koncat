package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.interfaze.DummyAPI

class AndroidLibraryAPI: DummyAPI {
    override fun onCall(param: String) {
        println("ExportedAndroidLibraryAPI is running...")
    }
}