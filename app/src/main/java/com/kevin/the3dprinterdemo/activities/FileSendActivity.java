package com.kevin.the3dprinterdemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.the3dprinterdemo.MainActivity;
import com.kevin.the3dprinterdemo.R;
import com.kevin.the3dprinterdemo.utils.SpHelper;
import com.kevin.the3dprinterdemo.utils.Utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileSendActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnChooseFile;
    private Button btnSendFile;
    private TextView tvShowFilePath;
    private TextView tvShowStatus;
    private static final int FILE_SELECT_CODE = 0x123;
    private int percent;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_send);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btnChooseFile = (Button)findViewById(R.id.btnChooseFile);
        btnSendFile = (Button)findViewById(R.id.btnSendFile);
        tvShowFilePath = (TextView)findViewById(R.id.tvShowFilePath);
        tvShowStatus = (TextView)findViewById(R.id.tvShowStatus);

        btnChooseFile.setOnClickListener(this);
        btnSendFile.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnChooseFile:
                choiceFile();
                break;
            case R.id.btnSendFile:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendFile();
                    }
                }).start();

                break;
        }
    }


    private void choiceFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(FileSendActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendFile(){
        int length = 0;
        double sumL = 0 ;
        byte[] sendBytes = null;
        Socket socket = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        boolean bool = false;
        Log.e("SendFileActivity", "into");

        try {
            File file = new File(tvShowFilePath.getText().toString()); //要传输的文件路径
            long l = file.length();
            socket = new Socket();
            socket.connect(new InetSocketAddress(SpHelper.getSingleton(FileSendActivity.this).getSpIp(), 115));
            dos = new DataOutputStream(socket.getOutputStream());
            fis = new FileInputStream(file);
            sendBytes = new byte[1024];
            Log.e("SendFileActivity","len="+file.length());
            String cmd1 = "STOR OLD /sd/"+file.getName()+"\n";
            String cmd2 = "SIZE " + file.length() + "\n";
            String cmd3 = "DONE\n";
            byte[] cmdByte1 = cmd1.getBytes();
            byte[] cmdByte2 = cmd2.getBytes();
            byte[] cmdByte3 = cmd3.getBytes();
            dos.write(cmdByte1, 0, cmdByte1.length);
            dos.flush();
            dos.write(cmdByte2, 0, cmdByte2.length);
            dos.flush();
            while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                sumL += length;
                percent = (int)((sumL / l) *100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvShowStatus.setText("文件传输中：" + percent+"%");
                    }
                });

                Log.e("SendFileActivity","已传输："+((sumL/l)*100)+"%");
                dos.write(sendBytes, 0, length);
                dos.flush();
            }
            dos.write(cmdByte3, 0, cmdByte3.length);
            dos.flush();
            //虽然数据类型不同，但JAVA会自动转换成相同数据类型后在做比较
            if(sumL==l){
                bool = true;
            }
        }catch (Exception e) {
            Log.e("SendFileActivity", "客户端文件传输异常"+e.toString());
            bool = false;
            e.printStackTrace();
        } finally{
            try {
                if (dos != null)
                    dos.close();
                if (fis != null)
                    fis.close();
                if (socket != null)
                    socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Log.e("SendFileActivity", bool ? "成功" : "失败");
        data = bool ? "成功" : "失败";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvShowStatus.setText("文件传输" + data);
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = Utils.getPath(this, uri);
                    tvShowFilePath.setText(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
