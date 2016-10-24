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

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity
        implements ReceptionFragment.ReceptionFragmentListener,
        StatusFragment.OnListFragmentInteractionListener, View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final int REQUEST_CODE_OPERATION = 1;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private ReceptionFragment recieveFragment;
    private StatusFragment mStatusFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //  機器一覧フラグメントの取得
        mStatusFragment = (StatusFragment)getSupportFragmentManager()
                .findFragmentById(R.id.StatusList);

        // リスナーの設定
        Button btn = (Button) findViewById(R.id.btnReturnBoard);
        btn.setOnClickListener(this);

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

        // ReceptionFragment を　生成
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
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

    // サーバーからの一方送信
    @Override
    public String onRequestRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData = "";
        if(cmd.equals("73")) { //機器情報
            mData = dsHelper.makeSendData("50","");
        }
        return mData;
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("73")){ //機器情報
            if(bdRecievedData.getString("format").equals("JSON")) {
                String bo_active =bdRecievedData.getString("bo_active");
                ArrayList arrEquip = (ArrayList)bdRecievedData.getParcelableArrayList("m_device"); //機器情報を取り出す

                //　背景色の設定
                setBackground(bo_active);
                //　機器情報の更新
                updateEquipments(arrEquip);

            }
        }
        // 受信待機
        recieveFragment.listen();
    }
    public void onClick(View v){
        int id = v.getId();
        Intent intent;

        switch (id){
            case R.id.btnReturnBoard:
                //recieveFragment.onPause();
                //recieveFragment.closeServer();
                // 盤選択に戻る
                intent = new Intent(this,BoardActivity.class);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private void updateEquipments(ArrayList data){

        List<BoardItem> ITEMS = new ArrayList<>();
        for (Object value : data) {
            Bundle row = (Bundle) value;  // Bundleの入れ子なのでキャスト
            ITEMS.add(BoardDataUtil.toList(row));
        }
        mStatusFragment.updateStatus(ITEMS);
    }
    // 応答受信
/*    @Override
    public void onResponseRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("72")){ //機器情報
            if(bdRecievedData.getString("format").equals("JSON")) {
                ArrayList arrEquip = (ArrayList)bdRecievedData.getParcelableArrayList("m_device"); //機器情報を取り出す

                for (Object value : arrEquip) {
                    Bundle row = (Bundle) value;  // Bundleの入れ子なのでキャスト
                    System.out.print("tx_lb:"+row.getString("tx_lb"));
                    System.out.print(" tx_clr:"+row.getString("tx_clr"));
                    System.out.print(" in_disp_hi:"+row.getString("in_disp_hi"));
                    System.out.println(" tx_blink:"+row.getString("in_disp_blink"));

                }
            }
        }
    }

*/
    @Override
    public void onListFragmentInteraction(BoardItem item) {

    }

}
