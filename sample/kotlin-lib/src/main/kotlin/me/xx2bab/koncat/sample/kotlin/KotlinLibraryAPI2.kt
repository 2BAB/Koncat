package me.xx2bab.koncat.sample.kotlin

import me.xx2bab.koncat.sample.interfaze.DummyAPI2

class KotlinLibraryAPI2 : DummyAPI2 {

    override fun onCall(param: String) {
        println("KotlinLibraryAPI2 is running...")
    }

}

