package com.example.rickysun.controller;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.sql.ClientInfoStatus;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Button BtnSetting=null;
    private Button BtnConnect=null;
    private Button BtnDisConnect=null;
    private Socket Client=null;
    private String IP="";
    private String Port="";
    private String TimeInterval="";
    private Handler handler=null;
    private SensorManager sm=null;
    private int cmd=-1;
    private int oldCmd=-1;
    private Semaphore semaphore = new Semaphore(1,false);
    private Object objLock=new Object();
    private  int STOP=0, LEFT=1, RIGHT=2,FORWARD=3,BACKWARD=4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BtnSetting = findViewById(R.id.setting_btn);
        BtnConnect=findViewById(R.id.connect_btn);
        BtnDisConnect=findViewById(R.id.disconnect_btn);

        Intent intent=getIntent();
        IP=intent.getStringExtra("IP");
        Port=intent.getStringExtra("Port");
        TimeInterval=intent.getStringExtra("TimeInterval");


        this.UpdateUI();


        //连接服务器
        BtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run()
                    {
                        try {
                            Client = new Socket();
                            Client.connect(new InetSocketAddress(IP,Integer.parseInt(Port)),2000);
                            BufferedReader in = new BufferedReader(new InputStreamReader(Client.getInputStream()));
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                    Client.getOutputStream())), true);

                            if (Client.isConnected()) {
                                Message msg= handler.obtainMessage();
                                msg.what=0;
                                msg.arg1=0;
                                msg.arg2=1;
                                handler.sendMessage(msg);
                                if (!Client.isOutputShutdown()) {
                                    while(true)
                                    {
                                       Thread.sleep(Integer.parseInt(TimeInterval));
                                        synchronized (objLock)
                                        {
                                            if(oldCmd!=cmd) {
                                                out.println(String.format("%d",cmd));
                                                oldCmd=cmd;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        catch (Exception ex)
                        {
                            //
                            Message msg= handler.obtainMessage();
                            msg.what=1;
                            msg.obj="Failed to connect server:" + ex.getMessage();
                            handler.sendMessage(msg);
                        }
                    }
                }.start();
            }

        });

        //断开服务器
        BtnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run()
                    {
                        if(Client!=null)
                        {
                            try {
                                Client.close();
                                Client = null;
                                Message msg=handler.obtainMessage();
                                msg.what=0;
                                msg.arg1=1;
                                msg.arg2=0;
                                handler.sendMessage(msg);
                            }
                            catch (IOException ex)
                            {

                            }
                        }
                    }
                }.start();

            }
        });


        //设置
        BtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        sm= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),sm.SENSOR_DELAY_GAME);

    }//OnCreate



    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        int Angle=20;
        synchronized (objLock) {

            if (Math.abs(values[1]) < Angle &&  Math.abs(values[2])<Angle) {
                cmd=STOP;
            }
            else {
                if(Math.abs(values[2])>=Angle)
                {
                    cmd=values[2] > 0 ? RIGHT : LEFT;
                }
                else {
                    if(Math.abs(values[1])>=Angle)
                        cmd=values[1] > 0 ? FORWARD : BACKWARD;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void UpdateUI() //更新UI
    {
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case 0:
                        BtnConnect.setEnabled(msg.arg1==1);
                        BtnDisConnect.setEnabled(msg.arg2==1);
                        break;
                    case 1:
                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;

                }
            }
        };
    }

}
