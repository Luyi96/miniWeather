package com.example.ly.gson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;

/**
 * Created by LY on 2019/9/26.
 */

public class utility {
    public static weatherbean transresponse(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent =jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,weatherbean.class);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
