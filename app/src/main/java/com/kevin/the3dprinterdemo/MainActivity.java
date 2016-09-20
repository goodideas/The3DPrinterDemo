package com.kevin.the3dprinterdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.the3dprinterdemo.activities.FileSendActivity;
import com.kevin.the3dprinterdemo.activities.PrintActivity;
import com.kevin.the3dprinterdemo.activities.SettingActivity;
import com.kevin.the3dprinterdemo.utils.OnConnectListener;
import com.kevin.the3dprinterdemo.utils.OnTcpReceive;
import com.kevin.the3dprinterdemo.utils.SdAdapter;
import com.kevin.the3dprinterdemo.utils.SpHelper;
import com.kevin.the3dprinterdemo.utils.TcpHelper;
import com.kevin.the3dprinterdemo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String LAST_PATH = "...";
    private static final int REQUEST_CODE_PRINT = 0x124;

    private TextView tvShowIp;
    private TextView tvShowPort;
    private Button btnConnect;
    private Button btnRoot;

    private ListView sdListView;
    private SpHelper spHelper;
    private List<String> sList = new ArrayList<>();
    private TcpHelper tcpHelper;
    private boolean connected = true;
    private SdAdapter sdAdapter;
    private String abPath = "/";
    private String receiveData = "";
    private String[] splits;
    private boolean isLsCmd = false;
    private String lastItem = "";
    private List<String> tempList;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvShowIp = (TextView) findViewById(R.id.tvShowIp);
        tvShowPort = (TextView) findViewById(R.id.tvShowPort);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        sdListView = (ListView) findViewById(R.id.sdListView);
        Utils.onSetIpAndPort(this, null, null, spHelper, tvShowIp, tvShowPort);
        spHelper = SpHelper.getSingleton(this);
        btnRoot = (Button) findViewById(R.id.btnRoot);

        btnRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abPath = "/";
                sList.clear();
                sList.add(LAST_PATH);
                sdAdapter.notifyDataSetChanged();
                if (tcpHelper != null) {
                    tcpHelper.send("ls /\r\n");
                }

            }
        });


        btnConnect.setOnClickListener(this);
        sdAdapter = new SdAdapter(MainActivity.this, sList);
        sdListView.setAdapter(sdAdapter);

        sdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                lastItem = item;
                if (item.equals(LAST_PATH)) {
                    String tempPath = pathBack(abPath);
                    if (!TextUtils.isEmpty(tempPath)) {
                        abPath = tempPath;
                        if (abPath.length() >= 1 && abPath.substring(abPath.length() - 1).equalsIgnoreCase("/")) {
                            tcpHelper.send("ls " + abPath.substring(0, abPath.length() - 1));
                            tcpHelper.send("\r\n");
                            Log.e(TAG, "click path=" + abPath.substring(0, abPath.length() - 1));

                        }
                        sList.clear();
                        sList.add(LAST_PATH);
                        sdAdapter.notifyDataSetChanged();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showInfo(MainActivity.this, "根目录！！！");
                            }
                        });
                    }

                } else {

                    //判断是不是gcode文件
                    if (item.length() >= 6 && item.substring(item.length() - 6, item.length()).equals(".gcode")) {
                        //选择操作
                        abPath = abPath + item;
                        Log.e(TAG, "path=" + abPath);
                        showDialogs("选择操作", item+" 为gcode文件,是否要打印？");

                        //目录操作
                    } else if (item.substring(item.length() - 1, item.length()).equals("/")) {
                        abPath = abPath + item;
                        if (tcpHelper != null) {
                            if (abPath.length() >= 1 && abPath.substring(abPath.length() - 1).equalsIgnoreCase("/")) {
                                tcpHelper.send("ls " + abPath.substring(0, abPath.length() - 1));
                                tcpHelper.send("\r\n");
                                Log.e(TAG, "click path=" + abPath.substring(0, abPath.length() - 1));

                            }

                        }
                        sList.clear();
                        sList.add(LAST_PATH);
                        sdAdapter.notifyDataSetChanged();

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showInfo(MainActivity.this, "目标是不是目录！");
                            }
                        });
                    }


                }


            }
        });

    }

    /**
     * 根据路径来返回上一个路径
     * ex sd/gcode/ff/dfg/ ->  sd/gcode/ff/
     *
     * @param data 绝对路径
     * @return 上一级路径 如果没有就返回NULL
     */
    private String pathBack(String data) {
        String result = null;
        int count = 0;
        int l = data.length();
        for (int i = l - 1; i >= 0; i--) {
            if (abPath.charAt(i) == '/') {
                count++;
                if (count == 1) {
                    result = "/";
                }
                if (count == 2) {
                    result = abPath.substring(0, i + 1);
                    break;
                }

            }
        }
        return result;
    }


    @Override
    public void onClick(View v) {

        if (connected) {
            connected = false;
            btnConnect.setText("断开连接");
            connect();

        } else {
            connected = true;
            btnConnect.setText("连接");
            disconnect();
        }


    }

    private void disconnect() {
        tcpHelper = TcpHelper.getSingleton(this);
        tcpHelper.close();
    }

    private void connect() {
        tcpHelper = TcpHelper.getSingleton(this);
        tcpHelper.setOnConnectListener(new OnConnectListener() {
            @Override
            public void isConnect(boolean isCon) {
                final String data = isCon ? "连接成功" : "连接失败";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showInfo(MainActivity.this, data);
                    }
                });

            }
        });

        tcpHelper.setOnTcpReceive(new OnTcpReceive() {
            @Override
            public void onReceive(byte[] receive) {

                receiveData = asciiToString(receive);
                Log.e(TAG, "接收的数据=" + receiveData);
                if (!receiveData.contains("Smoothie command shell")) {

                    if (receiveData.contains("Could not open")) {
                        if (lastItem.length() > 0) {
                            if (abPath.contains(lastItem)) {
                                abPath = abPath.substring(0, abPath.length() - lastItem.length());
                            }
                        }
                        tcpHelper.send("ls /\r\n");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showInfo(MainActivity.this, receiveData);

                            }
                        });
                    } else {

                        splits = receiveData.split("\\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                for (String t : splits) {
                                    if (!t.equals("ok")) {
                                        if(t.equals("/")){
                                            String last = sList.get(sList.size()-1);
                                            sList.remove(sList.size()-1);
                                            sList.add(last+t);
                                            continue;
                                        }
                                        sList.add(t);

                                    }
                                }
                                sdAdapter = new SdAdapter(MainActivity.this, sList);
                                sdListView.setAdapter(sdAdapter);
                                sdAdapter.notifyDataSetChanged();

                            }
                        });


                    }


                }

            }
        });
    }


    private String asciiToString(byte[] da) {
        StringBuilder sb = new StringBuilder();
        for (byte aDa : da) {
//                Log.e(TAG,"char="+(int)aDa);
            if ((int) aDa != 13) {
                sb.append((char) aDa);
            }
        }
//        Log.e(TAG, "sb.toString()=" + sb.toString().trim());
        return sb.toString().trim();
    }


//    private List<String> sdPathSort(List<String> list){
//        tempList = list;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while(findSlash(tempList)!=-1){
//                    int k = findSlash(tempList);
//                    tempList = replace(tempList,k);
//                }
//            }
//        }).start();
//        return tempList;
//    }
//
//    //找出斜杠
//    private static int findSlash(List<String> list){
//        int res = -1;
//        int length = list.size();
//        for(int i=0;i<length;i++){
//            //找出单独的斜杠
//            if(list.get(i).equals("/")){
//                res = i;
//                break;
//            }
//        }
//
//        return  res;
//    }
//
//
//    private static List<String> replace(List<String> list,int k) {
//        List<String> mlist = new ArrayList<>();
//        int len = list.size();
//        for(int i = 0;i<len;i++){
//            if((i+1 == k)){
//                mlist.add(list.get(i)+list.get(i+1));
//                continue;
//            }
//            if(i!=k)
//                mlist.add(list.get(i));
//
//        }
//        return mlist;
//
//    }
//
//


    private void showDialogs(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spHelper.saveSpPrintFile(abPath);
                startActivityForResult(new Intent(MainActivity.this, PrintActivity.class), REQUEST_CODE_PRINT);
//                startActivity(new Intent(MainActivity.this, PrintActivity.class));
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (lastItem.length() > 0) {
                    if (abPath.contains(lastItem)) {
                        abPath = abPath.substring(0, abPath.length() - lastItem.length());
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tcpHelper != null) {
            tcpHelper.close();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.onSetIpAndPort(this, null, null, spHelper, tvShowIp, tvShowPort);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(MainActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
//            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        }

        if (id == R.id.nav_send_file) {
            startActivity(new Intent(MainActivity.this, FileSendActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_PRINT){
//            String tempPath = pathBack(abPath);
//            if (!TextUtils.isEmpty(tempPath)) {
//                abPath = tempPath;
//            }

            if (lastItem.length() > 0) {
                if (abPath.contains(lastItem)) {
                    abPath = abPath.substring(0, abPath.length() - lastItem.length());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



}
