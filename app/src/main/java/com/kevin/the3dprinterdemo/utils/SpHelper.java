package com.kevin.the3dprinterdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator
 * on 2016/9/18.
 */
public class SpHelper {

    private volatile static SpHelper singleton;

    private static final String spFileName = "The3DPrinterConfig";
    private static final String spIp = "ip";
    private static final String spPort = "port";
    private static final String spTest = "test";

    private SharedPreferences configShared;
    private SharedPreferences.Editor configEditor;

    private SpHelper(Context context) {
        configShared = context.getSharedPreferences(spFileName, Activity.MODE_PRIVATE);
    }

    public static SpHelper getSingleton(Context context) {
        if (singleton == null) {
            synchronized (SpHelper.class) {
                if (singleton == null) {
                    singleton = new SpHelper(context);
                }
            }
        }
        return singleton;
    }

    //Ip
    public String getSpIp() {
        return configShared.getString(spIp, null);
    }

    public void saveSpIp(String ip) {
        configEditor = configShared.edit();
        configEditor.putString(spIp, ip);
        configEditor.apply();
    }

    //Port
    public String getSpPort() {
        return configShared.getString(spPort, null);
    }

    public void saveSpPort(String ip) {
        configEditor = configShared.edit();
        configEditor.putString(spPort, ip);
        configEditor.apply();
    }

    //Test
    public String getSpTest() {
        return configShared.getString(spTest, null);
    }

    public void saveSpTest(String test) {
        configEditor = configShared.edit();
        configEditor.putString(spTest, test);
        configEditor.apply();
    }


}
