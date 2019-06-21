package com.monitor.android.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.monitor.android.R;
import com.monitor.android.util.PhoneInfo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelAct extends Activity {
    TextView txt,exportTxt;
    /*项目总文件夹路径*/
    public  static String PROJECT_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "com.monitor.android" + File.separator;

    private static String filePath = PROJECT_FILE_PATH + "tel_info.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tel);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // 通过Resources获取
        DisplayMetrics dm2 = getResources().getDisplayMetrics();

        // 获取屏幕的默认分辨率
        Display display = getWindowManager().getDefaultDisplay();
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_RING );
        txt=(TextView)findViewById(R.id.txt);
        exportTxt=(TextView)findViewById(R.id.exportTxt);
        txt.setText("Resolving power:"+display.getWidth()+"x"+display.getHeight()+"\n\n\n"+"Manufacturer："+ PhoneInfo.getPhoneBrand()+"\n\n\n"+"Device name:"+PhoneInfo.getPhoneModel()+"\n\n\n"+"Available memory:"+getAvailMemory()+"\n\n\n"
                +"cpu使用率:"+readUsage()+"%"
                +"\n\n\nCall Volume:"+mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL )
                +"\n\n\nSystem volume:"+mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM )
                +"\n\n\nRinger volume:"+mAudioManager.getStreamVolume( AudioManager.STREAM_RING )
                +"\n\n\nMusic volume:"+mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC )
                +"\n\n\nSound volume:"+mAudioManager.getStreamVolume( AudioManager.STREAM_ALARM  )
        );
        exportTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createExcel(filePath);
            }
        });

    }
    public void createExcel(String filePath) {
        // printlnToUser("reading XLSX file from resources");

        try {
            FileOutputStream fop = null;
            String sdCardDir =Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.e("tag","sdCardDir===="+sdCardDir);
            String content =txt.getText().toString();
            File file = new File(sdCardDir,"telInfo.txt");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            Toast.makeText(this,"export success,file path is"+sdCardDir+"/"+"telInfo.txt",Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("tag","e:"+e);
            Toast.makeText(this,"export fail",Toast.LENGTH_SHORT).show();

        }
    }

    public String getAvailMemory() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

        am.getMemoryInfo(mi);

        //mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化

    }
    public double readUsage( )
    {
        double usage=0;
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/stat" ) ), 1000 );
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);

             usage =currTotal* 100.0f / (currTotal     + currIdle    );

        }
        catch( IOException ex )
        {
            ex.printStackTrace();
        }
return usage;
    }



}
