package com.example.root.xposd_hook_new;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Button;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.XC_MethodHook;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import de.robv.android.xposed.XposedHelpers;

import de.robv.android.xposed.callbacks.XC_LoadPackage;



public class HookTest implements IXposedHookLoadPackage {
    protected void replaceLauncherActivity(final String activityName, ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("android.app.Activity",
                classLoader,
                "onStart",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        Activity activity = (Activity) methodHookParam.thisObject;
                        PackageManager pm = activity.getPackageManager();
                        Intent launcherIntent = pm.getLaunchIntentForPackage(activity.getPackageName());
                        Intent intent = activity.getIntent();
                        // 第一个LaunchIntent是广告页面, 所以当第二次获取的intent和launchIntent相同时, 就可以判断它是广告页面, 直接finish.
                        String curIntentName = launcherIntent.getComponent().flattenToString();
                        String intentName = intent.getComponent().flattenToString();
                        if (intentName.equals(curIntentName)) {
                            Intent Mainintent = new Intent();
                            Mainintent.setClassName(activity, activityName);
                            activity.finish();
                            activity.startActivity(Mainintent);
                        }
                    }
                }
        );
    }

    public static void autoLauch(final String LaunchReceiveUI, ClassLoader cl){
        // hook红包界面初始化“开”按钮的方法，在该方法完成后自动点击开按钮领取红包
        XposedHelpers.findAndHookMethod(LaunchReceiveUI, cl, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("执行了 LaunchReceiveUI 函数");
                        Field buttonField = XposedHelpers.findField(param.thisObject.getClass(), "aokI");
                        final Button kaiButton = (Button) buttonField.get(param.thisObject);
                        boolean b = kaiButton.performClick();
                        XposedBridge.log("执行点击");
                        // ((Activity)param.thisObject).finish();
                    }
                });
}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 一系列的广告去除定义
        if (loadPackageParam.packageName.equals("com.kugou.android")) {
            replaceLauncherActivity("com.kugou.android.app.MediaActivity", loadPackageParam.classLoader);
        } else if (loadPackageParam.packageName.equals("com.netease.cloudmusic")) {
            replaceLauncherActivity("com.netease.cloudmusic.activity.MainActivity", loadPackageParam.classLoader);
        } else if (loadPackageParam.packageName.equals("com.tencent.mm")) {
            autoLauch("com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI", loadPackageParam.classLoader);
        }
    }
}