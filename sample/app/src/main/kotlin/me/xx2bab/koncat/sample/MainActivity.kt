package me.xx2bab.koncat.sample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import me.xx2bab.koncat.runtime.Koncat
import me.xx2bab.koncat.sample.android.AndroidLibraryAPI2
import me.xx2bab.koncat.sample.annotation.CustomMark
import me.xx2bab.koncat.sample.annotation.ExportActivity
import me.xx2bab.koncat.sample.annotation.MemberRequired
import me.xx2bab.koncat.sample.interfaze.DummyAPI
import me.xx2bab.koncat.sample.kotlin.KotlinLibraryAPI2
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

@ExportActivity
@CustomMark
@MemberRequired(level = 1)
class MainActivity : Activity() {

    val androidApi2: AndroidLibraryAPI2 by inject()
    val kotlinApi2: KotlinLibraryAPI2 by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Custom Processor test
        val router = Class.forName("me.xx2bab.koncat.sample.CustomRouterImpl")
            .newInstance() as CustomRouter
        val apis = router.getDummyAPIList().joinToString("\r\n")
        findViewById<TextView>(R.id.extend_proc_text_view).text = apis
        Log.d("Export APIs: ", apis)

        // Koncat Processor test
        val koncat = Koncat()

        val collectedAnnotatedClasses = koncat.getAnnotatedClasses(ExportActivity::class)!!.joinToString("\r\n") { it.name }
        val collectedInterfaces = koncat.getTypedClasses(DummyAPI::class)!!.map { constructor ->
            constructor().onCall("MainActivity")
        }.joinToString("\r\n")
        val collectedProperties = koncat.getTypedProperties(Module::class)!!.size

        findViewById<TextView>(R.id.koncat_proc_text_view).text = """
            1. collectedAnnotatedClasses:
            $collectedAnnotatedClasses
            2. collectedInterfaces:
            $collectedInterfaces
            3. collectedProperties (size): $collectedProperties
        """.trimIndent()

        androidApi2.onCall("")
        kotlinApi2.onCall("")
    }

    @CustomMark
    class NestedClass

    @CustomMark
    inner class InnerClass
}