package com.intc_service.boardapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intc_service.boardapp.Util.SettingPrefUtil;
import com.intc_service.boardapp.loader.SendRequestLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransmissionFragment.TransmissionFragmentListener} interface
 * to handle interaction events.
 * Use the {@link TransmissionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransmissionFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>{
    private static final String ARG_HOST = "host";
    private static final String ARG_PORT = "port";

    private String mHost;
    private int mPort;
    private String localHost;
    private int localPort;

    private TransmissionFragmentListener mListener;

    public TransmissionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TransmissionFragment.
     */
    public static TransmissionFragment newInstance() {
        TransmissionFragment fragment = new TransmissionFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ShraedPreferencesから取得
        Context context = getActivity();
        mHost = SettingPrefUtil.getServerIpAddress(context);
        mPort = SettingPrefUtil.getServerPort(context);
        localHost = SettingPrefUtil.getClientIpAddress(context);
        localPort = SettingPrefUtil.getClientPort(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        return textView;
    }

    @Override
    public void onAttach(Context context) {
        // 発火しない？
        super.onAttach(context);
        if (context instanceof TransmissionFragmentListener) {
            mListener = (TransmissionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TransmissionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        // 発火しない？
        super.onDetach();
        mListener = null;
    }

    /* Loader */
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        if(args != null) {
            String data = args.getString("Data");
            String host = args.getString("Host");
            int port = args.getInt("Port");
            return  new SendRequestLoader(getActivity(), host, port, data);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        int id = loader.getId();

        getLoaderManager().destroyLoader(id);  // ローダーを破棄

        ((TransmissionFragmentListener)getActivity()).onResponseRecieved(data);  // Activity event

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public void send(String data){
        if(data != null) {
            Bundle args = new Bundle();
            args.putString("Data",data);
            args.putString("Host",mHost);
            args.putInt("Port",mPort);
            // Loaderを初期化する
            getLoaderManager().restartLoader(1, args, this);  // onCreateLoaderが呼ばれる
        }
    }
    public void halt(String data){
        if(data != null) {
            Bundle args = new Bundle();
            args.putString("Data",data);
            args.putString("Host",localHost);
            args.putInt("Port",localPort);
            // Loaderを初期化する
            getLoaderManager().restartLoader(1, args, this);  // onCreateLoaderが呼ばれる
        }
    }
    /**

     */
    public interface TransmissionFragmentListener {
        void onResponseRecieved(String data);
        void onFinishTransmission(String data);
    }

    /* Loader */
}
