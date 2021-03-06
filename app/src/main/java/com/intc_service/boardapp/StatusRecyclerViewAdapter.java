package com.intc_service.boardapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.intc_service.boardapp.Util.BoardDataUtil.BoardItem;
import com.intc_service.boardapp.StatusFragment.OnListFragmentInteractionListener;


import java.util.Iterator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BoardItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class StatusRecyclerViewAdapter extends RecyclerView.Adapter<StatusRecyclerViewAdapter.ViewHolder> {

    private final List<BoardItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public StatusRecyclerViewAdapter(List<BoardItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_status, parent, false);
        return new ViewHolder(view);
    }

    private int getColorInt(String code){
        if(code.equals("")) return 0;
        //  色指定（16進 から 10進）
        int color = Color.rgb(
                Integer.valueOf( code.substring( 0, 2 ), 16 ),
                Integer.valueOf( code.substring( 2, 4 ), 16 ),
                Integer.valueOf( code.substring( 3, 6 ), 16 ) );
        return color;
    }
    private void blinkButton(ViewHolder holder) {

        // ボタンのブリンク処理
        Resources res = holder.mView.getResources();
        AlphaAnimation alphaWrap = new AlphaAnimation(0f, 1f);
        alphaWrap.setDuration(1500);
        alphaWrap.setRepeatCount(Animation.INFINITE);
        alphaWrap.setRepeatMode(Animation.RESTART);
        alphaWrap.setInterpolator(new CycleInterpolator(1));
        int bgWrapColor = res.getColor(R.color.colorBgTransparent);

        if (holder.mItem.in_disp_hi.equals("1")) {
            bgWrapColor = res.getColor(R.color.colorTextBlack);
            holder.mWrapFrame.setBackgroundColor(bgWrapColor);
            holder.mWrapFrame.startAnimation(alphaWrap);
        } else {
            holder.mWrapFrame.setBackgroundColor(bgWrapColor);
            alphaWrap.cancel();
        }

        AlphaAnimation alphaButton = new AlphaAnimation(0f, 1f);
        alphaButton.setDuration(1500);
        alphaButton.setRepeatCount(Animation.INFINITE);
        alphaButton.setRepeatMode(Animation.RESTART);
        alphaButton.setInterpolator(new CycleInterpolator(1));

        int btnColor = getColorInt(holder.mItem.tx_clr);
        ColorDrawable color_drawable = (ColorDrawable) holder.mLabelView.getBackground();
        if(Integer.toHexString(holder.mBeforeColor).equals("ffffffff")){
            // 初回
            holder.mBeforeColor = btnColor;
        }else if(holder.mBeforeColor==0){
            holder.mBeforeColor = color_drawable.getColor();
        }
        if(holder.mItem.in_disp_blink.equals("1")){
            // 下地のボタンに前の色を付ける

            int btnBeforeColor = holder.mBeforeColor;

            //if(Integer.toHexString(btnBeforeColor).equals("ffcccccc")){
                btnBeforeColor = res.getColor(R.color.colorTextBlack);
            //}
            holder.mLabelBefore.setBackgroundColor(btnBeforeColor);
            holder.mLabelView.startAnimation(alphaButton);
        }else{
            holder.mLabelBefore.setBackgroundColor(bgWrapColor);
            alphaButton.cancel();
        }
        holder.mLabelView.setBackgroundColor(btnColor);
    }
    private void setButtonSize(ViewHolder holder){
        Iterator<BoardItem> i = mValues.iterator();
        int cnt=0;
        while(i.hasNext()){
            BoardItem item = i.next();
            //ステータスが"1"（確認中）in_snoを返す
            if(!item.tx_lb.equals("") && !item.tx_clr.equals("")){
                cnt++;
            }
        }
        // サイズはpixel指定
        switch (cnt){
            case 1:
                holder.mLabelView.setHeight(900);
                holder.mLabelView.setTextSize(80);
                holder.mLabelBefore.setHeight(900);
                holder.mLabelBefore.setTextSize(80);
                break;
            case 2:
                holder.mLabelView.setHeight(600);
                holder.mLabelView.setTextSize(70);
                holder.mLabelBefore.setHeight(600);
                holder.mLabelBefore.setTextSize(70);
                break;
            case 3:
                holder.mLabelView.setHeight(410);
                holder.mLabelView.setTextSize(65);
                holder.mLabelBefore.setHeight(410);
                holder.mLabelBefore.setTextSize(65);
                break;
            case 4:
                holder.mLabelView.setHeight(290);
                holder.mLabelView.setTextSize(65);
                holder.mLabelBefore.setHeight(290);
                holder.mLabelBefore.setTextSize(65);
                break;
            default:
                holder.mLabelView.setHeight(225);
                holder.mLabelView.setTextSize(50);
                holder.mLabelBefore.setHeight(225);
                holder.mLabelBefore.setTextSize(50);
                break;
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mLabelView.setText(mValues.get(position).tx_lb);
        holder.mLabelBefore.setText(mValues.get(position).tx_lb);

        setButtonSize(holder);
        blinkButton(holder);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mLabelView;
        public final TextView mLabelBefore;
        public FrameLayout mWrapFrame;
        public BoardItem mItem;
        public int mBeforeColor = 0;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLabelView = (TextView) view.findViewById(R.id.label);
            mLabelBefore = (TextView) view.findViewById(R.id.label_before);
            mWrapFrame = (FrameLayout) view.findViewById(R.id.wrap_frame);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLabelView.getText() + "'";
        }
    }

    public void updateStatus(List<BoardItem> data){
        for(int i=0; i<data.size(); i++){
            mValues.get(i).tx_lb = data.get(i).tx_lb;
            mValues.get(i).tx_clr = data.get(i).tx_clr;
            mValues.get(i).in_disp_blink = data.get(i).in_disp_blink;
            mValues.get(i).in_disp_hi = data.get(i).in_disp_hi;
        }

    }
}
