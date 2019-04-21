package xr.example.com.routeplan.routeplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import xr.example.com.routeplan.baidumap_overlayutil.DrivingRouteOverlay;
import xr.example.com.routeplan.baidumap_overlayutil.OverlayManager;
import xr.example.com.routeplan.baidumap_overlayutil.TransitRouteOverlay;
import xr.example.com.routeplan.explanation_class.about;
import xr.example.com.routeplan.explanation_class.use;
import xr.example.com.routeplan.R;

import static android.content.ContentValues.TAG;

public class bai_driveroute extends Activity implements OnGetGeoCoderResultListener, BaiduMap.OnMapClickListener
        , OnGetRoutePlanResultListener, AdapterView.OnItemSelectedListener {
    private LocationClient mLocationClient;
    private MyLocationListener2 mLocationListener;
    private MyLocationConfiguration.LocationMode mCurrentMode;//??????????
    RoutePlanSearch mrouteSearch = null;
    GeoCoder mGeoSearch;
   // private LocationManager locationManager;
    BaiduMap baiduMap = null;
    MapView mapView = null;
    EditText edt_start_pos, edt_end_pos;
    Button next_node, last_node, next_line;
    TextView strInfo;
    Spinner drive_way, drive_strategy;

    LatLng myCurrentposition;
    LatLng nodeLocation = null;//节点位置
    LatLng from_point;
    String city =null;
    private Marker mMarker;
    double cur_lon = 117.26359;
    double cur_lat = 31.87112;//当前城市的经纬度
    double cur_lat1, cur_lon1;
    int index = -1;
    int nodeIndex = -1;
    int drivingResultIndex = 0;

    private int totalLine = 0;// 记录某种搜索出的方案数量
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    boolean isclick = false;
    boolean isfirstlocate = true;
    boolean isfirstspinner_f=false;
    private TextView popupText = null;//相比较dialog函数多了通过参数view，with,height来设置显示位置的功能
    String nodeTitle = null;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.bai_drive);
        intloc();
    }

    private void intloc() {
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapClickListener(this);

        next_line = (Button) findViewById(R.id.next_line);
        edt_start_pos = (EditText) findViewById(R.id.edt_start_pos);
        edt_end_pos = (EditText) findViewById(R.id.edt_end_pos);
        next_node = (Button) findViewById(R.id.next);
        last_node = (Button) findViewById(R.id.pre);
        strInfo = (TextView) findViewById(R.id.strInfo);

        drive_way = (Spinner) findViewById(R.id.travel_way);
        drive_way.setOnItemSelectedListener(this) ;
        drive_strategy = (Spinner) findViewById(R.id.drive_strategy);
        drive_strategy.setOnItemSelectedListener(this);

        mGeoSearch = GeoCoder.newInstance();
        mGeoSearch.setOnGetGeoCodeResultListener(this);
        mrouteSearch = RoutePlanSearch.newInstance();
        mrouteSearch.setOnGetRoutePlanResultListener(this);

        mLocationClient = new LocationClient(this);//是否可用getApplicationContext取代this；????????
        mLocationListener = new MyLocationListener2();
        mLocationClient.registerLocationListener(mLocationListener);
        initLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 10000;
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

    public void driving_search1(){
        //   mGeoSearch.geocode(new GeoCodeOption().city(edt_city.getText().toString()).address(edt_start_pos.getText().toString()));
        PlanNode startNode = PlanNode.withCityNameAndPlaceName(city, edt_start_pos.getText().toString());
        //mGeoSearch.geocode(new GeoCodeOption().city(edt_city.getText().toString()).address(edt_start_pos.getText().toString()));//由于双击之后才能显示路线，具体原因不明；
        //PlanNode startNode = PlanNode.withLocation(from_point);
        PlanNode endNode = PlanNode.withLocation(new LatLng(cur_lat1, cur_lon1));
        mrouteSearch.drivingSearch((new DrivingRoutePlanOption())
                .policy(DrivingRoutePlanOption.DrivingPolicy.values()[drivingResultIndex]).from(startNode).to(endNode));
        isclick = false;
    }
    public void driving_search2(){
        PlanNode startNode = PlanNode.withCityNameAndPlaceName(city, edt_start_pos.getText().toString());
        PlanNode endNode = PlanNode.withCityNameAndPlaceName(city, edt_end_pos.getText().toString());
        mrouteSearch.drivingSearch((new DrivingRoutePlanOption()).policy(DrivingRoutePlanOption.DrivingPolicy.values()[drivingResultIndex]).from(startNode).to(endNode));
    }
    public void driving_search3(){
        PlanNode startNode = PlanNode.withLocation(myCurrentposition);
        PlanNode endNode = PlanNode.withLocation(new LatLng(cur_lat1, cur_lon1));
        mrouteSearch.drivingSearch((new DrivingRoutePlanOption()).policy(DrivingRoutePlanOption.DrivingPolicy.values()[drivingResultIndex]).from(startNode).to(endNode));
        isclick = false;
    }
    public void driving_search4(){
        PlanNode startNode = PlanNode.withLocation(myCurrentposition);
        PlanNode endNode = PlanNode.withCityNameAndPlaceName(city, edt_end_pos.getText().toString());
        mrouteSearch.drivingSearch((new DrivingRoutePlanOption()).policy(DrivingRoutePlanOption.DrivingPolicy.values()[drivingResultIndex]).from(startNode).to(endNode));
    }

    public  void searchButtonProcess(View v) {
        switch (v.getId()) {
            case R.id.next:
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.pre) {
                    return;
                }
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                }
                Object step1 = route.getAllStep().get(nodeIndex);
                if (step1 instanceof DrivingRouteLine.DrivingStep) {
                    nodeLocation = ((DrivingRouteLine.DrivingStep) step1).getEntrance().getLocation();
                    nodeTitle = ((DrivingRouteLine.DrivingStep) step1).getInstructions();
                }
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                popupText = new TextView(bai_driveroute.this);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                baiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
                break;
            case R.id.pre:
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.pre) {
                    return;
                }
                if (nodeIndex > 0) {
                    nodeIndex--;
                }
                Object step2 = route.getAllStep().get(nodeIndex);
                if (step2 instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step2).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step2).getInstructions();
                }
                if (nodeLocation == null || nodeTitle ==null) {
                    return;
                }
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                popupText = new TextView(bai_driveroute.this);
                popupText.setBackgroundResource(R.drawable.ic_showroute);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                baiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
                break;
            case R.id.next_line:
                switch (index) {
                    case 0:
                        driving_search1();
                        break;
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult ) {
        baiduMap.clear();
        if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR)
        {
            Toast.makeText(bai_driveroute.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(bai_driveroute.this, "共查询出" + totalLine + "条符合条件的线路",Toast.LENGTH_SHORT).show();
            //  if (totalLine > 1) {
            //        nextLineBtn.setEnabled(true);
            //     }
            // 通过getTaxiInfo()可以得到很多关于打车的信息
            Toast.makeText(bai_driveroute.this,
                    "该路线打车总路程" + transitRouteResult.getTaxiInfo().getDistance(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_driveroute.this,"抱歉，未找到驾车线路",Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex=-1;
            route=result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay=overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();

            totalLine = result.getRouteLines().size();
            Toast.makeText(bai_driveroute.this,
                    "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_SHORT).show();
            if (totalLine > 1) {
                next_line.setEnabled(true);
            }
            // 通过getTaxiInfo()可以得到很多关于打车的信息
             Toast.makeText(bai_driveroute.this, "该路线打车总路程" + result.getTaxiInfo(),Toast.LENGTH_SHORT).show();
           //  strInfo.setText(result.getTaxiInfos().size());
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {//
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_driveroute.this, "地址转经纬度失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null != mMarker) {
            mMarker.remove();
        }
        baiduMap.clear();
        cur_lat=result.getLocation().latitude;
        cur_lon=result.getLocation().longitude;
        from_point=new LatLng(cur_lat,cur_lon);

      //  PlanNode frompoint_Node = PlanNode.withLocation(from_point);
        //PlanNode endNode = PlanNode.withLocation(new LatLng(cur_lat1, cur_lon1));
      //  mrouteSearch.drivingSearch((new DrivingRoutePlanOption()).from(frompoint_Node).to(endNode));

    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {//  if (TextUtils.isEmpty(reverseGeoCodeResult.getAddress()))
              if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_driveroute.this, "抱歉，未能找到您所单击的地址", Toast.LENGTH_LONG).show();
            return;
        }
        if (null != mMarker) {
            mMarker.remove();
        }
        isclick=true;
        baiduMap.clear();
        baiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation)));
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        Toast.makeText(bai_driveroute.this, result.getAddress(),Toast.LENGTH_LONG).show();
        String showinfo=String.format("纬度：%.5f 经度：%.5f", result.getLocation().latitude, result.getLocation().longitude);
        strInfo.setText(showinfo);
        edt_end_pos.setText(result.getAddress());//单击的终点位置
        cur_lat1= result.getLocation().latitude;//单击的纬度
        cur_lon1= result.getLocation().longitude;//单击的经度
        //用第二种表达形式显示addOverlay;
        LatLng from = new LatLng( result.getLocation().latitude,
                result.getLocation().longitude);
        BitmapDescriptor bdB = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_navigation);
        OverlayOptions ooP = new MarkerOptions().position(from).icon(bdB);
        mMarker = (Marker) (baiduMap.addOverlay(ooP));
        //   MapStatus mMapStatus = new MapStatus.Builder().target(from)
        //    .build();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      switch(adapterView.getId())
      {
            case R.id.travel_way:
                     if (i == 0 || i == 1 || i == 2)
                     {
                        route = null;
                        baiduMap.clear();
                        index = 0;
                      if (edt_start_pos.getText().toString().trim().length() != 0 && isclick)//单击终点，起点手动输入
                      {
                      next_line.setEnabled(false);
                      driving_search1();
                     } else if (edt_start_pos.getText().toString().trim().length() != 0 && !isclick) //手动输入起始点与终点
                     {
                      next_line.setEnabled(false);
                      driving_search2();
                     } else if (isclick && edt_start_pos.getText().toString().trim().length() == 0) {//单击确定终点，起点为自身位置
                      next_line.setEnabled(false);
                     driving_search3();
                      } else if (!isclick && edt_end_pos.getText().toString().trim().length() != 0 && edt_start_pos.getText().toString().trim().length() == 0) {//起点为自身位置，终点手动输入
                       next_line.setEnabled(false);
                       driving_search4();
                      }
                }
                break;
        case R.id.drive_strategy:
      //      if (isfirstspinner) {
          //      view.setVisibility(View.INVISIBLE);
          //     isfirstspinner = false;  //可使spinner默认不选上
         //   }
                if (i == 0) {
                    if(isfirstspinner_f) {
                        drivingResultIndex = 0;
                        Toast.makeText(bai_driveroute.this, "避免拥堵", Toast.LENGTH_SHORT).show();
                    }
                    isfirstspinner_f=true;
                }

                if (i == 1) {
                    drivingResultIndex = 1;
                    Toast.makeText(bai_driveroute.this, "最短距离", Toast.LENGTH_SHORT).show();
                }

                if (i == 2) {
                    drivingResultIndex = 2;
                    Toast.makeText(bai_driveroute.this, "费用最少", Toast.LENGTH_SHORT).show();
                }

                if (i == 3) {
                    drivingResultIndex = 3;
                    Toast.makeText(bai_driveroute.this, "最短优先", Toast.LENGTH_SHORT).show();
                }
                break;
       }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
        }
    }

    @Override
    public void onMapClick(LatLng point) {//单击地图方法的实现
        mGeoSearch.reverseGeoCode(new ReverseGeoCodeOption().location(point));
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mLocationClient.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();
        mGeoSearch.destroy();
    }

    public class MyLocationListener2 implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            baiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder().
                    accuracy(bdLocation.getRadius()).direction(100).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();//生成定位数据
            baiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);  //第三个参数是位置图片没有就默认
            city = bdLocation.getCity();
            baiduMap.setMyLocationConfigeration(config);
            Log.e(TAG, "onReceiveLocation:111");
            myCurrentposition = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            if (isfirstlocate) {
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(myCurrentposition));//以我的位置为中心%或者也用isfirstlocate来控制
                isfirstlocate = false;
            }
        }
    }
        private void setActionBar() {
            android.app.ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
            actionBar.setDisplayShowHomeEnabled(false);//取消logo
            actionBar.setTitle("返回");//设置返回字样
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_instruction, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.add_item://打开使用说明页面
                    Intent intent = new Intent(bai_driveroute.this, use.class);
                    startActivity(intent);
                    break;
                case R.id.about://打开关于百度软件的页面
                    Intent intent2 = new Intent(bai_driveroute.this, about.class);
                    startActivity(intent2);
                    break;
                case R.id.txv_setting://打开设置页面
                    Intent intent3 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent3);
                    break;
                case android.R.id.home:
                    this.finish();
                    return false;
            }
            return true;
        }

}