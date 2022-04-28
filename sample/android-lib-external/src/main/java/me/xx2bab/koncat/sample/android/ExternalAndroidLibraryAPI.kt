package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.interfaze.DummyAPI

class ExternalAndroidLibraryAPI : DummyAPI {

    override fun onCall(param: String) {
        println("ExternalAndroidLibraryAPI is running...")
    }

}