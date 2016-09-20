package com.kevin.the3dprinterdemo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kevin.the3dprinterdemo.R;
import com.kevin.the3dprinterdemo.utils.OnTcpReceive;
import com.kevin.the3dprinterdemo.utils.SpHelper;
import com.kevin.the3dprinterdemo.utils.TcpHelper;

public class PrintActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnStartPrint;
    private Button btnStopPrint;
    private Button btnClearLog;
    private TextView tvShowPrintLog;
    private TextView tvFileName;
    private TcpHelper tcpHelper;
    private String rec;
    private SpHelper spHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btnStartPrint = (Button)findViewById(R.id.btnStartPrint);
        btnStopPrint = (Button)findViewById(R.id.btnStopPrint);
        btnClearLog = (Button)findViewById(R.id.btnClearLog);
        tvShowPrintLog = (TextView)findViewById(R.id.tvShowPrintLog);
        tvFileName = (TextView)findViewById(R.id.tvFileName);
        btnStartPrint.setOnClickListener(this);
        btnStopPrint.setOnClickListener(this);
        btnClearLog.setOnClickListener(this);

        tcpHelper = TcpHelper.getSingleton(this);
        spHelper = SpHelper.getSingleton(this);
        tvFileName.setText(spHelper.getSpPrintFile());
        tcpHelper.setOnTcpReceive(new OnTcpReceive() {
            @Override
            public void onReceive(byte[] receive) {
                rec = asciiToString(receive);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvShowPrintLog.append(rec + "\n");
                    }
                });

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStartPrint:
                Log.e("dsdas","asdasdasd");
                if(spHelper.getSpPrintFile()!=null){
                    if(tcpHelper==null){
                        tcpHelper = TcpHelper.getSingleton(this);
                    }
                    tcpHelper.send("play "+spHelper.getSpPrintFile()+"\r\n");
                }

                break;
            case R.id.btnStopPrint:
                Log.e("stop","stopPrint");
                if(tcpHelper==null){
                    tcpHelper = TcpHelper.getSingleton(this);
                }
                tcpHelper.send("abort \r\n");
                break;
            case R.id.btnClearLog:
                tvShowPrintLog.setText("");
                break;
        }

    }

    private String asciiToString(byte[] da){
        StringBuilder sb = new StringBuilder();
        for (byte aDa : da) {

            if ((int)aDa != 13) {
                sb.append((char) aDa);
            }
        }
//        Log.e(TAG, "sb.toString()=" + sb.toString().trim());
        return sb.toString().trim();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


}

