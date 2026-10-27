package com.pierre.gcperf.ui

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "GCPerf Ativo!\n\nMódulo LSPosed funcionando.\nAs features estão sendo forçadas nos apps da Vivo."
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#121212"))
            gravity = Gravity.CENTER
        }
        setContentView(textView)
    }
}
