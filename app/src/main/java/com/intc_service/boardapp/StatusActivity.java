package com.intc_service.boardapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intc_service.boardapp.Util.BoardDataUtil;
import com.intc_service.boardapp.Util.BoardDataUtil.BoardItem;
import com.intc_service.boardapp.Util.DataStructureUtil;
import com.intc_service.boardapp.Util.alertDialogUtil;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        StatusFragment.OnListFragmentInteractionListener, View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final int REQUEST_CODE_OPERATION = 1;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private StatusFragment mStatusFragment;

    private String mRecievedData;
    private boolean noTap;
    private Button mBtnUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //  機器一覧フラグメントの取得
        mStatusFragment = (StatusFragment)getSupportFragmentManager()
                .findFragmentById(R.id.StatusList);

        // リスナーの設定
        Button btnReturn = (Button) findViewById(R.id.btnReturnBoard);
        btnReturn.setOnClickListener(this);

        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        mBtnUpdate.setOnClickListener(this);
        mBtnUpdate.setVisibility(View.INVISIBLE);
        noTap=true;

        // 機器データをIntentから取得
        Intent intent = getIntent();

        // ヘッダ設定
        String tx_bname = intent.getStringExtra("bname");
        TextView mLabelView = (TextView)findViewById(R.id.bname);
        mLabelView.setText(tx_bname);

        Bundle bdBoardInfo = intent.getBundleExtra("boardinfo");
        String bo_active = bdBoardInfo.getString("bo_active");

        //　背景色の設定
        setBackground(bo_active);

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // サーバーからの指示を待機
        recieveFragment.listen();

    }

    private void setBackground(String status){
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.activity_status);
        Resources res = getResources();
        if(status.equals("True")){
            rl.setBackgroundColor(res.getColor(R.color.colorBackgroundActivate));
        }else{
            rl.setBackgroundColor(res.getColor(R.color.colorBackgroundDefault));
        }

    }

    // サーバーからの送信
    @Override
    public String onRequestRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData = "";
        if(cmd.equals("73")) { //機器情報
            mData = dsHelper.makeSendData("50", "");
        }else if (cmd.equals("9C")) {  // 電源OFF画面 onFinishRecieveProgress で処理
            mData = "50@$";
        }else if(cmd.equals("99")){
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
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        mBtnUpdate.setVisibility(View.INVISIBLE);
        if(cmd.equals("73")) { //機器情報
            if (bdRecievedData.getString("format").equals("JSON")) {
                String bo_active = bdRecievedData.getString("bo_active");
                ArrayList arrEquip = (ArrayList) bdRecievedData.getParcelableArrayList("m_device"); //機器情報を取り出す

                //　背景色の設定
                setBackground(bo_active);
                //　機器情報の更新
                updateEquipments(arrEquip);
                // 受信待機
                recieveFragment.listen();
            }
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        }else if(cmd.equals("99")) { // accept キャンセル
            // ここでは何もせず、応答の"99"受信で処理

        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, null,  getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            mBtnUpdate.setVisibility(View.VISIBLE);
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            mBtnUpdate.setVisibility(View.VISIBLE);
            recieveFragment.listen();
        }else{
            recieveFragment.listen();
        }

    }
    public void onClick(View v){
        int id = v.getId();
        String mData ="";
        DataStructureUtil ds = new DataStructureUtil();
        switch (id){
            case R.id.btnReturnBoard:
                mData = ds.makeSendData("32", "");
                // [P] 盤再選択コマンドを送信
                sendFragment.send(mData);
                break;
            case R.id.btn_update: // 画面更新ボタンクリック
                if(noTap) {
                    // サーバーへ画面更新[90]送信
                    mData = ds.makeSendData("90", "");
                    sendFragment.send(mData);
                    noTap=false;
                }
                break;
        }
    }

    private void updateEquipments(ArrayList data){

        List<BoardItem> ITEMS = new ArrayList<>();
        for (Object value : data) {
            Bundle row = (Bundle) value;  // Bundleの入れ子なのでキャスト
            // ラベル、カラーが空白の行は無視
            if(!row.getString("tx_lb").isEmpty() && !row.getString("tx_clr").isEmpty()) {
                ITEMS.add(BoardDataUtil.toList(row));
            }
        }

        mStatusFragment.updateStatus(ITEMS);
    }

    // 応答受信
    @Override
    public void onResponseRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        // コマンド[99]応答受信
        if(cmd.equals("71")) {  // 場所、盤名情報受信
            mRecievedData=data;
            sendFragment.halt("99@$");  // 待ち受け終了コマンド送信
        }else if(cmd.equals("99")) {  // コマンド[99]応答受信
            recieveFragment.closeServer(); //待ち受けを中止する。
            // 盤選択に戻る
            Intent intent;
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("cmd71",mRecievedData);  // 71（場所、盤データをセットし、場所選択画面へ）
            startActivity(intent);
        }else if (cmd.equals("9N")) {  // 画面更新（正常）
            noTap = true;
        }else if (cmd.equals("9Q")) {  // 画面更新（異常）
            noTap = true;
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment,  getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment,  getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }
    }

    @Override
    public void onFinishTransmission(String data) {

    }
    @Override
    public void onListFragmentInteraction(BoardItem item) {

    }
}
