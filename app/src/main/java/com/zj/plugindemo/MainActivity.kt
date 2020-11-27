package com.zj.plugindemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun openPlugin(view: View) {
        try {
            val clazz = Class.forName("com.zj.testplugin.Test")
            val print = clazz.getMethod("test")
            print.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}