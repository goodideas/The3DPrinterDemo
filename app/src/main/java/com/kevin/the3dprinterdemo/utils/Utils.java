package com.kevin.the3dprinterdemo.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator
 * on 2016/9/18.
 */
public class Utils {

    private static Toast toast;

    public static void showInfo(Context context,String data){
        if(toast==null){
            toast = Toast.makeText(context,data,Toast.LENGTH_SHORT);
        }else{
            toast.setText(data);
        }
        toast.show();
    }


    /**
     * 设置 ip和端口
     * @param etIp ip
     * @param etPort 端口
     * @param spHelper sharedPreferences
     */
    public static void onSetIpAndPort(Context context,EditText etIp, EditText etPort, SpHelper spHelper,TextView tvIp,TextView tvPort) {
        //判断spHelper是否为空
        if (spHelper == null) {
            spHelper = SpHelper.getSingleton(context);
        }

        //如果etIp不为空，并且sp也有保存数据，就设置
        if (!TextUtils.isEmpty(spHelper.getSpIp())) {

            if(etIp!=null){
                etIp.setText(spHelper.getSpIp());
            }
            if(tvIp!=null){
                tvIp.setText(spHelper.getSpIp());
            }


        }

        //如果etPort不为空，并且sp也有保存数据，就设置
        if (!TextUtils.isEmpty(spHelper.getSpPort())) {
            if(etPort!=null){
                etPort.setText(spHelper.getSpPort());
            }
            if(tvPort!=null){
                tvPort.setText(spHelper.getSpPort());
            }

        }

    }


    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }







}
