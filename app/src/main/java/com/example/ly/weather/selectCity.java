package com.example.ly.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ly.db.CityBean;
import com.example.ly.db.MyAdapter;
import com.example.ly.db.dbApplication;
import com.example.ly.db.FilterListener;


import java.util.ArrayList;
import java.util.List;

public class selectCity extends AppCompatActivity implements View.OnClickListener{
    private ImageView BackBtn;
    private List<CityBean> CityList;
    private dbApplication dbApp;

    private List<String> dataList =new ArrayList<>();
    private ListView mlistView;
    private EditText searchEdit;
    //private ArrayAdapter<String> adapter ;

    String cur_city;
    protected static String located_city;///静态变量传递
    String selected_city;

    boolean isFilter;
    private MyAdapter adapter = null;

    private TextView titleCityText,locatedcityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        Log.d("selectcity","oncreate");

        ///当前城市传递与显示
        Intent intent= getIntent();
        cur_city=intent.getStringExtra("curcity");
        titleCityText=(TextView)findViewById(R.id.title_curcity) ;
        String s="当前城市："+cur_city;
        titleCityText.setText(s);

        //定位城市传递与显示
        locatedcityText=(TextView)findViewById(R.id.locatedcity) ;
        String s2="定位地点："+located_city;
        locatedcityText.setText(s2);
        locatedcityText.setOnClickListener(this);

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

        searchEdit = (EditText) findViewById(R.id.search);
        mlistView=(ListView)findViewById(R.id.listview);
//        adapter= new ArrayAdapter<String>(selectCity.this, android.R.layout.simple_list_item_1,dataList);
//        mlistView.setAdapter(adapter);
//        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    selected_city=dataList.get(position);
//                    Toast.makeText(selectCity.this, selected_city, Toast.LENGTH_SHORT).show();
//
//                    SharedPreferences.Editor editor=getSharedPreferences("latest_city",MODE_PRIVATE).edit();
//                    editor.putString("city",selected_city);
//                    editor.apply();
//
//                    Intent i = new Intent();
//                    i.putExtra("cityCode", selected_city);
//                    setResult(RESULT_OK, i);
//                    finish();
//            }
//        });
        setData();// 给listView设置adapter
        setListeners();// 设置监听
    }
    /**
     * 数据初始化并设置adapter
     */
    private void setData() {
        // 这里创建adapter的时候，构造方法参数传了一个接口对象，这很关键，回调接口中的方法来实现对过滤后的数据的获取
        adapter = new MyAdapter(dataList, this, new FilterListener() {
            // 回调方法获取过滤后的数据
            public void getFilterData(List<String> list) {
                // 这里可以拿到过滤后数据，所以在这里可以对搜索后的数据进行操作
                Log.e("TAG", "接口回调成功");
                Log.e("TAG", list.toString());
                setItemClick(list);
            }
        });
        mlistView.setAdapter(adapter);
    }

    /**
     * 给listView添加item的单击事件
     * @param filter_lists  过滤后的数据集
     */
    protected void setItemClick(final List<String> filter_lists) {
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 点击对应的item时，弹出toast提示所点击的内容
                    selected_city=filter_lists.get(position);
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


    private void setListeners() {
        // 没有进行搜索的时候，也要添加对listView的item单击监听
        setItemClick(dataList);
        /**
         * 对编辑框添加文本改变监听，搜索的具体功能在这里实现
         * 很简单，文本该变的时候进行搜索。关键方法是重写的onTextChanged（）方法。
         */
        searchEdit.addTextChangedListener(new TextWatcher() {
            /**
             *
             * 编辑框内容改变的时候会执行该方法
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                if(adapter != null){
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
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
            case R.id.locatedcity:
                Intent i = new Intent();
                i.putExtra("cityCode", located_city);
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }
}
