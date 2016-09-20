package com.kevin.the3dprinterdemo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kevin.the3dprinterdemo.R;
import com.kevin.the3dprinterdemo.utils.SpHelper;
import com.kevin.the3dprinterdemo.utils.Utils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etIp;
    private EditText etPort;
    private Button btnSave;
    private SpHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        etIp = (EditText) findViewById(R.id.etIp);
        etPort = (EditText) findViewById(R.id.etPort);
        btnSave = (Button) findViewById(R.id.btnSave);
        spHelper = SpHelper.getSingleton(this);
        btnSave.setOnClickListener(this);
        Utils.onSetIpAndPort(this, etIp, etPort, spHelper,null,null);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.onSetIpAndPort(this, etIp, etPort, spHelper, null, null);
    }



    public void saveInfo(View view) {
        spHelper.saveSpIp(etIp.getText().toString());
        spHelper.saveSpPort(etPort.getText().toString());
        Toast.makeText(SettingActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
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
    public void onClick(View v) {
        saveInfo(v);
    }
}
