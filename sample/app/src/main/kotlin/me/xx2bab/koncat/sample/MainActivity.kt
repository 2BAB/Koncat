package me.xx2bab.koncat.sample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import me.xx2bab.koncat.cupcake.KoncatCupCake
import me.xx2bab.koncat.sample.annotation.CustomMark
import me.xx2bab.koncat.sample.annotation.ExportActivity
import me.xx2bab.koncat.sample.annotation.MemberRequired

@ExportActivity
@CustomMark
@MemberRequired(level = 1)
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Custom Processor test
        val router = Class.forName("me.xx2bab.koncat.sample.CustomRouterImpl")
            .newInstance() as CustomRouter
        val apis = router.getCustomMarkList().joinToString("\r\n")
        findViewById<TextView>(R.id.apis_text_view).text = apis
        Log.d("Export APIs: ", apis)

        // Koncat Cupcake Processor test
        val cupcake = KoncatCupCake()
        val exportActivities = cupcake.getAnnotatedClasses(ExportActivity::class)!!
        findViewById<TextView>(R.id.cupcake_text_view).text = exportActivities
            .map { it.name }.joinToString("\r\n")
    }

    @CustomMark
    class NestedClass

    @CustomMark
    inner class InnerClass
}