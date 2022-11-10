package com.example.root.xposd_hook_new;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

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
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 一系列的广告去除定义
        if (loadPackageParam.packageName.equals("com.kugou.android")) {
            replaceLauncherActivity("com.kugou.android.app.MediaActivity", loadPackageParam.classLoader);
        } else if (loadPackageParam.packageName.equals("com.netease.cloudmusic")) {
            replaceLauncherActivity("com.netease.cloudmusic.activity.MainActivity", loadPackageParam.classLoader);
        }
    }
}