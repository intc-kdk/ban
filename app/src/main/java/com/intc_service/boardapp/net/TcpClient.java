package com.intc_service.boardapp.net;

import android.content.Context;

import com.intc_service.boardapp.AppLogRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * タブレット発呼のソケット通信を行うクラス
 */

public class TcpClient {
    private String mSendData;
    private String mHost;
    private int mPort;
    private Context mContext;
    private Socket connection = null;

    private final int timeout = 3000;
    private final int retry = 2;

    public TcpClient(Context context, String host, int port, String data)
    {
        this.mHost = host;
        this.mPort = port;
        this.mSendData = data;
        this.mContext = context;
    }

    public String connect() {
        int sendCnt=0;
        String message="";
        while(sendCnt<retry){
            message = this.transport();
            if(message.indexOf("Exception") < 0){
                return message;
            }
            sendCnt++;
        }

        return message;
    }
    private String transport() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String message = "";

        try {
            //ソケット
            InetSocketAddress endpoint= new InetSocketAddress(mHost, mPort);
            connection = new Socket();
            connection.connect(endpoint, timeout);
            connection.setSoTimeout(timeout);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            //リクエスト
            writer.write(mSendData);
            writer.flush();

            AppLogRepository.create(mContext,"S",mSendData);
            System.out.println("<< サーバーへ送信 >>"+mSendData);

            //レスポンス
            int result;
            StringBuilder builder = new StringBuilder();
            while((result = reader.read()) != -1 ){
                builder.append((char)result);
                if(result == 36) {
                    break;
                }
            }
            message=builder.toString();
            AppLogRepository.create(mContext,"R",message);
            System.out.println("<< サーバーから受信 >>"+message);
            if(message.length() == 0 || message.indexOf("$") < 0 ){
                // 受信サイズ0
                System.out.println("Recieved illegal data");
                message = "91@Recieved illegal data$";
                AppLogRepository.create(mContext,"E",message);
            }
        } catch ( SocketException e) {
            System.out.println("SocketException error");
            message = "91@SocketException error: " + e.getMessage()+"$";
            AppLogRepository.create(mContext,"E",message);
            e.printStackTrace();
        } catch ( SocketTimeoutException e) {
            System.out.println("SocketTimeoutException error");
            message = "92@SocketTimeoutException error: " + e.getMessage()+"$";
            AppLogRepository.create(mContext,"E",message);
            e.printStackTrace();
        } catch (IOException  e) {
            System.out.println(message);
            message = "93@IOException error: " + e.getMessage()+"$";
            AppLogRepository.create(mContext,"E",message);
            e.printStackTrace();
        } finally {
            try{
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
    public void disconnect() {
        try{
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
