package com.zj.plugindemo

import android.app.Application
import com.zj.plugindemo.util.LoadClassUtil
import com.zj.plugindemo.util.LoadUtil

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LoadClassUtil.loadClass(this)
    }
}