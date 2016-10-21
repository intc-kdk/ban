package com.intc_service.boardapp.Util;

import android.os.Bundle;
import android.util.AndroidRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 送受信データに関するユーティリティ
 */

public class DataStructureUtil {

    private static final String FORMAT = "^(\\d{2})@(.*)\\$$";
    private static final String JSON_FORMAT = "^\\{(.*)\\}$";
    private static final String DELIMITER = "@";
    private static final String TRAILER = "$";

    private static final String KEY_FORMAT = "format";
    private static final String KEY_COMMAND = "command";
    private static final String KEY_TEXT = "text";
    private static final String DATA_TYPE_JSON = "JSON";
    private static final String DATA_TYPE_TEXT = "TEXT";

    private String mRecievedData;
    private String mRecievedCmd;

    public static List<ProcItem> ITEMS = new ArrayList<ProcItem>();

    public DataStructureUtil(){

    }

    public String setRecievedData(String d){
        Pattern p = Pattern.compile(FORMAT);
        Matcher m = p.matcher(d);
        String cmd ="";

        if(m.find()){
            this.mRecievedCmd=m.group(1);
            this.mRecievedData=m.group(2);
            cmd = mRecievedCmd;
        }else{
            // TODO: 不正フォーマットの処理
            cmd = "99";
        }

        return cmd;
    }

    public Bundle getRecievedData(){

        Pattern p = Pattern.compile(JSON_FORMAT);
        Matcher m = p.matcher(mRecievedData);
        Bundle bdData = new Bundle();
        ITEMS= new ArrayList<ProcItem>();
        // 先頭、末尾のブラケットでのJSONフォーマットを簡易判定
        if(m.find()){
            //JSONデータ は"JSON" を返し、別メソッドでJSONObjectを取得
            final JSONObject jo;
            try {
                jo = new JSONObject(mRecievedData);
                bdData = this.toBundle(jo);   // JSONからBundleへ変換
                this.toList(jo);
            }
            catch (JSONException e){
                    // 例外処理
                throw new AndroidRuntimeException(e);
            }
            bdData.putString(KEY_FORMAT,DATA_TYPE_JSON);
        }else{
            //テキストデータ
            bdData.putString(KEY_FORMAT,DATA_TYPE_TEXT);
            bdData.putString(KEY_TEXT,mRecievedData);
        }
        bdData.putString(KEY_COMMAND,mRecievedCmd);
        return bdData;
    }

    public String makeSendData(String cmd, String data){
        StringBuilder sb = new StringBuilder();

        sb.append(cmd);
        sb.append(DELIMITER);
        sb.append(data);
        sb.append(TRAILER);

        return cmd + DELIMITER + data + TRAILER; //sb.toString();
    }

    public static Bundle toBundle(final JSONObject json) {
        final Bundle bundle = new Bundle();
        final Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (json.isNull(key)) {
                bundle.putString(key, null);
                continue;
            }
            final Object value = json.opt(key);
            if(key.equals("")){ key = "blank";}

            if (value instanceof JSONObject) {
                bundle.putBundle(key, toBundle((JSONObject) value));
            } else if (value instanceof JSONArray) {
                bundle.putParcelableArrayList(key, toBundle((JSONArray) value));
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (boolean) value);
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (int) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (long) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (double) value);
            }
        }
        return bundle;
    }



    /**
     * JSONArray を ArrayList<Bundle> に適切に変換して返す.
     *
     * @param array JSONArray
     * @return ArrayList<Bundle>
     */
    public static ArrayList<Bundle> toBundle(final JSONArray array) {
        final ArrayList<Bundle> bundles = new ArrayList<>();
        for (int i = 0, size = array.length(); i < size; i++) {
            bundles.add(toBundle(array.optJSONObject(i)));
        }
        return bundles;
    }

    /* */
    public static void toList(final JSONObject json) {

        final Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            if (json.isNull(key)) {
                continue;
            }
            if(key.equals("tejun")){
                final Object value = json.opt(key);
                if (value instanceof JSONArray) {
                    makeEntry((JSONArray) value);
                }
            }
            final Object value = json.opt(key);
        }
    }

    private static void addItem(ProcItem item) {
        ITEMS.add(item);
    }
    private static ProcItem createProcItem(Bundle entry) {
        return new ProcItem(entry.getString("in_sno"), entry.getString("in_swno"), entry.getString("tx_sno"), entry.getString("tx_basho"),  entry.getString("tx_bname"), entry.getString("tx_swname"),
                entry.getString("tx_action"), entry.getString("tx_biko"), entry.getString("dotime"), entry.getString("tx_gs"), entry.getString("tx_com"), entry.getString("tx_s_l"),
                entry.getString("tx_s_r"),entry.getString("tx_b_l"), entry.getString("tx_b_r"), entry.getString("tx_clr1"), entry.getString("tx_clr2"), entry.getString("ts_b"),
                entry.getString("cd_status"), entry.getString("bo_gs"));
    }
    private static void  makeEntry(final JSONArray array) {
        for (int i = 0, size = array.length(); i < size; i++) {
            final Iterator<String> iterator = array.optJSONObject(i).keys();
            final Bundle bEntry = new Bundle();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                final Object value = array.optJSONObject(i).opt(key);

                if (array.optJSONObject(i).isNull(key)) {
                    continue;
                }
                bEntry.putString(key, value.toString());
            }
            addItem(createProcItem(bEntry));
        }
    }

    /**
     * 手順書データのクラス
    */
    public static class ProcItem {
        public final int in_sno;
        public final String in_swno;
        public final String tx_sno;
        public final String tx_basho;
        public final String tx_bname;
        public final String tx_swname;
        public final String tx_action;
        public final String tx_biko;
        public final String dotime;
        public final String tx_gs;
        public final String tx_com;
        public final String tx_s_l;
        public final String tx_s_r;
        public final String tx_b_l;
        public final String tx_b_r;
        public final String tx_clr1;
        public final String tx_clr2;
        public final String ts_b;
        public String cd_status;
        public final String bo_gs;

        public ProcItem(String in_sno ,String in_swno ,String tx_sno ,String tx_basho , String tx_bname ,String tx_swname ,String tx_action ,String tx_biko ,String dotime ,String tx_gs ,String tx_com ,String tx_s_l ,String tx_s_r ,String tx_b_l ,String tx_b_r ,String tx_clr1 ,String tx_clr2 ,String ts_b ,String cd_status ,String bo_gs){
            this.in_sno = Integer.parseInt(in_sno);
            this.in_swno = in_swno;
            this.tx_sno = tx_sno;
            this.tx_basho = tx_basho;
            this.tx_bname = tx_bname;
            this.tx_swname = tx_swname;
            this.tx_action = tx_action;
            this.tx_biko = tx_biko;
            this.dotime = dotime;
            this.tx_gs = tx_gs;
            this.tx_com = tx_com;
            this.tx_s_l = tx_s_l;
            this.tx_s_r = tx_s_r;
            this.tx_b_l = tx_b_l;
            this.tx_b_r = tx_b_r;
            this.tx_clr1 = tx_clr1;
            this.tx_clr2 = tx_clr2;
            this.ts_b = ts_b;
            this.cd_status = cd_status;
            this.bo_gs = bo_gs;
        }
    }
}
