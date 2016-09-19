package com.kevin.the3dprinterdemo.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Administrator
 * on 2016/9/18.
 */
public class TcpHelper {

    private static final String TAG = "TcpHelper";
    private static TcpHelper singleton;
    private OnTcpReceive onTcpReceive;
    private OnConnectListener onConnectListener;
    private Context mContext;
    private SpHelper spHelper;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private SocketAddress socketAddress;
    private Thread socketThread;
    private boolean isWhile = true;
    private byte[] receiveData;
    private String buffer;
    private MyHandler myHandler;
    private String sendData;

    private TcpHelper(Context context) {
        mContext = context;
        spHelper = SpHelper.getSingleton(context);
        initSocket();
        myHandler = new MyHandler(Looper.getMainLooper());
    }

    public static TcpHelper getSingleton(Context context) {
        if (singleton == null) {
//            synchronized (TcpHelper.class) {
//                if (singleton == null) {
            singleton = new TcpHelper(context);
//                }
//            }
        }
        return singleton;
    }

    private void initSocket() {

        socketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                socket = new Socket();

                socketAddress = new InetSocketAddress(spHelper.getSpIp(), Integer.parseInt(spHelper.getSpPort()));
                try {
                    socket.connect(socketAddress, 1000);

                    if (onConnectListener != null) {
                        onConnectListener.isConnect(true);
                    }
                    os = socket.getOutputStream();
                    is = socket.getInputStream();
                    receive();
                } catch (IOException e) {
                    if (onConnectListener != null) {
                        onConnectListener.isConnect(false);
                    }
                    e.printStackTrace();
                }
            }
        });
        socketThread.start();

    }

    public void receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {

//                BufferedReader bff = new BufferedReader(new InputStreamReader(is));
//                String line;
//                buffer="";
//                try {
//                    while ((line = bff.readLine()) != null) {
//                        buffer = line + buffer;
//                    }
//
//                    Log.e(TAG,"接收的数据"+buffer.trim());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                while(isWhile){
//                    try {
//                        receiveData = new byte[512];
//                        is.read(receiveData);
//                        if(is.available()!=0){
//                           if( is.read(receiveData)!=-1){
//                               if(onTcpReceive!=null){
//                                   onTcpReceive.onReceive(receiveData);
//                               }
//
//                           }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }


//                try {
//                    while (isWhile) {
//                        receiveData = new byte[512];
//                        if(is.read(receiveData) != -1){
//
//                            if (onTcpReceive != null) {
//                                onTcpReceive.onReceive(receiveData);
//                            }
//                        }
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


                while (isWhile) {
                    int count = 0;
                    try {
                        while (count == 0) {
                            count = is.available();
                        }
                        receiveData = new byte[count];
                        int re = is.read(receiveData);
                        if (onTcpReceive != null) {
                            onTcpReceive.onReceive(receiveData);
                        }
//                        Message msg = new Message();
//                        msg.what = 1;
//                        Bundle data = new Bundle();
//                        data.putByteArray("msg",receiveData);
//                        msg.setData(data);
//                        myHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

    public void send(String data) {

        sendData = data;
        Log.e(TAG, "CMD=" + sendData + " cmdLength=" + sendData.length());
        try {
            os.write(sendData.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void close() {
        try {
            isWhile = false;
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }

            if (socket != null) {
                socket.close();
            }
            singleton = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class MyHandler extends Handler {

        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

//                Log.e(TAG, msg.getData().getString("msg"));
//                Utils.showInfo(mContext, msg.getData().getString("msg"));
                byte[] result = (byte[]) msg.getData().get("msg");
                if (onTcpReceive != null) {
                    onTcpReceive.onReceive(result);
                }

            }

        }
    }

    public void setOnConnectListener(OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    public void setOnTcpReceive(OnTcpReceive onTcpReceive) {
        this.onTcpReceive = onTcpReceive;
    }


}
