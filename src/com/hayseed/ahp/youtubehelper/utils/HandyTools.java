package com.hayseed.ahp.youtubehelper.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HandyTools
{
    public static String getLocalTime (long utcTime)
    {
        Date date = new Date (utcTime);

        DateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
                
        String s = format.format (date);
        
        return format.format (date);
    }
}