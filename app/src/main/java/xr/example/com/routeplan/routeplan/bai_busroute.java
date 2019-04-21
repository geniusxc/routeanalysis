package xr.example.com.routeplan.routeplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

import xr.example.com.routeplan.R;
import xr.example.com.routeplan.baidumap_overlayutil.BusLineOverlay;
import xr.example.com.routeplan.baidumap_overlayutil.TransitRouteOverlay;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2018/5/20.
 */

public class bai_busroute extends Activity implements OnGetRoutePlanResultListener {
    private List<String> buslineIdList;
    EditText cityname,bus_id;

    boolean isfirstlocate=true;
    int buslineIndex=0;
    String busline=null;
    String city=null;
    String provider;
    int totalLine;
    private MyLocationConfiguration.LocationMode mCurrentMode;//??????????
    LocationManager locationManager;

    RoutePlanSearch mrouteSearch = null;
    LatLng myCurrentposition;
    private MyLocationListener mLocationListener;
    BaiduMap baiduMap;
    MapView mapView;
    private LocationClient mLocationClient;
    private BusLineSearch busLineSearch;
    private PoiSearch poiSearch;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.bai_bus);
        init_bus();
    }
    public void init_bus()
    {
        cityname=(EditText)findViewById(R.id.cityname);
        bus_id=(EditText)findViewById(R.id.bus_id);
        buslineIdList=new ArrayList<String>();

        mapView=(MapView)findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        mrouteSearch = RoutePlanSearch.newInstance();
        mrouteSearch.setOnGetRoutePlanResultListener(this);
        busLineSearch= BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
        poiSearch= PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        mLocationClient = new LocationClient(this);//是否可用getApplicationContext取代this；????????
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
        initLocation();
        initLocationManager();
    }

    public void initLocationManager() {//使得以我的位置为中心但不反复定位
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            //当前没有可用的位置提供器时,弹出Toast提示
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null){
            navigateTo(location);
        }
        //locationManager.requestLocationUpdates(provider,5000,5,locationListener);
    }

    private void navigateTo(Location location) {
//如果是第一次创建,就获取位置信息并且将地图移到当前位置
//为防止地图被反复移动,所以就只在第一次创建时执行
        if (isfirstlocate) {
//LatLng对象主要用来存放经纬度//封装设备当前位置并且显示在地图上
//zoomTo是用来设置百度地图的缩放级别,范围为3~19,数值越大越精确
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isfirstlocate = false;
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=10000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        // option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }


    public void btn_searchbusid(View view){
        baiduMap.clear();
        city  = cityname.getText().toString();
        busline = bus_id.getText().toString();
        poiSearch.searchInCity((new PoiCitySearchOption()).city(city).keyword(busline));
    }

    public void btn_nextline(View view){
        searchBusline();
    }

    public void searchBusline() {
       if (buslineIndex >= buslineIdList.size()) {
          buslineIndex = 0;
      }
        if (buslineIndex >= 0 && buslineIndex < buslineIdList.size() && buslineIdList.size() > 0)
        {
            boolean flag = busLineSearch.searchBusLine((new BusLineSearchOption().city(city).uid(buslineIdList.get(buslineIndex))));
            if (flag) {
                Toast.makeText(bai_busroute.this, "检索成功~",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(bai_busroute.this, "检索失败~", Toast.LENGTH_SHORT).show();
            }
            buslineIndex++;
        }
    }

    /**
     * 以下是对公交线路的实例化对象的‘充实’
     */
    OnGetBusLineSearchResultListener busLineSearchResultListener = new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult) {
            if (busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(bai_busroute.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            else
            {
                baiduMap.clear();
                BusLineOverlay overlay = new bai_busroute.MyBuslineOverlay(baiduMap);// 用于显示一条公交详情结果的Overlay
                overlay.setData(busLineResult);
                overlay.addToMap();// 将overlay添加到地图上
                overlay.zoomToSpan();// 缩放地图，使所有overlay都在合适的视野范围内
                baiduMap.setOnMarkerClickListener(overlay);
                // 公交线路名称
                Toast.makeText(bai_busroute.this, "最大票价："+busLineResult.getMaxPrice(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            baiduMap.clear();
            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR)
            {
                Toast.makeText(bai_busroute.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR)
            {
                TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(baiduMap);
                transitRouteOverlay.setData(transitRouteResult.getRouteLines().get(0));// 设置一条驾车路线方案??????????????????
                baiduMap.setOnMarkerClickListener(transitRouteOverlay);
                transitRouteOverlay.addToMap();
                transitRouteOverlay.zoomToSpan();
                totalLine = transitRouteResult.getRouteLines().size();
                Toast.makeText(bai_busroute.this, "共查询出" + totalLine + "条符合条件的线路",Toast.LENGTH_SHORT).show();
              //  if (totalLine > 1) {
            //        nextLineBtn.setEnabled(true);
           //     }
                // 通过getTaxiInfo()可以得到很多关于打车的信息
                Toast.makeText(bai_busroute.this,
                        "该路线打车总路程" + transitRouteResult.getTaxiInfo().getDistance(),Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    class MyBuslineOverlay extends BusLineOverlay {//用的是v4的BusLineOverlay 写的是v3的方法
        public MyBuslineOverlay(BaiduMap arg0) {
            super(arg0);
        }
        /**
         * 站点点击事件
         */
        @Override
        public boolean onBusStationClick(int arg0) {
            MarkerOptions options = (MarkerOptions) getOverlayOptions().get(arg0);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(options.getPosition()));
            return true;
        }
    }

    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                Toast.makeText(bai_busroute.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
                // 遍历所有poi，找到类型为公交线路的poi
                buslineIdList.clear();
                for (PoiInfo poi : poiResult.getAllPoi()) {
                    if (poi.type == PoiInfo.POITYPE.BUS_LINE || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                        buslineIdList.add(poi.uid);
                    }
                }
                searchBusline();
            }
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        }
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    } ;

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            baiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder().
                    accuracy(bdLocation.getRadius()).direction(100).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();//生成定位数据
            baiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);  //第三个参数是位置图片没有就默认
            baiduMap.setMyLocationConfigeration(config);
            Log.e(TAG, "onReceiveLocation:111");
            myCurrentposition = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
          //  baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(myCurrentposition));//以我的位置为中心
        }
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.instruction_menu, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return false;
        }
        return true;
    }
    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
    }

}
