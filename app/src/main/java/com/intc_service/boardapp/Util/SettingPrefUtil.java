package com.intc_service.boardapp.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 設定情報に関するユーティリティ
 */

public class SettingPrefUtil {

    // ファイル名
    public static final String PREF_FILE_NAME = "settings";

    private static final String KEY_SERVER_IP_ADDR = "ip.server";             // key
    private static final String KEY_SERVER_IP_ADDR_DEFAULT = "192.168.10.20"; // default value

    private static final String KEY_SERVER_PORT = "port.server";    // key
    private static final int KEY_SERVER_PORT_DEFAULT = 1234;   // default value

    private static final String KEY_CLIENT_PORT = "port.client";    // key
    private static final int KEY_CLIENT_PORT_DEFAULT = 5678;   // default value


    public static String getServerIpAddress(Context context){
        SharedPreferences sp = context.getSharedPreferences( PREF_FILE_NAME, Context.MODE_PRIVATE );
        return sp.getString( KEY_SERVER_IP_ADDR, KEY_SERVER_IP_ADDR_DEFAULT);
    }
    public static int getServerPort(Context context){
        SharedPreferences sp = context.getSharedPreferences( PREF_FILE_NAME, Context.MODE_PRIVATE );
        return sp.getInt( KEY_SERVER_PORT, KEY_SERVER_PORT_DEFAULT);
    }
    public static int getClientPort(Context context){
        SharedPreferences sp = context.getSharedPreferences( PREF_FILE_NAME, Context.MODE_PRIVATE );
        return sp.getInt( KEY_CLIENT_PORT, KEY_CLIENT_PORT_DEFAULT);
    }

    public static void StoreServerIpAddress(Context context, String prefix){
        SharedPreferences sp = context.getSharedPreferences( PREF_FILE_NAME, Context.MODE_PRIVATE );

        // Editorを取得
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(KEY_SERVER_IP_ADDR, prefix);

        editor.apply();  // or commit()
    }
}
