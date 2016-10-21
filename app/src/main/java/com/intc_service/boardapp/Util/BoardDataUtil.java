package com.intc_service.boardapp.Util;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by takashi on 2016/10/20.
 */

public class BoardDataUtil {

    public static BoardItem toList(Bundle row) {
        return new BoardItem( row.getString("tx_lb"),row.getString("tx_clr"), row.getString("in_disp_hi"), row.getString("in_disp_blink"));
    }
    /**
     * 機器情報のクラス
     */
    public static class BoardItem {
        public final String tx_lb;
        public String tx_clr;
        public String in_disp_hi;
        public String in_disp_blink;

        public BoardItem(String tx_lb ,String tx_clr ,String in_disp_hi ,String in_disp_blink){
            this.tx_lb = tx_lb;
            this.tx_clr = tx_clr;
            this.in_disp_hi = in_disp_hi;
            this.in_disp_blink = in_disp_blink;
        }
    }
}
