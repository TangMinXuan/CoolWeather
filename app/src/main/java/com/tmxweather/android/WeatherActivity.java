package com.tmxweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tmxweather.android.gson.Weather;
import com.tmxweather.android.service.AutoUpdateService;
import com.tmxweather.android.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
{
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    private Button setButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        if (Build.VERSION.SDK_INT >=21)
        {
            View decorView = getWindow ().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor( Color.TRANSPARENT);

        }
        setContentView( R.layout.activity_weather );

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView)findViewById( R.id.bing_pic_img );

        swipeRefresh = (SwipeRefreshLayout)findViewById( R.id.swipe_refresh );
        swipeRefresh.setColorSchemeResources( R.color.colorPrimary );

        drawerLayout = (DrawerLayout)findViewById( R.id.drawer_layout );
        navButton = (Button)findViewById( R.id.nav_button );

        setButton = (Button)findViewById( R.id.set_button );

        navButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                drawerLayout.openDrawer( GravityCompat.START );
            }
        } );

        swipeRefresh.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                requestWeather( mWeatherId );
            }
        } );

        setButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(WeatherActivity.this,SettingActivity.class);
                startActivity( intent );
            }
        } );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );

        String bingPic = prefs.getString( "bing_pic",null );
        if (bingPic!=null)
        {
            Glide.with( this ).load( bingPic ).into( bingPicImg );
        }
        else
        {
            loadBingPic();
        }

        String weatherId = getIntent().getStringExtra( "weather_id" );
        mWeatherId = weatherId;
        weatherLayout.setVisibility( View.INVISIBLE );

        requestWeather( weatherId );
    }

    /**
     * 加载每日一图
     */
    private void loadBingPic()
    {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest( requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.d( "MyLog",e.toString() );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this ).edit();
                editor.putString( "bing_pic",bingPic );
                editor.apply();

                runOnUiThread( new Runnable() {
                    @Override
                    public void run()
                    {
                        Glide.with( WeatherActivity.this ).
                                load( bingPic ).into( bingPicImg );
                    }
                } );
            }
        } );
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(String weatherId)
    {
        HeConfig.init("HE1902141203411712", "7e1ec64f061941079fadd9c93b9667b0");
        HeConfig.switchToFreeServerNode();

        final Weather weather = new Weather();

        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences( WeatherActivity.this );
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences( WeatherActivity.this ).edit();

        if (prefs.getString( "weatherId",null )!=null)
        {
            String idInSP =  prefs.getString( "weatherId",null );
            if (idInSP.equals( weatherId )==false)
            {//有缓存时，判断请求id与缓存id是否相等，否,则替换掉缓存id并将三个属性的缓存置空
                if (weatherId==null)
                {
                    weatherId=idInSP;
                }
                else
                {
                    editor.putString( "weatherId",weatherId );
                    editor.putString( "now",null );
                    editor.putString( "forecast",null );
                    editor.putString( "lifestyle",null );
                    editor.apply();
                }
            }
        }
        else
        {
            //没缓存时，写入当前城市id
            editor.putString( "weatherId",weatherId );
            editor.apply();
        }

        if (prefs.getString("now",null )!=null )
        {
            Gson gson = new Gson();
            final List<Now> nowList = gson.fromJson( prefs.getString("now",null ),
                    new TypeToken<List<Now>>(){}.getType() );
            runOnUiThread( new Runnable() {
                @Override
                public void run()
                {
                    weather.now=nowList.get( 0 );
                    if (weather.isNull() == true)
                    {
                        showWeatherInfo( weather );
                    }
                }
            } );
        }
        else
        {
            HeWeather.getWeatherNow( this, weatherId,
                    new HeWeather.OnResultWeatherNowBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( WeatherActivity.this,
                                    "Now on Error",Toast.LENGTH_SHORT ).show();

                        }

                        @Override
                        public void onSuccess(final List<Now> list)
                        {
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    Gson gson = new Gson();
                                    final String jsonData = gson.toJson( list );

                                    final List<Now> nowList = gson.fromJson( jsonData,
                                            new TypeToken<List<Now>>(){}.getType() );
                                    weather.now=nowList.get( 0 );

                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences( WeatherActivity.this ).edit();
                                    editor.putString( "now",jsonData );
                                    editor.apply();

                                    if (weather.isNull() == true)
                                    {
                                        showWeatherInfo( weather );
                                    }
                                }
                            } );
                        }
                    } );
        }



        if (prefs.getString("forecast",null )!=null )
        {
            Gson gson = new Gson();
            final List<Forecast> forecastList = gson.fromJson( prefs.getString("forecast",null ),
                    new TypeToken<List<Forecast>>(){}.getType() );
            runOnUiThread( new Runnable() {
                @Override
                public void run()
                {
                    weather.forecast=forecastList.get( 0 );
                    if (weather.isNull() == true)
                    {
                        showWeatherInfo( weather );
                    }
                }
            } );
        }
        else
        {
            HeWeather.getWeatherForecast( this, weatherId,
                    new HeWeather.OnResultWeatherForecastBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( WeatherActivity.this,
                                    "Forecast on Error",Toast.LENGTH_SHORT ).show();
                        }

                        @Override
                        public void onSuccess(final List<Forecast> list)
                        {
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    Gson gson = new Gson();
                                    final String jsonData = gson.toJson( list );

                                    final List<Forecast> forecastList = gson.fromJson( jsonData,
                                            new TypeToken<List<Forecast>>(){}.getType() );

                                    weather.forecast=forecastList.get( 0 );

                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences( WeatherActivity.this ).edit();
                                    editor.putString( "forecast",jsonData );
                                    editor.apply();

                                    if (weather.isNull() == true)
                                    {
                                        showWeatherInfo( weather );
                                    }
                                }
                            } );
                        }
                    } );
        }


        if (prefs.getString("lifestyle",null )!=null)
        {
            Gson gson = new Gson();

            final List<Lifestyle> lifestyleList = gson.fromJson( prefs.getString("lifestyle",null ),
                    new TypeToken<List<Lifestyle>>(){}.getType() );
            runOnUiThread( new Runnable() {
                @Override
                public void run()
                {
                    weather.lifestyle=lifestyleList.get( 0 );
                    if (weather.isNull() == true)
                    {
                        showWeatherInfo( weather );
                    }
                }
            } );
        }
        else
        {
            HeWeather.getWeatherLifeStyle( this, weatherId,
                    new HeWeather.OnResultWeatherLifeStyleBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( WeatherActivity.this,
                                    "LifeStyle on Error",Toast.LENGTH_SHORT ).show();
                        }

                        @Override
                        public void onSuccess(final List<Lifestyle> list)
                        {
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    Gson gson = new Gson();
                                    final String jsonData = gson.toJson( list );

                                    final List<Lifestyle> lifestyleList = gson.fromJson( jsonData,
                                            new TypeToken<List<Lifestyle>>(){}.getType() );

                                    weather.lifestyle=lifestyleList.get( 0 );

                                    SharedPreferences.Editor editor = PreferenceManager.
                                            getDefaultSharedPreferences( WeatherActivity.this ).edit();
                                    editor.putString( "lifestyle",jsonData );
                                    editor.apply();

                                    if (weather.isNull() == true)
                                    {
                                        showWeatherInfo( weather );
                                    }
                                }
                            } );
                        }
                    } );

        }

        //loadBingPic();
    }

    /**
     * 处理并展示weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather)
    {
        String cityName = weather.now.getBasic().getLocation();
        String updateTime = weather.now.getUpdate().getLoc().split( " " )[1];
        String degree = weather.now.getNow().getTmp() + "°C";
        String weatherInfo = weather.now.getNow().getCond_txt();
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (ForecastBase forecastBase:weather.forecast.getDaily_forecast())
        {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecastBase.getDate());
            infoText.setText(forecastBase.getCond_txt_d());
            maxText.setText(forecastBase.getTmp_max());
            minText.setText(forecastBase.getTmp_min());
            forecastLayout.addView(view);
        }

        String comfort = "建议一：" + weather.lifestyle.getLifestyle().get( 0 ).getTxt();
        String carWash = "建议二：" + weather.lifestyle.getLifestyle().get( 2 ).getTxt();
        String sport = "建议三：" + weather.lifestyle.getLifestyle().get( 4 ).getTxt();
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing( false );

        Intent intent = new Intent( this,AutoUpdateService.class );
        startService( intent );
    }

}
