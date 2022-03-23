package me.xx2bab.koncat.sample

import android.app.Activity
import android.os.Bundle
import me.xx2bab.koncat.sample.annotation.ExportActivity

@ExportActivity
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}