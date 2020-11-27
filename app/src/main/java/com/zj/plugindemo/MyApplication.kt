package com.zj.plugindemo

import android.app.Application
import com.zj.plugindemo.util.HookUtil
import com.zj.plugindemo.util.LoadClassUtil

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LoadClassUtil.loadClass(this)

    }
}