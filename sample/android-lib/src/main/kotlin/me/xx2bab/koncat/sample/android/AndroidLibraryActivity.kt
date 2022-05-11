package me.xx2bab.koncat.sample.android

import android.app.Activity
import android.os.Bundle
import me.xx2bab.koncat.sample.annotation.CustomMark
import me.xx2bab.koncat.sample.annotation.ExportActivity
import me.xx2bab.koncat.sample.annotation.MemberRequired

@ExportActivity
@CustomMark
@MemberRequired(level = 2)
class AndroidLibraryActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}