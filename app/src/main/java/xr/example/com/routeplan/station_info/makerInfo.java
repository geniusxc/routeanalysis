package xr.example.com.routeplan.station_info;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/6/17.
 */

public class makerInfo implements Serializable{
    private double lng,lat;
    private String content;
    private LatLng latlng;

    public makerInfo(double lng, double lat, String content) {
        this.lng = lng;
        this.lat = lat;
        this.content = content;
        latlng=new LatLng(lat, lng);
    }
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LatLng getLatlng() {
        return latlng;
    }
    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

}

