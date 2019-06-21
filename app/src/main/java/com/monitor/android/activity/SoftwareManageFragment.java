package com.monitor.android.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.monitor.android.R;
import com.monitor.android.adapter.SoftwareAdapter;
import com.monitor.android.bean.AppInfo;
import com.monitor.android.util.StorageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public class SoftwareManageFragment extends BaseFragment {


    Context mContext;
    public static final int REFRESH_BT = 111;
    private static final String ARG_POSITION = "position";
    private int position; // 0:应用软件，2 系统软件
    SoftwareAdapter mAutoStartAdapter;

    ListView listview;


    TextView topText;
    List<AppInfo> userAppInfos = null;
    List<AppInfo> systemAppInfos = null;

    View mProgressBar;
    TextView mProgressBarText;

    private Method mGetPackageSizeInfoMethod;

    AsyncTask<Void, Integer, List<AppInfo>> task;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(ARG_POSITION);


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.fragment_software, container, false);
        listview=(ListView)view.findViewById(R.id.listview);
        topText=(TextView)view.findViewById(R.id.topText);
        mProgressBar=(View)view.findViewById(R.id.progressBar);
        mProgressBarText=(TextView)view.findViewById(R.id.progressBarText);
        mContext = getActivity();
        try {
            mGetPackageSizeInfoMethod = mContext.getPackageManager().getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        fillData();
    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }

    private void fillData() {

        if (position == 0) {
            topText.setText("");

        } else {
            topText.setText("卸载下列软件，会影响正常使用");

        }


        task = new AsyncTask<Void, Integer, List<AppInfo>>() {
            private int mAppCount = 0;

            @Override
            protected List<AppInfo> doInBackground(Void... params) {
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> packInfos = pm.getInstalledPackages(0);
                publishProgress(0, packInfos.size());
                List<AppInfo> appinfos = new ArrayList<AppInfo>();
                for (PackageInfo packInfo : packInfos) {
                    publishProgress(++mAppCount, packInfos.size());
                    final AppInfo appInfo = new AppInfo();
                    appInfo.setAppIcon(packInfo.applicationInfo.loadIcon(pm));
                    appInfo.setUid(packInfo.applicationInfo.uid);

                    if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        appInfo.setUserApp(false);//系统应用
                    } else {
                        appInfo.setUserApp(true);//用户应用
                    }
                    if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        appInfo.setInRom(false);
                    } else {
                        appInfo.setInRom(true);
                    }
                    String appName = packInfo.applicationInfo.loadLabel(pm).toString();
                    appInfo.setAppName(appName);
                    String packname = packInfo.packageName;
                    appInfo.setPackname(packname);
                    String version = packInfo.versionName;
                    appInfo.setVersion(version);
                    try {
                        mGetPackageSizeInfoMethod.invoke(mContext.getPackageManager(), new Object[]{
                                packname,
                                new IPackageStatsObserver.Stub() {
                                    @Override
                                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                                        synchronized (appInfo) {
                                            appInfo.setPkgSize(pStats.cacheSize + pStats.codeSize + pStats.dataSize);

                                        }
                                    }
                                }
                        });
                    } catch (Exception e) {
                    }

                    appinfos.add(appInfo);
                }
                return appinfos;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    mProgressBarText.setText("Scaning...");
                } catch (Exception e) {

                }

            }

            @Override
            protected void onPreExecute() {
                try {
                    showProgressBar(true);
                    mProgressBarText.setText("Scaning…");
                } catch (Exception e) {

                }

                //    loading.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(  List<AppInfo> result) {

                super.onPostExecute(result);


                try {

                    showProgressBar(false);

                    userAppInfos = new ArrayList<>();
                    systemAppInfos = new ArrayList<>();
                    long allSize = 0;
                    for (AppInfo a : result) {
                        if (a.isUserApp()) {
                            allSize += a.getPkgSize();
                            userAppInfos.add(a);
                        } else {
                            systemAppInfos.add(a);
                        }
                    }
                    if (systemAppInfos.size()>0){
                        String txt="";
                        for (int i=0;i<systemAppInfos.size();i++){
                            txt+="app name:"+systemAppInfos.get(i).getAppName()+",Resources:"+(StorageUtil.convertStorage(systemAppInfos.get(i).getPkgSize()))+";";
                        }
                        createExcel(txt);
                    }
                    if (position == 0) {
                        topText.setText(getString(R.string.software_top_text, userAppInfos.size(), StorageUtil.convertStorage(allSize)));
                        mAutoStartAdapter = new SoftwareAdapter(mContext, userAppInfos);
                        listview.setAdapter(mAutoStartAdapter);

                    } else {

                        mAutoStartAdapter = new SoftwareAdapter(mContext, systemAppInfos);
                        listview.setAdapter(mAutoStartAdapter);


                    }
                } catch (Exception e) {

                }
            }

        };
        task.execute();


    }
    public void createExcel(String content) {
        // printlnToUser("reading XLSX file from resources");

        try {
            FileOutputStream fop = null;
            String sdCardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.e("tag","sdCardDir===="+sdCardDir);

            File file = new File(sdCardDir,"applist.txt");
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
            Toast.makeText(getContext(),"export success,file path is"+sdCardDir+"/"+"applist.txt",Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("tag","e:"+e);
            Toast.makeText(getContext(),"export fail",Toast.LENGTH_SHORT).show();

        }
    }

    private boolean isProgressBarVisible() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
