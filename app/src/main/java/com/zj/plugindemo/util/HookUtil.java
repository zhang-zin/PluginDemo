package com.zj.plugindemo.util;


import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.zj.plugindemo.ProxyActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * hook Activity启动流程
 *
 * @author zhangjin
 */
public class HookUtil {

    private static final String TARGET_INTENT = "target_intent";

    public static void hookAms() {
        try {
            Field singletonField;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Class<?> activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");
                singletonField = activityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                singletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            } else {
                Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
                singletonField = activityManagerNativeClass.getDeclaredField("gDefault");
            }

            // 获取Singleton<T> 类对象
            singletonField.setAccessible(true);
            Object gDefault = singletonField.get(null);

            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(gDefault);

            if (mInstance == null) {
                return;
            }
            Class<?> iActivityManagerClass;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                iActivityManagerClass = Class.forName("android.app.IActivityManager");
            } else {
                iActivityManagerClass = Class.forName("android.app.IActivityTaskManager");
            }
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iActivityManagerClass},
                    (proxy, method, args) -> {
                        if ("startActivity".equals(method.getName())) {
                            for (int i = 0; i < args.length; i++) {
                                if (args[i] instanceof Intent) {
                                    Intent intent = (Intent) args[i];
                                    Intent proxyIntent = new Intent();
                                    proxyIntent.setClassName("com.zj.plugindemo", ProxyActivity.class.getName());
                                    proxyIntent.putExtra(TARGET_INTENT, intent);
                                    args[i] = proxyIntent;
                                    break;
                                }
                            }
                        }

                        return method.invoke(mInstance, args);
                    });

            mInstanceField.set(gDefault, proxyInstance);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void hookHandler() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field field = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            field.setAccessible(true);
            Object sCurrentActivityThread = field.get(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            final Handler mH = (Handler) mHField.get(sCurrentActivityThread);

            Field mCallback = Handler.class.getDeclaredField("mCallback");
            mCallback.setAccessible(true);
            mCallback.set(mH, (Handler.Callback) msg -> {
                switch (msg.what) {
                    case 100:
                        try {
                            Field intentField = msg.obj.getClass().getDeclaredField("intent");
                            intentField.setAccessible(true);
                            Intent proxyIntent = (Intent) intentField.get(msg.obj);

                            Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                            if (intent != null) {
                                intentField.set(msg.obj, intent);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 159:
                        try {
                            Field mActivityCallbacks = msg.obj.getClass().getDeclaredField("mActivityCallbacks");
                            mActivityCallbacks.setAccessible(true);
                            List list = (List) mActivityCallbacks.get(msg.obj);
                            for (int i = 0; i < list.size(); i++) {
                                if ("android.app.servertransaction.LaunchActivityItem".equals(list.get(i).getClass().getName())) {
                                    Object launchActivityItem = list.get(i);
                                    Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                    mIntentField.setAccessible(true);
                                    Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);

                                    Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                    if (intent != null) {
                                        mIntentField.set(launchActivityItem, intent);
                                    }
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            });

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
