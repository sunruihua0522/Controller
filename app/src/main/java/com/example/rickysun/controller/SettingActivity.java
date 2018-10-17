package com.example.rickysun.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.sql.Savepoint;

public class SettingActivity extends AppCompatActivity {

    Button btn_back=null;
    private EditText IP_Edit=null;
    private EditText Port_Edit=null;
    private EditText Time_Edit=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        IP_Edit=findViewById(R.id.IP_Edit);
        Port_Edit=findViewById(R.id.Port_Edit);
        Time_Edit=findViewById(R.id.TimeInterval_Edit);

        btn_back=findViewById(R.id.back_btn);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingActivity.this,MainActivity.class);
                intent.putExtra("IP",IP_Edit.getText().toString());
                intent.putExtra("Port",Port_Edit.getText().toString());
                intent.putExtra("TimeInterval",Time_Edit.getText().toString());
                startActivity(intent);
            }
        });
        if(savedInstanceState!=null)
        {
            IP_Edit.setText(savedInstanceState.getString("IP"));
            Port_Edit.setText(savedInstanceState.getString("Port"));
            Time_Edit.setText(savedInstanceState.getString("TimeInterval"));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle InstanceState) {
        InstanceState.putString("IP",IP_Edit.getText().toString());
        InstanceState.putString("Port",Port_Edit.getText().toString());
        InstanceState.putString("TimeInterval",Time_Edit.getText().toString());
        super.onSaveInstanceState(InstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle InstanceState) {
        super.onRestoreInstanceState(InstanceState);
        IP_Edit.setText(InstanceState.getString("IP"));
        Port_Edit.setText(InstanceState.getString("Port"));
    }
}
