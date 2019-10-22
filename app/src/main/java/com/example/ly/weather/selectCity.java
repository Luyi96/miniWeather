package com.example.ly.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ly.db.CityBean;
import com.example.ly.db.dbApplication;

import java.util.ArrayList;
import java.util.List;

public class selectCity extends AppCompatActivity implements View.OnClickListener{
    private ImageView BackBtn;
    private List<CityBean> CityList;
    private dbApplication dbApp;

    private List<String> dataList =new ArrayList<>();
    private ListView mlistView;
    private ArrayAdapter<String> adapter ;
    String cur_city;
    String selected_city;

    private TextView titleCityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        Log.d("selectcity","oncreate");

        Intent intent= getIntent();
        cur_city=intent.getStringExtra("curcity");
        titleCityText=(TextView)findViewById(R.id.title_curcity) ;
        String s="当前城市："+cur_city;
        titleCityText.setText(s);

        BackBtn=(ImageView)findViewById(R.id.title_back);
        BackBtn.setOnClickListener(this);

        dbApp = (dbApplication) getApplication();////获得一个Application对象，然后再得到相应的成员变量
        CityList=dbApp.getCityList();
        if(CityList.size()>0)
        {
            for (CityBean city : CityList) {
                dataList.add(city.getCity());
            }
        }

        mlistView=(ListView)findViewById(R.id.listview);
        adapter= new ArrayAdapter<String>(selectCity.this, android.R.layout.simple_list_item_1,dataList);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selected_city=dataList.get(position);
                    Toast.makeText(selectCity.this, selected_city, Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor=getSharedPreferences("latest_city",MODE_PRIVATE).edit();
                    editor.putString("city",selected_city);
                    editor.apply();

                    Intent i = new Intent();
                    i.putExtra("cityCode", selected_city);
                    setResult(RESULT_OK, i);
                    finish();
            }
        });
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
//                Intent i = new Intent();
//                i.putExtra("cityCode", "101160101");
//                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }
}
