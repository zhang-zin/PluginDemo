package com.zj.plugindemo.util

import android.content.Context
import dalvik.system.DexClassLoader

class LoadUtil {

    companion object {
        private const val pluginAppName = "testplugin-debug.apk"
        fun loadClass(context: Context) {
            try {
                val copyFiles = AssetsUtil.copyFiles(context, pluginAppName)

                val baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader")
                val pathListField = baseDexClassLoader.getDeclaredField("pathList")
                pathListField.isAccessible = true

                val dexPathListClass = Class.forName("dalvik.system.DexPathList")
                val dexElementsField = dexPathListClass.getDeclaredField("dexElements")
                dexElementsField.isAccessible = true

                val classLoader = context.classLoader
                val hostDexPathList = pathListField.get(classLoader)
                val hostDexElements = dexElementsField.get(hostDexPathList) as Array<*>

                val dexClassLoader =
                    DexClassLoader(copyFiles, context.cacheDir.absolutePath, null, classLoader)

                val pluginDexPathList = pathListField.get(dexClassLoader)
                val pluginDexElements = dexElementsField.get(pluginDexPathList) as Array<*>


                // 宿主dexElements = 宿主dexElements + 插件dexElements
//            Object[] obj = new Object[]; // 不行

                // 创建一个新数组
                val newDexElements = java.lang.reflect.Array.newInstance(
                    hostDexElements.javaClass.componentType,
                    hostDexElements.size + pluginDexElements.size
                ) as Array<*>

                 System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexElements.size)
                 System.arraycopy(
                     pluginDexElements,
                     0,
                     newDexElements,
                     hostDexElements.size,
                     pluginDexElements.size
                 )

                 dexElementsField.set(hostDexPathList, newDexElements)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}