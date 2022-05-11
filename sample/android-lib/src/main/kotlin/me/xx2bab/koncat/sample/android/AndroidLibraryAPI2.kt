package me.xx2bab.koncat.sample.android

import me.xx2bab.koncat.sample.interfaze.DummyAPI2

class AndroidLibraryAPI2: DummyAPI2 {
    override fun onCall(param: String) {
        println("AndroidLibraryAPI2 is running...")
    }
}