package com.intc_service.boardapp.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.intc_service.boardapp.net.TcpClient;
/**
 * サーバーへ要求を送信するためのLoader
 */

public class SendRequestLoader extends AsyncTaskLoader<String>{
    private String mSendData;
    private String mHost;
    private int mPort;
    private String mData;
    // 結果
    //private byte[] mData;
    private TcpClient client;
    public SendRequestLoader(Context context, String host, int port, String senddata ){
        super(context);
        mHost = host;
        mPort = port;
        mSendData = senddata;
    }

    @Override
    public String loadInBackground() {
        client = new TcpClient(mHost, mPort, mSendData);

        return client.connect();
    }
    @Override
    public void deliverResult(String data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if(mData != null){
                mData = null;
            }
            return;
        }

        mData = data;

        if(isStarted()){
            super.deliverResult(data);
        }
    }


    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mData = null;
    }
    /*
    @Override
    protected void onStartLoading() {
        // ロード開始処理 結果のキャッシュなど判定する
        forceLoad();  // lodInBackgroudが実行される
    }
    @Override
    protected void onStopLoading() {
        // 停止処理
        client.disconnect();
    }
    @Override
    protected void onReset() {
        // リセット処理
    }*/
}