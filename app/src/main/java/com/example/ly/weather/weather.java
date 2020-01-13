package com.example.ly.weather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.ly.util.Netutil;
import com.example.ly.gson.weatherbean;
import com.example.ly.gson.forecastbean;
import com.example.ly.gson.suggestionbean;
import com.example.ly.gson.utility;


public class weather extends AppCompatActivity implements View.OnClickListener  {

    private TextView temptext,citytext,humitext,windtext,weathertext,datetext;
    private ImageView weathericon,located_flag;
    private RelativeLayout mainlayout;
    private DrawerLayout mDrawerLayout;

    //声明AMapLocationClient类对象
    public AMapLocationClient mlocationClient=null;
    public  AMapLocationClientOption mLocationOption=null;
    private static final int MY_PERMISSIONS_REQUEST_CALL_LOCATION = 1;///定位权限

    ///下拉刷新
    public SwipeRefreshLayout myrefresh;
    /////城市选择按钮
    private Button citysSelect;
    ////刷新按钮
    private Button locatBtn;
    ///主界面
    ScrollView weatherLayout;

    ///已解析网络数据的message
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_FORECAST_WEATHER = 2;
    private static final int UPDATE_SUGGESTION_WEATHER = 3;
    ///定时器
    Timer timer = new Timer();
    long delay = 2000;
    long intevalPeriod =30*60*1000;

    ////当前选择的城市
    public String cur_city;
    //当前定位城市
    public String located_city="大兴";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始信息加载前先隐藏layout
        weatherLayout = (ScrollView) findViewById(R.id.main_weatherlayout);
        weatherLayout.setVisibility(View.INVISIBLE);

        ////读取上次保存的城市
        SharedPreferences pref=getSharedPreferences("latest_city",MODE_PRIVATE);
        cur_city=pref.getString("city","北京");///默认北京
        Log.d("reload", "上次保存的城市："+cur_city);

        ////滑动菜单初始化
        //mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_selectcity);

        locatBtn=(Button) findViewById(R.id.title_locate);
        locatBtn.setOnClickListener(this);
        /////城市选择单击事件
        citysSelect=(Button) findViewById(R.id.selectcity_bt);
        citysSelect.setOnClickListener(this);
        ///下拉刷新
        myrefresh=(SwipeRefreshLayout)findViewById(R.id.swipe);
        myrefresh.setColorSchemeResources(R.color.colorPrimary);
        myrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Netutil.getNetworkState(weather.this) != Netutil.NETWORN_NONE) {
                    // Log.d("myWeather", "网络OK");
                    Log.d("Refresh", "天气手动刷新");
                    queryWeatherCode(cur_city);
                    Toast.makeText(weather.this, "已更新！", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("myWeather", "网络未连接");
                    Toast.makeText(weather.this, "网络未连接！", Toast.LENGTH_LONG).show();
                }
            }
        });
        timer.schedule(task,delay,intevalPeriod);///启动定时器

        //检查版本是否大于M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_CALL_LOCATION);
            } else {
                //"权限已申请";
                Log.d("定位权限", "已获取");
                //showLocation();
            }
        }
    }

    ////定位权限获取函数
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //"权限已申请"
                showLocation();
            } else {
                Toast.makeText(weather.this, "权限已拒绝,不能定位", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 单次客户端的定位监听
     */
    AMapLocationListener locationSingleListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
//                    //获取当前定位结果来源，如网络定位结果，详见定位类型表
//                    Log.i("定位类型", aMapLocation.getLocationType() + "");
//                    Log.i("获取纬度", aMapLocation.getLatitude() + "");
//                    Log.i("获取经度", aMapLocation.getLongitude() + "");
//                    Log.i("获取精度信息", aMapLocation.getAccuracy() + "");
//                    //如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                    Log.i("地址", aMapLocation.getAddress());
//                    Log.i("国家信息", aMapLocation.getCountry());
//                    Log.i("省信息", aMapLocation.getProvince());
//                    Log.i("城市信息", aMapLocation.getCity());
//                    Log.i("城区信息", aMapLocation.getDistrict());
//                    Log.i("街道信息", aMapLocation.getStreet());
//                    Log.i("街道门牌号信息", aMapLocation.getStreetNum());
//                    Log.i("城市编码", aMapLocation.getCityCode());
//                    Log.i("地区编码", aMapLocation.getAdCode());
//                    Log.i("获取当前定位点的AOI信息", aMapLocation.getAoiName());
//                    Log.i("获取当前室内定位的建筑物Id", aMapLocation.getBuildingId());
//                    Log.i("获取当前室内定位的楼层", aMapLocation.getFloor());
//                    Log.i("获取GPS的当前状态", aMapLocation.getGpsAccuracyStatus() + "");
//
//                    //获取定位时间
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date date = new Date(aMapLocation.getTime());
//
//                    Log.i("获取定位时间", df.format(date));

                    // 停止定位（单次定位）
                    mlocationClient.stopLocation();
                    //located_city=aMapLocation.getDistrict();
                    Log.i("当前定位城市", located_city);
                    cur_city=located_city;//当前城市设定为定位城市
                    queryWeatherCode(located_city);///根据定位城市刷新天气
                    Toast.makeText(weather.this, "定位已更新！", Toast.LENGTH_LONG).show();
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    private void showLocation() {
        try {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mlocationClient.setLocationListener(locationSingleListener);

            //该方法默认为false。单次定位
            mLocationOption.setOnceLocation(true);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);

            //给定位客户端对象设置定位参数
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //mLocationOption.setInterval(5000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            //启动定位
            mlocationClient.startLocation();//启动定位
            Log.d("定位请求", "已发起");
            Toast.makeText(weather.this, "获取定位中…", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 停止定位
        if (null != mlocationClient) {
            mlocationClient.stopLocation();
        }
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != mlocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyLocation();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.selectcity_bt){
            ///定位城市静态变量传递
            selectCity.located_city=located_city;
            //////跳转到第二个活动
            Intent i= new Intent(this,selectCity.class);
            i.putExtra("curcity",cur_city);
            startActivityForResult(i,1);////requestcode为1
            ///滑动菜单
            //mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        if(v.getId()==R.id.title_locate){
            showLocation();
//            if (Netutil.getNetworkState(this) != Netutil.NETWORN_NONE) {
//               // Log.d("myWeather", "网络OK");
//                Log.d("Refresh", "天气手动刷新");
//                Toast.makeText(weather.this, "已更新！", Toast.LENGTH_LONG).show();
//                queryWeatherCode(cur_city);
//            } else {
//                Log.d("myWeather", "网络未连接");
//                Toast.makeText(weather.this, "网络未连接！", Toast.LENGTH_LONG).show();
//           }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            cur_city= data.getStringExtra("cityCode");////获取通过intent返回的数据
            Log.d("myWeather", "选择的城市为"+cur_city);
        }
        queryWeatherCode(cur_city);
    }

    ////定时器massage处理
    TimerTask task =new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            time_handler.sendMessage(message);
        }
    };
    Handler time_handler =new Handler(){
        @Override
        public void handleMessage(Message msg){
            Log.d("Refresh", "天气定时刷新");
            queryWeatherCode(cur_city);
            super.handleMessage(msg);
        }
    };

    //主线程处理消息并更新UI
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    showweather((weatherbean)msg.obj);
                    break;
                case UPDATE_FORECAST_WEATHER:
                    showforecast((forecastbean)msg.obj);
                    break;
                case UPDATE_SUGGESTION_WEATHER:
                    showsuggestion((suggestionbean)msg.obj);
                    showlifelevel((suggestionbean)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    ////获取网络数据
    private void queryWeatherCode(String cityCode) {
        updateNow(cityCode);
        updateForecast(cityCode);
        updateSuggestion(cityCode);
        myrefresh.setRefreshing(false);//隐藏下拉刷新的进度圈
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void updateSuggestion(String cityCode) {
        final String address = "https://free-api.heweather.net/s6/weather/lifestyle?location="+cityCode+"&key=5e2517c2134c4e729b5652669fa76753";
        Log.d("suggestion信息获取地址：", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                    }
                    String responseStr = response.toString();
                    Log.d("suggestion信息返回json:", responseStr);
                    final  suggestionbean imf=utility.transresponseSuggestion(responseStr);/////解析数据
                    Log.d("suggestion", "解析完成");
                    if(imf!=null)//////用消息机制在主线程中更新UI
                    {
                        Message msg =new Message();
                        msg.what = UPDATE_SUGGESTION_WEATHER;
                        msg.obj=imf;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
    private void updateForecast(String cityCode) {
        final String address = "https://free-api.heweather.net/s6/weather/forecast?location="+cityCode+"&key=5e2517c2134c4e729b5652669fa76753";
        Log.d("forecast信息获取地址：", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                    }
                    String responseStr = response.toString();
                    Log.d("forecast信息返回json:", responseStr);
                    final  forecastbean imf=utility.transresponseForecast(responseStr);/////解析数据
                    Log.d("forecast", "解析完成");
                    if(imf!=null)//////用消息机制在主线程中更新UI
                    {
                        Message msg =new Message();
                        msg.what = UPDATE_FORECAST_WEATHER;
                        msg.obj=imf;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
    private void updateNow(String cityCode) {
        final String address = "https://free-api.heweather.net/s6/weather/now?location="+cityCode+"&key=5e2517c2134c4e729b5652669fa76753";
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    final weatherbean imf=utility.transresponseNow(responseStr);/////解析数据
                    Log.d("transWeather", "解析完成");
                    if(imf!=null)//////用消息机制在主线程中更新UI
                    {
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=imf;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    ///更新UI数据
    private void showweather(weatherbean weather) {
        if(weather.getStatus().equals("ok")) {
            temptext = (TextView) findViewById(R.id.temp_text);
            humitext = (TextView) findViewById(R.id.humi_text);
            citytext = (TextView) findViewById(R.id.title_city);
            windtext = (TextView) findViewById(R.id.wind_text);
            weathertext=(TextView)findViewById(R.id.weather_txt);
            datetext=(TextView)findViewById(R.id.date_text);
            located_flag=(ImageView)findViewById(R.id.located_flag) ;
            ///获取系统日期
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH)+1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayofweek=calendar.get(Calendar.DAY_OF_WEEK);
            String xingqi=String.valueOf(dayofweek);
            if("1".equals(xingqi)){
                xingqi ="天";
            }else if("2".equals(xingqi)){
                xingqi ="一";
            }else if("3".equals(xingqi)){
                xingqi ="二";
            }else if("4".equals(xingqi)){
                xingqi ="三";
            }else if("5".equals(xingqi)){
                xingqi ="四";
            }else if("6".equals(xingqi)){
                xingqi ="五";
            }else if("7".equals(xingqi)){
                xingqi ="六";
            }
            datetext.setText(month+"月"+day+"日"+" "+"星期"+xingqi);

            temptext.setText(weather.getNow().getTmp());
            String humi=weather.getNow().getHum()+"%";
            humitext.setText(humi);
            String wind=weather.getNow().getWind_sc()+"级"+weather.getNow().getWind_dir();
            windtext.setText(wind);
            weathertext.setText(weather.getNow().getCond_txt());

            citytext.setText(weather.getBasic().getLocation());
            if(cur_city.equals(located_city)){
                located_flag.setVisibility(View.VISIBLE);
            }
            else{
                located_flag.setVisibility(View.INVISIBLE);
            }
            updateImage(weather.getNow().getCond_txt(),calendar.get(Calendar.HOUR_OF_DAY));
        }
    }

    private void updateImage(String weatherTxt,int curTime)
    {
        weathericon=(ImageView) findViewById(R.id.weather_icon);
        mainlayout=(RelativeLayout)findViewById(R.id.activity_weather);

        if(curTime>=6 && curTime<18)
        {
            mainlayout.setBackgroundResource(R.drawable.bg);

            if("晴".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.sun);
            }else if("多云".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.cloudy);
            }else if("小雨".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.s_rain);
            }else if("中雨".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.m_rain);
            }else if("大雨".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.b_rain);
            }else if("雷阵雨".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.thunder_rain);
            }else if("阴".equals(weatherTxt)){
                weathericon.setImageResource(R.drawable.yin);
            }
        }
        else
        {
            mainlayout.setBackgroundResource(R.drawable.night_bg);
            weathericon.setImageResource(R.drawable.night);
        }
    }

    private void showforecast(forecastbean forecastweather){
         LinearLayout forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        forecastLayout.removeAllViews();
        int index=1;
        for (forecastbean.DailyForecastBean forecast : forecastweather.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView todayText = (TextView) view.findViewById(R.id.today_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            ImageView iconImg = (ImageView) view.findViewById(R.id.icon_img);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.getDate());
            switch (index){
                case 1: todayText.setText("今天");break;
                case 2: todayText.setText("明天");break;
                case 3: todayText.setText("后天");break;
                default: break;
            }
            infoText.setText(forecast.getCond_txt_d());
            setIcon(iconImg,forecast.getCond_txt_d());
            maxText.setText(forecast.getTmp_max());
            minText.setText(forecast.getTmp_min());
            forecastLayout.addView(view);
            index++;//用于判断明后天
        }
    }
    ///根据天气情况给IMAGEVIEW设置图标
    private void setIcon(ImageView v,String weatherTxt){
        if("晴".equals(weatherTxt)){
            v.setImageResource(R.drawable.sun);
        }else if("多云".equals(weatherTxt)){
            v.setImageResource(R.drawable.cloudy);
        }else if("小雨".equals(weatherTxt)){
            v.setImageResource(R.drawable.s_rain);
        }else if("中雨".equals(weatherTxt)){
            v.setImageResource(R.drawable.m_rain);
        }else if("大雨".equals(weatherTxt)){
            v.setImageResource(R.drawable.b_rain);
        }else if("雷阵雨".equals(weatherTxt)){
            v.setImageResource(R.drawable.thunder_rain);
        }else if("阴".equals(weatherTxt)){
            v.setImageResource(R.drawable.yin);
        }
    }

    private void showsuggestion(suggestionbean weathersuggestion){
        TextView comfortText=(TextView) findViewById(R.id.comfort_text);
        TextView clothesText=(TextView) findViewById(R.id.clothes_text);
        TextView travelText=(TextView) findViewById(R.id.travel_text);
        TextView sportText=(TextView) findViewById(R.id.sport_text);

        List<suggestionbean.LifestyleBean> suggestions=weathersuggestion.getLifestyle();

        String comfort = "舒适度：" + suggestions.get(0).getTxt();
        String clothes = "穿衣建议：" + suggestions.get(1).getTxt();
        String travel = "旅行建议：" + suggestions.get(4).getTxt();
        String sport = "运动建议：" + suggestions.get(3).getTxt();
        comfortText.setText(comfort);
        clothesText.setText(clothes);
        travelText.setText(travel);
        sportText.setText(sport);
    }

    private void showlifelevel(suggestionbean weathersuggestion){
        TextView airText=(TextView) findViewById(R.id.airlevel);
        TextView sunshineText=(TextView) findViewById(R.id.sunshinelevel);
        TextView carText=(TextView) findViewById(R.id.carlevel);
        TextView coldText=(TextView) findViewById(R.id.coldlevel);
        List<suggestionbean.LifestyleBean> suggestions=weathersuggestion.getLifestyle();
        airText.setText(suggestions.get(7).getBrf());
        sunshineText.setText(suggestions.get(5).getBrf());
        carText.setText(suggestions.get(6).getBrf());
        coldText.setText(suggestions.get(2).getBrf());
    }

}


