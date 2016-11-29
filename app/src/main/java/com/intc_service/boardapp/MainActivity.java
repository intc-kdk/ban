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
import com.intc_service.boardapp.Util.alertDialogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private Bundle bdRecievedData = new Bundle();

    private String mBname;
    private Bundle mBoardinfo = new Bundle();
    private String mTag="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // 盤ステータスからの戻りかどうか確認
        Intent intent = getIntent();
        String recievedData = "";
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Iterator<?> iterator = extras.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.equals("cmd71")) {
                    recievedData = extras.getString("cmd71");
                    break;
                }
            }
        }
        if (recievedData.isEmpty()) {
            // [P] 起動電文を作成
            DataStructureUtil ds = new DataStructureUtil();
            String mData = ds.makeSendData("30", "");
            // [P] 起動通知を送信
            sendFragment.send(mData);
        } else {
            // 場所選択画面表示
            onResponseRecieved(recievedData);
        }

        recieveFragment.listen();
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

        if(cmd.equals("71")) { //起動応答
            if (bdRecievedData.getString("format").equals("JSON")) {
                // 場所一覧を作成
                mBoardinfo = bdRecievedData;
                setTableRows(bdRecievedData.getBundle("盤情報"));
            }
        }else if(cmd.equals("72")){ //機器情報
            if(bdRecievedData.getString("format").equals("JSON")) {
                ArrayList arrEquip = (ArrayList)bdRecievedData.getParcelableArrayList("m_device"); //機器情報を取り出す
                mBname = bdRecievedData.getString("tx_bname");
                mBoardinfo = bdRecievedData;
                sendFragment.halt("99@$");  // 待ち受けを停止する

            }
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("99")) {  // サーバークローズ
            recieveFragment.closeServer(); //待ち受けを中止する。
            if(mTag.isEmpty()){
                // 盤ステータス画面へ
                Intent intent = new Intent(this, StatusActivity.class);
                intent.putExtra("bname", mBname);
                intent.putExtra("boardinfo", mBoardinfo);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this,BoardActivity.class);
                // 盤選択画面へ
                intent.putExtra("key",mTag);
                intent.putExtra("info",mBoardinfo.getBundle("盤情報"));

                startActivity(intent);
            }

        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
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
        mTag = (String)v.getTag();

        sendFragment.halt("99@$"); // 受信待機を破棄して、次画面へ
        /*Intent intent = new Intent(this,BoardActivity.class);
        // 盤選択画面へ
        intent.putExtra("key",tag);
        intent.putExtra("info",bdRecievedData.getBundle("盤情報"));

        startActivity(intent);*/

    }

    @Override
    public String onRequestRecieved(String data) {
        // サーバーからの要求（data）を受信
        //System.out.println("ReqRecieved:"+data);
        String mData = "";
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("9C")) {  // 電源OFF画面 onFinishRecieveProgress で処理
            mData = "50@$";
        } else if (cmd.equals("99")) {
            mData = "99@$";
        } else if (cmd.equals("91")) {  // 受信エラー処理 onFinishRecieveProgress で処理
            mData = "";
        } else if (cmd.equals("92")) {  // タイムアウト onFinishRecieveProgress で処理
            mData = "";
        }

        return mData;
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        // サーバー発呼のコマンド送受信後の処理
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("99")) { // accept キャンセル
            // ここでは何もせず、応答の"99"受信で処理
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else {
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        }
    }
}
