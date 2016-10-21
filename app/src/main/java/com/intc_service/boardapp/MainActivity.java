package com.intc_service.boardapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.intc_service.boardapp.Util.DataStructureUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private Bundle bdRecievedData = new Bundle();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [P] 起動電文を作成
        DataStructureUtil ds = new DataStructureUtil();
        String mData = ds.makeSendData("30","");

        // [P] 起動を通知
        // TransmissionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // [P] 起動通知を送信
        sendFragment.send(mData);
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("71")){ //起動応答
            if(bdRecievedData.getString("format").equals("JSON")) {
                // 場所一覧を作成
                setTableRows(bdRecievedData.getBundle("盤情報"));
            }
        }

        // TODO: [P] ログを取得

    }

    @Override
    public void onFinishTransmission(String data) {

    }

    /**/

    private void setTableRows(Bundle bdRows){
        Set keys = bdRows.keySet();
        List<String> stringKeys = new ArrayList<>();
        for (Object key : keys) {
            stringKeys.add(key.toString());
        }
        Collections.sort(stringKeys);

        // TableLayoutのグループを取得
        ViewGroup vg = (ViewGroup)findViewById(R.id.TableLayout);

        for (int i=0; i < stringKeys.size(); i++) {
            // 行を追加
            getLayoutInflater().inflate(R.layout.location_row, vg);
            // 文字設定
            TableRow tr = (TableRow)vg.getChildAt(i);
            String str = stringKeys.get(i);
            // 場所名
            if(str.equals("blank")) { // 場所名が ブランクの時の処理
                ((TextView) (tr.getChildAt(0))).setText("");
            }else{
                ((TextView) (tr.getChildAt(0))).setText(str);
            }
            // ボタン
            ViewGroup fl =(FrameLayout)(tr.getChildAt(1));
            ((Button)fl.getChildAt(0)).setOnClickListener(this);
            ((Button)fl.getChildAt(0)).setTag(str);

        }

    }
    public void onClick(View v){
        String tag = (String)v.getTag();

        Intent intent = new Intent(this,BoardActivity.class);
        // 盤選択画面へ
        intent.putExtra("key",tag);
        intent.putExtra("info",bdRecievedData.getBundle("盤情報"));

        startActivity(intent);

    }
}
