package com.example.acer.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acer.coolweather.db.City;
import com.example.acer.coolweather.db.Country;
import com.example.acer.coolweather.db.Province;
import com.example.acer.coolweather.util.HttpUtil;
import com.example.acer.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private Button backButton;
    private TextView textTitle;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    //记录当前列表级别
    private int currentLevel;
    //列表级别
    private static int LEVEL_PROVINCE = 0;
    private static int LEVEL_CITY = 1;
    private static int LEVEL_COUNTRY = 2;
    //记录选中省份和城市
    private  Province selectedProvince;
    private City selectedCity;
    private ProgressDialog progressDialog;

    //获得控件加载数据到Fragment
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        backButton = view.findViewById(R.id.back_button);
        textTitle = view.findViewById(R.id.title_text);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //为listview中的item添加监听事件
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCountries();
                } else if (currentLevel == LEVEL_COUNTRY) {
                    String weatherId = countryList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            //返回加载上一级内容
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTRY) {
                    queryCities();
                }
            }
        });
    }

    private void queryProvinces() {
        backButton.setVisibility(View.GONE);
        textTitle.setText("中国");
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromSever(address,"province");
        }
    }

    private void queryCities(){
        backButton.setVisibility(View.VISIBLE);
        textTitle.setText(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city: cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromSever(address,"city");
        }
    }

    private void queryCountries(){
        backButton.setVisibility(View.VISIBLE);
        textTitle.setText(selectedCity.getCityName());
        countryList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size() > 0){
            dataList.clear();
            for (int i = 0; i < countryList.size(); i++) {
                dataList.add(countryList.get(i).getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        } else {
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()
                    +"/"+selectedCity.getCityCode();
            queryFromSever(address,"country");
        }
    }

    private void queryFromSever(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //响应失败
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //响应成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;//响应状态
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                } else if ("country".equals(type)) {
                    result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                //网络数据加载成功
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if("city".equals(type)){
                                queryCities();
                            } else if("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
