package com.intc_service.boardapp;

import android.content.Context;
import android.os.Build;

import com.intc_service.boardapp.Util.SettingPrefUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by takashi on 2016/10/24.
 */

public class AppLogRepository {
    //ファイル名フォーマット
    private static final String APPLOG_FILE_FORMAT = "yyyyMMdd";
    private static final String APPLOG_FILE_EXT = ".txt";

    private static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String DIRECTORY_LOGS = "Logs";

    //インスタンスを作らせない
    private AppLogRepository() {}

    //ログを新規に保存する
    public static String create(Context context, String kind, String data){

        String log = makeLogData(context, kind, data);
        //出力先ディレクトリを取得
        File outputDir = getOutputDir(context);

        if(outputDir == null){
            // ディレクトリが見つからない
            return null;
        }

        File outputFile = getFileName(context, outputDir);

        if(outputFile == null
                || !writeToFile(outputFile, log)){
            //ファイルの書き込みに失敗
            return null;
        }

        return "";
    }

    //ログの出力先ディレクトリを取得する
    private static File getOutputDir(Context context){
        File outputDir;

        if (Build.VERSION.SDK_INT >= 19) {
            outputDir = context.getExternalFilesDir(DIRECTORY_LOGS);
        } else {
            outputDir = new File(context.getExternalFilesDir(null),
                    "logs");
        }

        if (outputDir == null) {
            // 外部ストレージがマウントされていない等の場合
            return null;
        }

        boolean isExist = true;

        if (!outputDir.exists()
                || !outputDir.isDirectory()) {
            isExist = outputDir.mkdirs();
        }

        if (isExist) {
            return outputDir;

        } else {
            // ディレクトリの作成に失敗した場合
            return null;
        }
    }

    // 出力先ファイルを取得する
    private static File getFileName(Context context, File outputDir){

        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(APPLOG_FILE_FORMAT);

        String fileName = sdf.format(now.getTime())+APPLOG_FILE_EXT;//String.format(APPLOG_FILE_FORMAT, now);

        return new File(outputDir, fileName);
    }
    private static boolean writeToFile(File outputFile, String log){
        FileWriter writer = null;
        try {
            writer = new FileWriter(outputFile, true);
            writer.write(log);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
    // ログデータを作成する
    private static String makeLogData(Context context, String kind, String data){
        // ShraedPreferencesから取得
        String serverIp = String.valueOf(SettingPrefUtil.getServerIpAddress(context));
        String serverPort = String.valueOf(SettingPrefUtil.getServerPort(context));

        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);

        String datetime = sdf.format(now.getTime());
        String ip;
        if(kind.equals("S")){
            ip = serverIp + ":" + serverPort;
        }else{
            ip = serverIp + ":*";
        }

        String[] log = {datetime, kind, ip, data};
        String separator = ",";
        String nl = "\r\n";

        StringBuilder sb = new StringBuilder();
        for (String str : log) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(str);
        }
        return sb.toString()+nl;
    }

}
