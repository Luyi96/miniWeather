package com.example.ly.db;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by LY on 2019/10/10.
 */

public class dbApplication extends Application{
    private static dbApplication mApplication;
    private CityDB mCityDB;
    private List<CityBean> mCityList;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("dbApplication","oncreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }

    //创建citylist读取的线程
    private void initCityList(){
        mCityList = new ArrayList<CityBean>();
        new Thread(new Runnable() {
            @Override
            public void run() {
// TODO Auto-generated method stub
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList()
    {
        mCityList = mCityDB.getAllCity();
//        int i=0;
//        for (CityBean city : mCityList) {
//            i++;
//            String cityName = city.getCity();
//            String cityCode = city.getNumber();
//            Log.d(TAG,cityCode+":"+cityName);
//        }
//        Log.d(TAG,"i="+i);
        return true;
    }
    public List<CityBean> getCityList() {
        return mCityList;
    }
    public static dbApplication getInstance(){
        return mApplication;
    }

    ////将城市列表从city.db添加到私有目录数据库
    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()     ////获取绝对路径
                + File.separator + getPackageName()                 ////File.separator 目录分隔符
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);          ////打开city.db   /data/data/com.example.ly.weather/databases1/city.db
        Log.d(TAG,path);
        if (!db.exists()) { ///未在该目录读取到city.db
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;

            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();///目录不存在则创建目录
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is = getAssets().open("city.db");///获取assets目录里的文件
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

}
