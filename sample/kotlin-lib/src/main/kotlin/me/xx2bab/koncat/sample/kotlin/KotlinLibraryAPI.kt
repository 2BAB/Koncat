package me.xx2bab.koncat.sample.kotlin

import me.xx2bab.koncat.sample.interfaze.DummyAPI

class KotlinLibraryAPI : DummyAPI {

    override fun onCall(param: String) {
        println("ExportedPureKotlinLibraryAPI is running...")
    }

}

