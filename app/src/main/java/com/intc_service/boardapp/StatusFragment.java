package com.intc_service.boardapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intc_service.boardapp.Util.BoardDataUtil;
import com.intc_service.boardapp.Util.BoardDataUtil.BoardItem;
import com.intc_service.boardapp.dummy.DummyContent;
import com.intc_service.boardapp.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class StatusFragment extends Fragment {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private StatusRecyclerViewAdapter mStatusViewAdapter;
    private List<BoardItem> ITEMS = new ArrayList<>();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StatusFragment() {
    }

    @SuppressWarnings("unused")
    public static StatusFragment newInstance(int columnCount) {
        StatusFragment fragment = new StatusFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 手順データを取得
        Intent intent = getActivity().getIntent();
        Bundle bdBoard = intent.getBundleExtra("boardinfo");
        ArrayList arrDevice = (ArrayList)bdBoard.getParcelableArrayList("m_device"); //機器情報を取り出す
        for (Object value : arrDevice) {
            Bundle row = (Bundle) value;  // Bundleの入れ子なのでキャスト
            ITEMS.add(BoardDataUtil.toList(row));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new StatusRecyclerViewAdapter(ITEMS, mListener));

            // Adapterへの参照
            mStatusViewAdapter = (StatusRecyclerViewAdapter)recyclerView.getAdapter();
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(BoardItem item);
    }

    public void updateStatus(List<BoardItem> data){
        List<BoardItem> Items = new ArrayList<>();
        Items = data;
        mStatusViewAdapter.updateStatus(Items);
        mStatusViewAdapter.notifyDataSetChanged();
    }

}
