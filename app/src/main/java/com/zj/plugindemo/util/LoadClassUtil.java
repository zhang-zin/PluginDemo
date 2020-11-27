package com.zj.plugindemo.util;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * 合并插件apk
 *
 * @author zhangjin
 */
public class LoadClassUtil {

    private static final String PLUGIN_APK_NAME = "testplugin-debug.apk";

    public static void loadClass(Context context) {
        String copyFiles = AssetsUtil.INSTANCE.copyFiles(context, PLUGIN_APK_NAME);

        try {
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            // 宿主
            Object hostPathList = pathListField.get(context.getClassLoader());
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            // 插件
            DexClassLoader dexClassLoader = new DexClassLoader(copyFiles, context.getCacheDir().getAbsolutePath(), "", context.getClassLoader());
            Object pluginPathList = pathListField.get(dexClassLoader);
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            Object[] newDexElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(), hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, newDexElements, hostDexElements.length, pluginDexElements.length);

            dexElementsField.set(hostPathList, newDexElements);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
