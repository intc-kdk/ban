package com.intc_service.boardapp.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * タブレット発呼のソケット通信を行うクラス
 */

public class TcpClient {
    private String mSendData;
    private String mHost;
    private int mPort;
    private Socket connection = null;

    public TcpClient(String host, int port, String data)
    {
        this.mHost = host;
        this.mPort = port;
        this.mSendData = data;
    }

    public String connect() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String message = "";

        try {
            //ソケット
            connection = new Socket(mHost, mPort);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            //リクエスト
            writer.write(mSendData);
            writer.flush();
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
System.out.println("<< サーバーから受信 >>"+message);
        } catch (IOException e) {
            message = "IOException error: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            message = "Exception: " + e.getMessage();

        } finally {
            try{
                reader.close();
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
