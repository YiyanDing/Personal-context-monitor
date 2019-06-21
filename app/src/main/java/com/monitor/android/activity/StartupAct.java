package com.monitor.android.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

import com.monitor.android.R;
import com.monitor.android.util.PhoneInfo;
import com.monitor.android.view.ChartView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartupAct extends Activity {
    private UsageStatsManager mUsmManager;
    ChartView chartView;
    TextView txt;
    //x轴坐标对应的数据
    private List<String> xValue = new ArrayList<>();
    //y轴坐标对应的数据
    private List<Integer> yValue = new ArrayList<>();
    //折线对应的数据
    private Map<String, Integer> value = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
          chartView = (ChartView) findViewById(R.id.chartview);
        txt = (TextView) findViewById(R.id.txt);
        txt.setText("The Y axis represents the number of startup times\n\n \n" +
                "\n" +
                "The X axis represents the name of the app\n");
        mUsmManager = getUsageStatsManager();
        if (getUsageList().isEmpty()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            try {
                printUsageStats(getUsageList());
            }catch (Exception e){

            }
        }
    }



    public void printUsageStats(List<UsageStats> usageStatsList) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        PackageManager pm = getPackageManager();             //拿到包管理者
        //拿到包的信息  PackageInfo是系统的一个类
        List<PackageInfo> info = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
                |PackageManager.GET_PERMISSIONS);


        for (UsageStats u : usageStatsList) {
            for (PackageInfo temp:info){
                if (temp.packageName.equals(u.getPackageName())){

                        xValue.add(temp.applicationInfo.loadLabel(pm).toString());
                        value.put(temp.applicationInfo.loadLabel(pm).toString(), u.getClass().getDeclaredField("mLaunchCount").getInt(u));//60--240




                }
            }


        }

        for (int i = 0; i < 8; i++) {
            if (i == 6) {
                yValue.add(400);
            } else if (i == 7) {
                yValue.add(600);
            } else {
                yValue.add(i * 50);
            }
        }

        chartView.setValue(value, xValue, yValue);
        chartView.setSelectIndex(1);//索引点
         //chartView.setMonth("图表");

    }
    @SuppressWarnings("ResourceType")
    private UsageStatsManager getUsageStatsManager() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
        return usm;
    }
    private List<UsageStats> getUsageList() {//com.UCMobile----ForegroundTime = 25:31----lasttimeuser = PM9:12:08----times = 12
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();
        List<UsageStats> usageList = mUsmManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        return usageList;
    }




}
