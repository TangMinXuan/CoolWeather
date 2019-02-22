package com.tmxweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tmxweather.android.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * AlarmManager是我们设定一个时间，然后在该时间到来时，AlarmManager为我们广播一个我们设定的Intent,
     * 通常我们使用 PendingIntent.
     * PendingIntent可以理解为Intent的封装包，简单的说就是在Intent上在加个指定的动作。
     * 在使用Intent的时候，我们还需要在执行startActivity、startService或sendBroadcast才能使Intent有用。
     * 而PendingIntent的话就是将这个动作包含在内了。
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        updateWeather();
        updateBingPic();

        AlarmManager manager = (AlarmManager)getSystemService( ALARM_SERVICE );
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;

        Intent i = new Intent( this,AutoUpdateService.class );
        PendingIntent pi = PendingIntent.getService( this,0,i,0 );

        /**
         * 取消alarm使用AlarmManager.cancel()函数，传入参数是个PendingIntent实例。
         * 该函数会将所有跟这个PendingIntent相同的Alarm  全部取消    !!!
         * 执行到这一步一共有两个pi，一个是我们上一行刚刚创建的pi，还没有传进Alarm，
         * 一个是上一次提醒时的pi，他已经传进Alarm并执行过一次。
         * 下面的cancel()取消的是第二个pi。
         */
        manager.cancel( pi );
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi );

        return super.onStartCommand( intent, flags, startId );
    }

    private void updateWeather()
    {
        HeConfig.init("HE1902141203411712", "7e1ec64f061941079fadd9c93b9667b0");
        HeConfig.switchToFreeServerNode();

        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences( this );
        String weatherId = prefs.getString( "weatherId",null );

        if (weatherId != null)
        {

            HeWeather.getWeatherNow( this, weatherId,
                    new HeWeather.OnResultWeatherNowBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( AutoUpdateService.this,
                                    "Now on Error",Toast.LENGTH_SHORT ).show();

                        }

                        @Override
                        public void onSuccess(List<Now> list)
                        {
                            Gson gson = new Gson();
                            String jsonData = gson.toJson( list );

                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                            editor.putString( "now",jsonData );
                            editor.apply();
                        }
                    } );

            HeWeather.getWeatherForecast( this, weatherId,
                    new HeWeather.OnResultWeatherForecastBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( AutoUpdateService.this,
                                    "Forecast on Error",Toast.LENGTH_SHORT ).show();
                        }

                        @Override
                        public void onSuccess(List<Forecast> list)
                        {
                            Gson gson = new Gson();
                            String jsonData = gson.toJson( list );

                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                            editor.putString( "forecast",jsonData );
                            editor.apply();
                        }
                    } );

            HeWeather.getWeatherLifeStyle( this, weatherId,
                    new HeWeather.OnResultWeatherLifeStyleBeanListener() {
                        @Override
                        public void onError(Throwable throwable)
                        {
                            Log.d( "MyLog",throwable.toString());
                            Toast.makeText( AutoUpdateService.this,
                                    "LifeStyle on Error",Toast.LENGTH_SHORT ).show();
                        }

                        @Override
                        public void onSuccess(List<Lifestyle> list)
                        {
                            Gson gson = new Gson();
                            String jsonData = gson.toJson( list );

                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                            editor.putString( "lifestyle",jsonData );
                            editor.apply();
                        }
                    } );
        }
        else
        {
            Log.d( "MyLog", "Service:weatherId is null" );
        }
    }

    private void updateBingPic()
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
                        AutoUpdateService.this ).edit();
                editor.putString( "bing_pic",bingPic );
                editor.apply();
            }
        } );
    }
}
