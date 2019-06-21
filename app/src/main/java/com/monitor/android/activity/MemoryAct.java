package com.monitor.android.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monitor.android.R;
import com.monitor.android.adapter.ClearMemoryAdapter;
import com.monitor.android.bean.AppProcessInfo;
import com.monitor.android.service.CoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryAct extends Activity implements CoreService.OnPeocessActionListener{
    ListView listView;
    ClearMemoryAdapter mClearMemoryAdapter;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    private CoreService mCoreService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(MemoryAct.this);
            mCoreService.scanRunProcess();
            //  updateStorageUsage();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow);
        listView=(ListView)findViewById(R.id.listView);
        mClearMemoryAdapter = new ClearMemoryAdapter(this, mAppProcessInfos);
        listView.setAdapter(mClearMemoryAdapter);
        bindService(new Intent(this, CoreService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);

Log.e("tag","----------1-------------");
    }

    @Override
    public void onScanStarted(Context context) {

    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        mAppProcessInfos.clear();


        for (AppProcessInfo appInfo : apps) {
            if (!appInfo.isSystem) {
                mAppProcessInfos.add(appInfo);

            }
        }
        mClearMemoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {

    }
}
