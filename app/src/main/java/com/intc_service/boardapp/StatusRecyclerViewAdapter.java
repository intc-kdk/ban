package com.intc_service.boardapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intc_service.boardapp.Util.BoardDataUtil.BoardItem;
import com.intc_service.boardapp.StatusFragment.OnListFragmentInteractionListener;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BoardItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
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
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mLabelView.setText(mValues.get(position).tx_lb);

        Resources res = holder.mView.getResources();

        int btnColor = getColorInt(holder.mItem.tx_clr);
        holder.mLabelView.setBackgroundColor(btnColor);

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

        public BoardItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLabelView = (TextView) view.findViewById(R.id.label);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLabelView.getText() + "'";
        }
    }
}
