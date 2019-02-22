package com.tmxweather.android.gson;

import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;

public class Weather
{
    public Now now;

    public Forecast forecast;

    public AirNow airNow;

    public Lifestyle lifestyle;

    public boolean isNull()
    {
        if (now!=null && forecast!=null && lifestyle!=null )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
