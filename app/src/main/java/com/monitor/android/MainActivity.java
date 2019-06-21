package com.monitor.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monitor.android.activity.AppAct;
import com.monitor.android.activity.EelectricityAct;
import com.monitor.android.activity.FlowAct;
import com.monitor.android.activity.LocationAct;
import com.monitor.android.activity.MemoryAct;
import com.monitor.android.activity.ScreenBrigAct;
import com.monitor.android.activity.StartupAct;
import com.monitor.android.activity.StepAct;
import com.monitor.android.activity.TelAct;
import com.monitor.android.bean.SDCardInfo;
import com.monitor.android.util.AppUtil;
import com.monitor.android.util.StorageUtil;
import com.monitor.android.view.ArcProgress;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener{
    LinearLayout electricityBut,flowBut,telInfoBtn,startupBtn,moemoryBtn,appBtn,stepBtn,locationBtn;
    ArcProgress arcStore;
    ArcProgress arcProcess;
    private Timer timer;
    private Timer timer2;
    TextView capacity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView(){
        electricityBut=(LinearLayout)findViewById(R.id.electricityBut);
        flowBut=(LinearLayout)findViewById(R.id.flowBut);
        telInfoBtn=(LinearLayout)findViewById(R.id.telInfoBtn);
        startupBtn=(LinearLayout)findViewById(R.id.startupBtn);
        moemoryBtn=(LinearLayout)findViewById(R.id.moemoryBtn);
        appBtn=(LinearLayout)findViewById(R.id.appBtn);
        stepBtn=(LinearLayout)findViewById(R.id.stepBtn);
        locationBtn=(LinearLayout)findViewById(R.id.locationBtn);

        arcStore=(ArcProgress)findViewById(R.id.arc_store);
        arcProcess=(ArcProgress)findViewById(R.id.arc_process);
        capacity=(TextView)findViewById(R.id.capacity);


        stepBtn.setOnClickListener(this);
        appBtn.setOnClickListener(this);
        moemoryBtn.setOnClickListener(this);
        startupBtn.setOnClickListener(this);
        telInfoBtn.setOnClickListener(this);
        electricityBut.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        flowBut.setOnClickListener(this);
        fillData();
        Log.e("tag","-----"+sHA1(this));
    }
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void fillData() {
        // TODO Auto-generated method stub
        timer = null;
        timer2 = null;
        timer = new Timer();
        timer2 = new Timer();


        long l = AppUtil.getAvailMemory(this);
        long y = AppUtil.getTotalMemory(this);
        final double x = (((y - l) / (double) y) * 100);
        //   arcProcess.setProgress((int) x);

        arcProcess.setProgress(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if (arcProcess.getProgress() >= (int) x) {
                            timer.cancel();
                        } else {
                            arcProcess.setProgress(arcProcess.getProgress() + 1);
                        }

                    }
                });
            }
        }, 50, 20);

        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(this);

        long nAvailaBlock;
        long TotalBlocks;
        if (mSDCardInfo != null) {
            nAvailaBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            nAvailaBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        final double percentStore = (((TotalBlocks - nAvailaBlock) / (double) TotalBlocks) * 100);

        capacity.setText(StorageUtil.convertStorage(TotalBlocks - nAvailaBlock) + "/" + StorageUtil.convertStorage(TotalBlocks));
        arcStore.setProgress(0);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if (arcStore.getProgress() >= (int) percentStore) {
                            timer2.cancel();
                        } else {
                            arcStore.setProgress(arcStore.getProgress() + 1);
                        }

                    }
                });
            }
        }, 50, 20);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.electricityBut:
                Intent intent=new Intent(this, EelectricityAct.class);
                startActivity(intent);
                break;
            case R.id.flowBut:
                Intent flowBut=new Intent(this, FlowAct.class);
                startActivity(flowBut);
                break;

            case R.id.telInfoBtn:
                Intent mTelAct=new Intent(this, TelAct.class);
                startActivity(mTelAct);
                break;

            case R.id.startupBtn:
                Intent mStartupAct=new Intent(this, StartupAct.class);
                startActivity(mStartupAct);
                break;
            case R.id.moemoryBtn:
                Intent mMemoryAct=new Intent(this, MemoryAct.class);
                startActivity(mMemoryAct);
                break;

            case R.id.appBtn:
                Intent mApp=new Intent(this, AppAct.class);
                startActivity(mApp);
                break;

            case R.id.stepBtn:
                Intent step=new Intent(this, StepAct.class);
                startActivity(step);
                break;
            case R.id.locationBtn:
                Intent location=new Intent(this, LocationAct.class);
                startActivity(location);
                break;
        }
    }


}
