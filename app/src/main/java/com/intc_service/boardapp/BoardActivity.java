package com.intc_service.boardapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.intc_service.boardapp.Util.DataStructureUtil;
import com.intc_service.boardapp.Util.alertDialogUtil;

public class BoardActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String selectedBoard;

    private String mBname;
    private Bundle mBoardinfo = new Bundle();

    private int mBtnId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        // リスナーの設定
        Button btn = (Button) findViewById(R.id.btnReturnMain);
        btn.setOnClickListener(this);

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        recieveFragment.listen();

        // 盤情報をIntentから取得
        Intent intent = getIntent();
        String key = intent.getStringExtra("key");
        Bundle boardInfo = intent.getBundleExtra("info");

        // テーブル表示
        // TableLayoutのグループを取得
        ViewGroup vg = (ViewGroup)findViewById(R.id.TableLayout);
        if(boardInfo.getBundle(key) instanceof Bundle){
            // 盤の構成が1台の時
            setTableRowsBundle(vg, boardInfo.getBundle(key),0);
        }else {
            ArrayList arrBoard = (ArrayList) boardInfo.getParcelableArrayList(key);
            setTableRows(vg, arrBoard);
        }

    }
    private void setTableRowsBundle(ViewGroup vg, Bundle row, int i){
        // 行を追加
        getLayoutInflater().inflate(R.layout.board_row, vg);

        // 文字設定
        TableRow tr = (TableRow)vg.getChildAt(i);
        String tx_bname = row.getString("tx_bname");
        String in_bno = row.getString("in_bno");
        String tx_rip = row.getString("tx_rip");

        ((TextView)(tr.getChildAt(0))).setText(tx_bname);
        ((TextView)(tr.getChildAt(0))).setTag(in_bno);
        ViewGroup fl =(FrameLayout)(tr.getChildAt(1));
        if(tx_rip.isEmpty()) {  // リモートIPが空のとき、ボタン表示
            ((Button)fl.getChildAt(0)).setVisibility(View.VISIBLE);
            ((Button)fl.getChildAt(0)).setOnClickListener(this);
            ((Button)fl.getChildAt(0)).setTag(i);
            ((TextView)fl.getChildAt(1)).setVisibility(View.INVISIBLE);
        }else{
            ((Button)fl.getChildAt(0)).setVisibility(View.INVISIBLE);
            ((TextView)fl.getChildAt(1)).setVisibility(View.VISIBLE);
        }

    }

    private void setTableRows(ViewGroup vg, ArrayList bdRows){

        int i = 0;
        for (Object value : bdRows) {
            Bundle row = (Bundle) value;  // Bundleの入れ子なのでキャスト

            setTableRowsBundle(vg, row, i);
            i++;

        }

    }

    public void onClick(View v){
        int id = v.getId();
        Intent intent;

        switch (id){
            case R.id.btnReturnMain:
                mBtnId=R.id.btnReturnMain;
                sendFragment.halt("99@$");
                // 場所選択に戻る
                /*intent = new Intent(this,MainActivity.class);
                startActivity(intent);*/
                break;
            default:
                int row = (int)v.getTag();
                ViewGroup vg = (ViewGroup)findViewById(R.id.TableLayout);
                String in_bno ="";
                for(int i = 0; i < vg.getChildCount(); i++) {
                    if(i == row){
                        TableRow tr = (TableRow)vg.getChildAt(i);
                        in_bno =(String)((TextView)(tr.getChildAt(0))).getTag();
                        selectedBoard=(String)((TextView)(tr.getChildAt(0))).getText();

                    }
                }
                // [P] 機器情報要求電文を作成
                DataStructureUtil ds = new DataStructureUtil();
                String mData = ds.makeSendData("31","{\"in_bno\":\""+in_bno+"\"}");
                sendFragment.send(mData);

                break;
        }
    }

    // 応答電文
    @Override
    public void onResponseRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("72")){ //機器情報
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
            if(mBtnId == R.id.btnReturnMain){
                // 場所選択に戻る
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
            }else {
                // 盤ステータス画面へ
                Intent intent = new Intent(this, StatusActivity.class);
                intent.putExtra("bname", mBname);
                intent.putExtra("boardinfo", mBoardinfo);
                startActivity(intent);
            }

        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment,  getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }

    }

    @Override
    public void onFinishTransmission(String data) {

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
