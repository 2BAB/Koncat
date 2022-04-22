package me.xx2bab.koncat.sample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import me.xx2bab.koncat.sample.annotation.ClassMark
import me.xx2bab.koncat.sample.annotation.ExportActivity

@ExportActivity
@ClassMark
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val router = Class.forName("me.xx2bab.koncat.sample.ExportCapabilityRouterImpl").newInstance()
                as ExportCapabilityRouter
        val apis = router.getExportAPIList().joinToString("\r\n")
        findViewById<TextView>(R.id.apis_text_view).text = apis
        Log.d("Export APIs: ", apis)
    }
}