package com.kevin.the3dprinterdemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.the3dprinterdemo.MainActivity;
import com.kevin.the3dprinterdemo.R;
import com.kevin.the3dprinterdemo.utils.Utils;

public class FileSendActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnChooseFile;
    private Button btnSendFile;
    private TextView tvShowFilePath;
    private TextView tvShowStatus;
    private static final int FILE_SELECT_CODE = 0x123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_send);

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
                sendFile();
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
