package com.example.ly.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.example.ly.util.Netutil;
import com.example.ly.gson.weatherbean;
import com.example.ly.gson.utility;





public class weather extends AppCompatActivity implements View.OnClickListener{

    private TextView temptext,citytext,humitext,windtext,weathertext,datetext;
    private ImageView weathericon;
    private RelativeLayout mainlayout;

    /////城市选择按钮
    private TextView citysSelect;
    ////刷新按钮
    private ImageView updateBtn;

    ///已解析网络数据的message
    private static final int UPDATE_TODAY_WEATHER = 1;
    ///定时器
    Timer timer = new Timer();
    long delay = 2000;
    long intevalPeriod =30*60*1000;

    ////当前选择的城市
    String cur_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ////读取上次保存的城市
        SharedPreferences pref=getSharedPreferences("latest_city",MODE_PRIVATE);
        cur_city=pref.getString("city","北京");///默认北京
        Log.d("reload", "上次保存的城市："+cur_city);

        ////为刷新按钮增加单击事件
        updateBtn=(ImageView)findViewById(R.id.title_update);
        updateBtn.setOnClickListener(this);
        /////城市选择单击事件
        citysSelect=(TextView)findViewById(R.id.title_city);
        citysSelect.setOnClickListener(this);

        timer.schedule(task,delay,intevalPeriod);///启动定时器
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.title_city){
            Intent i= new Intent(this,selectCity.class);
            i.putExtra("curcity",cur_city);
            startActivityForResult(i,1);////requestcode为1
        }
        if(v.getId()==R.id.title_update){
            if (Netutil.getNetworkState(this) != Netutil.NETWORN_NONE) {
               // Log.d("myWeather", "网络OK");
                Log.d("Refresh", "天气手动刷新");
                Toast.makeText(weather.this, "已更新！", Toast.LENGTH_LONG).show();
                queryWeatherCode(cur_city);
            } else {
                Log.d("myWeather", "网络未连接");
                Toast.makeText(weather.this, "网络未连接！", Toast.LENGTH_LONG).show();
            }
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
                    showweather((weatherbean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    ////获取网络数据
    private void queryWeatherCode(String cityCode) {
       // final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
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
                    final weatherbean imf=utility.transresponse(responseStr);/////解析数据
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
    private void showweather(weatherbean weather)
    {
        if(weather.getStatus().equals("ok")) {
            temptext = (TextView) findViewById(R.id.temp_text);
            humitext = (TextView) findViewById(R.id.humi_text);
            citytext = (TextView) findViewById(R.id.title_city);
            windtext = (TextView) findViewById(R.id.wind_text);
            weathertext=(TextView)findViewById(R.id.weather_txt);
            datetext=(TextView)findViewById(R.id.date_text);

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
            citytext.setText(weather.getBasic().getLocation());
            String humi=weather.getNow().getHum()+"%";
            humitext.setText(humi);
            String wind=weather.getNow().getWind_sc()+"级"+weather.getNow().getWind_dir();
            windtext.setText(wind);
            weathertext.setText(weather.getNow().getCond_txt());

            updateImage(weather.getNow().getCond_txt(),calendar.get(Calendar.HOUR_OF_DAY));
        }
    }

    private void updateImage(String weatherTxt,int curTime)
    {
        weathericon=(ImageView) findViewById(R.id.weather_icon);
        mainlayout=(RelativeLayout)findViewById(R.id.activity_weather);

        if(curTime>=6 && curTime<18)
        {
            mainlayout.setBackgroundResource(R.drawable.sun_bg);

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


}


