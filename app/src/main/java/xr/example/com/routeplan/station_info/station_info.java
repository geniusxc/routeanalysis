package xr.example.com.routeplan.station_info;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/6/14.
 */

public class station_info implements Serializable {
    public  String repeatstation_latlon;

    public station_info(String b)
    {
        repeatstation_latlon=b;
    }

    public String getRepeatstation_latlon(){return repeatstation_latlon;}
}
