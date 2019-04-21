package xr.example.com.routeplan.routeplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import xr.example.com.routeplan.R;
import xr.example.com.routeplan.baidumap_overlayutil.DrivingRouteOverlay;
import xr.example.com.routeplan.baidumap_overlayutil.OverlayManager;
import xr.example.com.routeplan.baidumap_overlayutil.TransitRouteOverlay;
import xr.example.com.routeplan.baidumap_overlayutil.WalkingRouteOverlay;
import xr.example.com.routeplan.routeinfo.onroute_basicinfo;
import xr.example.com.routeplan.routeinfo.onroute_expenseinfo;
import xr.example.com.routeplan.routeinfo.onroute_otherinfo;
import xr.example.com.routeplan.routeinfo.onroute_routeinfo;
import xr.example.com.routeplan.station_info.makerInfo;
import xr.example.com.routeplan.station_info.point;
import xr.example.com.routeplan.station_info.station_info;

public class bai_routeplan extends Activity implements OnGetGeoCoderResultListener, OnGetRoutePlanResultListener,
        View.OnClickListener,AdapterView.OnItemSelectedListener {
    private Marker mMarker;
    private GeoCoder geoCoder;
    private RouteLine route = null;
    private OverlayManager routeOverlay = null;
    private RoutePlanSearch mrouteSearch = null;
    private MassTransitRouteLine massroute = null;

    private WalkingRouteResult nowResultwalk = null;
    private TransitRouteResult nowResultransit = null;
    private DrivingRouteResult nowResultdrive = null;
    private MassTransitRouteResult nowResultmass = null;

    private MyLocationConfiguration.LocationMode mCurrentMode;
    private MyLocationListener1 mLocationListener;
    private LocationClient mLocationClient;
    MapView mapView = null;
    BaiduMap baiduMap;
    LatLng myCurrentposition;

  @SuppressLint("HandlerLeak")//忽略内存泄露的警告，Lint用于帮助提升代码的质量
  private  final  Handler  mhandler = new  Handler() {//不使用Handler机制可能会报错，地图页面无法正常显示
        @Override
        public void handleMessage(Message msg) {
            initmapEvent();//这是函数是添加百度地图点击事件的,长按地图在按下处添加一个标注
            makerInfo marker = new makerInfo(mylocation_lon, mylocation_lat, "我的位置");//？？？？？？？？？？？？
            addOverlay(marker);
        }
    };

    private Button btn_nextroute;
    private Button btn_routeinfo;
    private Button next_node, last_node;
    private TextView popupText = null;//相比较dialog函数多了通过参数view，with,height来设置显示位置的功能
    private Spinner drive_way, drive_strategy;

    private int nodeIndex = -1;
    private int clicktime = 0;
    private int drivingResultIndex = 0;
    private int nowSearchType =1;//当前的驾车方式的序号,默认单击下一基站点是打车方式
    private int i_lastpointtransit_flag_unpass = 0;
    private int i_lastpointtransit_flag_pass=0;
    private int i_onroute_routeinfo = 0;//popwindows的第二个界面用于记录已勘察过的站点
    private double cur_lat = 0, cur_lon = 0;//正地址解析
    private double mylocation_lat, mylocation_lon;//初始定位
    private double test_lat = 31.86200, test_lon = 117.26509;//用于验证的经纬度（农大交行　经：117.26509 纬：31.86200)
    private double test_lat_copy = test_lat, test_lon_copy = test_lon;//用于验证的经纬度（农大交行　经：117.26509 纬：31.86200)
    private double test_lat_1 = 31.86200, test_lon_1 = 117.26509;//用于验证的经纬度（农大交行　经：117.26509 纬：31.86200)

    private String nameoflongclick;//用于逆地址解析获取marker的名称
    private station_info get_stationinfo;
    private String string_stationinfo;
    private String city = null;//初始化获取所在位置的城市名
    private String startpoint;//默认的是定位的起点
    // private StringBuilder sb_onroute_routeinfo=new StringBuilder();
    private String[] sz_onroute_routeinfo;//popwindows的第二个界面
    private String[] sz_stationinfo_latlon;//包含了经纬度与地点模式等级的数组
    private String[] sz_routeinfo = {"基本信息", "路线信息", "费用信息", "其他信息"};
    private boolean isfirstlocate = true;
    private boolean useDefaultIcon = false;
    private boolean isfirstspinner_f = false;

    private static final String TAG = "bai_routeplan";

    private point pointinfo;
    private List<point> pointList;
    private boolean hasShownDialogue = false;
    private int flag_firststation = 0; // 此处需要判断传入的点多少，如1个按照要求clicktime不需要-1，但是多个点时需要-1；
   // private double d_onroute_expenseinfo_zuche=0;
    private double d_onroute_expenseinfo_dache=0;
    private double d_onroute_expenseinfo_zijia=0;
    private double d_onroute_expenseinfo;//popwindows的第三个界面用于记录已勘察过的站点后的费用，且为公交
    private double km_zuche=0;//情况特殊，需要单独统计总里程

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//软件开发包初始化；
        setContentView(R.layout.bai_routeplan);
        init_all();
        initLocation();
    }

    public void init_all() {
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        mrouteSearch = RoutePlanSearch.newInstance();
        mrouteSearch.setOnGetRoutePlanResultListener(this);

        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener1();
        mLocationClient.registerLocationListener(mLocationListener);

        get_stationinfo = (station_info) getIntent().getSerializableExtra("repeatinfo");
        string_stationinfo = get_stationinfo.getRepeatstation_latlon();//传递的是站点的经纬度String类型
        sz_stationinfo_latlon = string_stationinfo.split(" ");//也可以直接传数组过来，当然对于worker_mode就必须设置足够大的数组存储
        //可能的不同区站点信息,此时为数组类型
        pointList = new ArrayList<point>() {
        };
        for (int i = 0; i < sz_stationinfo_latlon.length; i++) {
            String s_lat = sz_stationinfo_latlon[i].substring(1, 9);
            double d_lat = Double.parseDouble(s_lat);
            String s_lon = sz_stationinfo_latlon[i].substring(10, 19);
            double d_lon = Double.parseDouble(s_lon);
            String s_level = sz_stationinfo_latlon[i].substring(0, 1);
            int i_level = Integer.parseInt(s_level);
            String s_dianname = sz_stationinfo_latlon[i].substring(19, sz_stationinfo_latlon[i].length());
            pointinfo = new point(d_lat, d_lon, i_level, s_dianname);
            pointList.add(pointinfo);
        }

        Collections.sort(pointList, new Comparator<point>() {
            @Override
            public int compare(point point_1, point point_2) {
                return point_2.getLevel().compareTo(point_1.getLevel());//法二表示：先创建一个Comparator的对象A（正序），在实现方法上
                //用Collections.sort(pointList,A.reversed());另外对于整形参数可以之间用-表示
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        });
        // 尽早申明，表示路线信息的第二个界面，用于存储选中所要勘察的点的最大存储容量  +2表示自身的点与打开第二个界面后的益处上线控制
        sz_onroute_routeinfo = new String[pointList.size() + 2];
        sz_onroute_routeinfo[i_onroute_routeinfo] = "之心城";//在实际应用过程中应以具体的自身位置作为首个参数，应用startpoint
        i_onroute_routeinfo++;
        sz_onroute_routeinfo[1]="请先单击下一集站点后查看路线信息";

        btn_nextroute = findViewById(R.id.btn_nextroute);
        btn_nextroute.setOnClickListener(this);
        btn_routeinfo = findViewById(R.id.btn_routeinfo);
        btn_routeinfo.setOnClickListener(this);
        next_node = findViewById(R.id.next);
        last_node = findViewById(R.id.pre);
        last_node.setVisibility(View.INVISIBLE);
        next_node.setVisibility(View.INVISIBLE);

        drive_way = (Spinner) findViewById(R.id.travel_way);
        drive_way.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(drive_way, AdapterView.INVALID_POSITION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        drive_way.setOnItemSelectedListener(this);
        drive_strategy = (Spinner) findViewById(R.id.drive_strategy);
        drive_strategy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(drive_strategy, AdapterView.INVALID_POSITION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        drive_strategy.setOnItemSelectedListener(this);

        mhandler.sendEmptyMessage(0);//必然转移到handle命令上去

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

    public void addOverlay(makerInfo Info)//添加marker到百度地图上
    {
        BitmapDescriptor mbitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapshow);//!!!!
        OverlayOptions overlayoptions = null;
        Marker marker = null;
        overlayoptions = new MarkerOptions()//
                .position(Info.getLatlng())// 设置marker的位置
                .icon(mbitmap)// 设置marker的图标
                .zIndex(9);// 設置marker的所在层級
        marker = (Marker) baiduMap.addOverlay(overlayoptions);

        Bundle bundle = new Bundle();
        bundle.putSerializable("marker", Info);
        marker.setExtraInfo(bundle);
    }

    public void initmapEvent()//地图长按事件 @param point长按的地理坐标 地图长按事件监听回调函数
    {
        BaiduMap.OnMapLongClickListener mlongclicklistener = new BaiduMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point));
                makerInfo marker = new makerInfo(point.longitude, point.latitude, nameoflongclick);
                addOverlay(marker);
            }
        };
        baiduMap.setOnMapLongClickListener(mlongclicklistener);

        BaiduMap.OnMarkerClickListener mMarkerlist = new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                makerInfo Info = (makerInfo) marker.getExtraInfo().get("marker");
                InfoWindow mInfoWindow;
                //生成一个TextView用户在地图中显示InfoWindow
                TextView location = new TextView(getApplicationContext());
                location.setBackgroundResource(R.drawable.ic_showroute);
                location.setPadding(30, 20, 20, 30);
                location.setText(Info.getContent());
                //将marker所在的经纬度的信息转化成屏幕上的坐标
                final LatLng ll = marker.getPosition();
                Point p = baiduMap.getProjection().toScreenLocation(ll);

                p.y -= 50;//定义mInfoWindow在所单击的marker的上方
                LatLng llInfo = baiduMap.getProjection().fromScreenLocation(p);

                mInfoWindow = new InfoWindow(location, llInfo, -50);//为弹出的InfoWindow添加点击事件(暂未实现)

                baiduMap.showInfoWindow(mInfoWindow);   //显示InfoWindow
                return true;
            }
        };
        baiduMap.setOnMarkerClickListener(mMarkerlist);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {//由于取消了地址编码功能，该函数暂时未用到
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_routeplan.this, "抱歉，未能由地址找到经纬度", Toast.LENGTH_SHORT).show();
            return;
        }
        //   baiduMap.clear();
        //if (null != mMarker) {
        ////    mMarker.remove();
        // }
        cur_lat = result.getLocation().latitude;
        cur_lon = result.getLocation().longitude;
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {//  if (TextUtils.isEmpty(reverseGeoCodeResult.getAddress()))
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_routeplan.this, "抱歉，未能找到您所单击的地址", Toast.LENGTH_LONG).show();
            return;
        }
        //  if (null != mMarker) {
        //      mMarker.remove();
        //   }
        //    baiduMap.clear();
        nameoflongclick = result.getAddress();

    }

    public void travel_search(double station1_lat, double station1_lon, double station2_lat, double station2_lon) {
        PlanNode startNode = PlanNode.withLocation(new LatLng(station1_lat, station1_lon));
        PlanNode endNode = PlanNode.withLocation(new LatLng(station2_lat, station2_lon));
        switch (nowSearchType) {
            case 0://自驾
            case 1://打车
            case 2://租车
                mrouteSearch.drivingSearch((new DrivingRoutePlanOption()).policy(DrivingRoutePlanOption.DrivingPolicy.values()[drivingResultIndex]).from(startNode).to(endNode));
                break;
            case 3:
                //对于公交而言暂时没有policy功能，下面的“合肥市”在实际过程中应该用city代替
                mrouteSearch.transitSearch((new TransitRoutePlanOption()).from(startNode).city("合肥市").to(endNode));
                break;
        }
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
        geoCoder.destroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_routeplan.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {  // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(bai_routeplan.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            next_node.setVisibility(View.VISIBLE);
            last_node.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 1) {
                nowResultwalk = result;
                if (!hasShownDialogue) {
                    bai_routeplan.MyTransitDlg myTransitDlg = new bai_routeplan.MyTransitDlg(bai_routeplan.this, result.getRouteLines(), RouteLineAdapter.Type.WALKING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new bai_routeplan.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            route = nowResultwalk.getRouteLines().get(position);
                            WalkingRouteOverlay overlay = new bai_routeplan.MyWalkingRouteOverlay(baiduMap);
                            baiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultwalk.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                route = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new bai_routeplan.MyWalkingRouteOverlay(baiduMap);
                baiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_routeplan.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            next_node.setVisibility(View.VISIBLE);
            last_node.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 1) {
                nowResultransit = result;
                if (!hasShownDialogue) {
                    bai_routeplan.MyTransitDlg myTransitDlg = new bai_routeplan.MyTransitDlg(bai_routeplan.this,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.TRANSIT_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new bai_routeplan.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {

                            route = nowResultransit.getRouteLines().get(position);
                            TransitRouteOverlay overlay = new bai_routeplan.MyTransitRouteOverlay(baiduMap);
                            baiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultransit.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                            expense_compute();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                route = result.getRouteLines().get(0);
                TransitRouteOverlay overlay = new bai_routeplan.MyTransitRouteOverlay(baiduMap);
                baiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                next_node.setVisibility(View.VISIBLE);
                last_node.setVisibility(View.VISIBLE);
                expense_compute();
            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_routeplan.this, "抱歉，未找到驾车线路", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息?????????????第二次循环时候被执行，故费用则少计算了起始点到底一个站点的钱
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            if (result.getRouteLines().size() > 1) {
                nowResultdrive = result;
                if (!hasShownDialogue) {
                    bai_routeplan.MyTransitDlg myTransitDlg = new bai_routeplan.MyTransitDlg(bai_routeplan.this,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.DRIVING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {//自定义的类中的一个函数
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new bai_routeplan.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            route = nowResultdrive.getRouteLines().get(position);
                            DrivingRouteOverlay overlay = new bai_routeplan.MyDrivingRouteOverlay(baiduMap);
                            baiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultdrive.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                            expense_compute();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                route = result.getRouteLines().get(0);
                DrivingRouteOverlay overlay = new bai_routeplan.MyDrivingRouteOverlay(baiduMap);
                routeOverlay = overlay;
                baiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                next_node.setVisibility(View.VISIBLE);
                last_node.setVisibility(View.VISIBLE);
                //以下为依据自驾、打车、租车的费用计算方式不同在路线详情的第３个界面显示勘察所需的费用问题
               expense_compute();
            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    private void expense_compute()//为了方便代码的阅读，将公交的费用计算放在expense_compute
     {
     switch(nowSearchType)
    {
        case 0://自驾费用计算,不需要考虑累计路程算费用
            double km_zijia = route.getDistance()/1000;
            if(clicktime==0||clicktime==1){
                if(i_lastpointtransit_flag_unpass==1||i_lastpointtransit_flag_pass==1){
                    d_onroute_expenseinfo_zijia+=km_zijia * 1.4;
                }
                else {
                    d_onroute_expenseinfo_zijia=km_zijia * 1.4;
                }
            }
            else{
                d_onroute_expenseinfo_dache+= km_zijia * 1.4;
            }
            break;

        case 1://打车,不需要考虑累计路程算费用
            double km_dache = route.getDistance()/1000;
            if(clicktime==0||clicktime==1){
                if(i_lastpointtransit_flag_unpass==1||i_lastpointtransit_flag_pass==1){
                    d_onroute_expenseinfo_dache+= 8 + km_dache * 4;
                }
                else{
                    d_onroute_expenseinfo_dache= 8 + km_dache * 4;;//此处目的是将bus的费用记为不可增量
                }
            }
            else{
                d_onroute_expenseinfo_dache+= 8 + km_dache * 4;
            }
            d_onroute_expenseinfo_dache+= 8 + km_dache * 4;
            break;

        case 2://租车
            if(clicktime==0||clicktime==1) {
                if(i_lastpointtransit_flag_unpass==1||i_lastpointtransit_flag_pass==1){
                    km_zuche += route.getDistance()/1000;//租车情况最特殊，
                }
               else {
                    km_zuche = route.getDistance()/1000;
                }
            }
            else {
                km_zuche += route.getDistance()/1000;
            }
              break;

        case 3://公交,不需要考虑累计路程算费用

            Calendar calendar=Calendar.getInstance();
            int month= calendar.get(Calendar.MONTH)+1;
            if (month == 6 || month == 7 || month == 8) {
                if(clicktime==0||clicktime==1) {
                    if(i_lastpointtransit_flag_unpass==1||i_lastpointtransit_flag_pass==1){
                        d_onroute_expenseinfo+=2;
                    }
                    //此处目的是将bus的费用记为不可增量，且实现了循环一次后自动从第一站点算起，（尽管没有单击费用信息手动清除）
                    else
                    {
                        d_onroute_expenseinfo = 2;
                    }
                }
                else//如果开始下一站，此刻费用是变化的
                {
                    d_onroute_expenseinfo+=2;
                }
            }
            else
            {
                if(clicktime==0||clicktime==1) {
                    //这是多个站点唯一的可能的两种需要计算费用的情况，且clicktime都为０
                    if(i_lastpointtransit_flag_unpass==1||i_lastpointtransit_flag_pass==1){
                        d_onroute_expenseinfo+=1;
                    }
                    else{
                        d_onroute_expenseinfo = 1;//此处目的是将bus的费用记为不可增量
                    }
                }
                else {
                    d_onroute_expenseinfo+=1;
                }
            }
            break;
    }
}
    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {//驾车
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

    private class MyTransitRouteOverlay extends TransitRouteOverlay {//换乘自定义

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    /*
   private class MyBikingRouteOverlay extends BikingRouteOverlay {//初级版本暂先用不到
       public MyBikingRouteOverlay(BaiduMap baiduMap) {
           super(baiduMap);
       }

       @Override
       public BitmapDescriptor getStartMarker() {
           if (useDefaultIcon) {
               return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
           }
           return null;
       }
       @Override
       public BitmapDescriptor getTerminalMarker() {
           if (useDefaultIcon) {
               return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
           }
           return null;
       }
   }

   private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {//初级版本暂先用不到
       public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
           super(baiduMap);
       }

       @Override
       public BitmapDescriptor getStartMarker() {
           if (useDefaultIcon) {
               return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
           }
           return null;
       }

       @Override
       public BitmapDescriptor getTerminalMarker() {
           if (useDefaultIcon) {
               return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
           }
           return null;
       }
   }
*/
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_nextroute:
                sz_onroute_routeinfo[i_onroute_routeinfo]=pointList.get(clicktime).getZ();

                point pt_info = pointList.get(clicktime);
                if(i_lastpointtransit_flag_unpass ==1)
                {
                    baiduMap.clear();
                    travel_search(test_lat, test_lon,test_lat_copy,test_lon_copy);
                    i_lastpointtransit_flag_unpass =0;
                    i_lastpointtransit_flag_pass=1;//表示此时已完成对所有的点的一次遍历
                    test_lon=test_lon_copy;
                    test_lon=test_lat_copy;
                }
                else
                {
                        baiduMap.clear();
                        clicktime++;
                        i_onroute_routeinfo++;
                    // ???????????????????????????为啥在第二次开始循环的时候会查找路线失败，此时clicktime还是会增１
                        travel_search(test_lat, test_lon, pt_info.getX(), pt_info.getY());//
                        test_lat = pt_info.getX();
                        test_lon = pt_info.getY();
                        if(i_lastpointtransit_flag_pass==1) {
                            sz_onroute_routeinfo = new String[pointList.size() + 2];
                            sz_onroute_routeinfo[0] = "之心城";//在第二次循环的时候先初始化位置//“之心城”在实际中应用startpoint
                            sz_onroute_routeinfo[i_onroute_routeinfo-1]=pointList.get(clicktime-1).getZ();
                        }
                        i_lastpointtransit_flag_pass=0;//务必对变量进行初始恢复

                        if (clicktime >= pointList.size())
                        {
                            clicktime = 0;
                            i_lastpointtransit_flag_unpass =1;
                            route = null;
                            i_onroute_routeinfo =1;//以实现循环后的路线记录也随之变化
                        }
                }
                break;
            case R.id.btn_routeinfo:
                View popupView = bai_routeplan.this.getLayoutInflater().inflate(R.layout.popupwindow, null);
                // TODO: xr come on!
                ListView lsvMore =  popupView.findViewById(R.id.lsvMore);
                lsvMore.setAdapter(new ArrayAdapter<String>(bai_routeplan.this, android.R.layout.simple_list_item_1, sz_routeinfo));
                //创建PopupWindow对象，指定宽度和高度
                PopupWindow window = new PopupWindow(popupView, 400, 600);
                //设置动画
                window.setAnimationStyle(R.style.popup_window_anim);
                //设置背景颜色
                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
                //设置可以获取焦点,如此便可以让单击后list框退出，而activity不退出
                window.setFocusable(true);
                //设置可以触摸弹出框以外的区域
                window.setOutsideTouchable(true);
                // 更新popupwindow的状态
                window.update();
                //以下拉的方式显示，并且可以设置显示的位置
                window.showAsDropDown(btn_routeinfo);
                lsvMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0://打开基本路线信息界面
                                Intent it = new Intent(bai_routeplan.this,onroute_basicinfo.class);
                                it.putExtra("nowSearchType",nowSearchType+" ");
                                startActivity(it);
                                break;
                            case 1://打开已勘察过的站点界面
                                if(i_lastpointtransit_flag_pass==1){//此时已到了终点
                                    String[]sz_copy1_onroute_routeinfo=sz_onroute_routeinfo;
                                    sz_copy1_onroute_routeinfo[0]="之心城finished";
                                    sz_onroute_routeinfo=new String[pointList.size()+2];
                                    sz_onroute_routeinfo[0]="之心城";//在第二次循环的时候先初始化位置//“之心城”在实际中应用startpoint
                                    Bundle b=new Bundle();
                                    b.putStringArray("b",sz_copy1_onroute_routeinfo);
                                    Intent it2 = new Intent(bai_routeplan.this,onroute_routeinfo.class);
                                    it2.putExtras(b);
                                    startActivity(it2);
                                }
                                else {
                                    String[]sz_copy2_onroute_routeinfo=sz_onroute_routeinfo;
                                    sz_copy2_onroute_routeinfo[0]="之心城"+sz_copy2_onroute_routeinfo.length;//在实际中还是应该用startpoint来代替
                                    Bundle b = new Bundle();
                                    b.putStringArray("b", sz_copy2_onroute_routeinfo);
                                    Intent it2 = new Intent(bai_routeplan.this, onroute_routeinfo.class);
                                    it2.putExtras(b);
                                    startActivity(it2);
                                }
                                break;
                            case 2://打开费用界面
                                int d_dache=(int)d_onroute_expenseinfo_dache;
                                int d_bus=(int)d_onroute_expenseinfo;
                                int d_zijia=(int)d_onroute_expenseinfo_zijia;
                                int d_zuche=(int)(260 + km_zuche * 0.01 * 6.53);

                                if(i_lastpointtransit_flag_pass==1){
                                    d_onroute_expenseinfo_dache=0;
                                    d_onroute_expenseinfo_zijia=0;
                                    d_onroute_expenseinfo=0;
                                    km_zuche=0;//租车费用也清空
                                }

                                Intent it3 = new Intent(bai_routeplan.this,onroute_expenseinfo.class);

                                it3.putExtra("nowSearchType",nowSearchType+"");//顺便传递便于路程工具判断的参数
                                it3.putExtra("travel_expense_zijia",d_zijia+"");
                                it3.putExtra("travel_expense_dache",d_dache+"");
                                it3.putExtra("travel_expense_zuche",d_zuche+"");
                                it3.putExtra("travel_expense",d_bus+"");
                                startActivity(it3);
                                break;
                            case 3://打开使用说明界面
                                Intent it4 =new Intent(bai_routeplan.this,onroute_otherinfo.class);
                                startActivity(it4);
                                break;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

           switch (adapterView.getId()) {
               case R.id.travel_way://带有策略的两个点之间的驾车线路规划
                   //  route = null;
                   //  baiduMap.clear();
                   //  index = 0;
                   if (isfirstspinner_f) {
                       // 此处需要判断传入的点多少，如1个按照要求clicktime不需要-1，但是多个点时需要-1；
                       if (clicktime == 0 && i_lastpointtransit_flag_unpass ==0) {//一个点或者任意点不单击
                           baiduMap.clear();
                           point pt_info = pointList.get(clicktime);
                              if (i == 0) {
                               nowSearchType = 0;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }  if (i == 1) {
                               nowSearchType = 1;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 2) {
                               nowSearchType = 2;//??????????????????????? 注意不同
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 3) { //此处为公交换乘
                               nowSearchType = 3;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }
                       }

                       if (pointList.size()>=2 && clicktime== 1) {// 多个点单击一次
                           baiduMap.clear();
                           int clicktime_2=clicktime -1;//为了增强code可读性:2表示第二种情况
                           point pt_info = pointList.get(clicktime_2);
                             if (i == 0) {
                               nowSearchType = 0;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 1) {
                               nowSearchType = 1;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }  if (i == 2) {
                               nowSearchType = 2;//??????????????????????? 注意不同
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 3) { //此处为公交换乘

                               nowSearchType = 3;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }
                       }

                       if (pointList.size() >2 && clicktime >=2) {//2个或更多点单击至最后地前一次
                           baiduMap.clear();
                           int clicktime_3_1 = clicktime - 1;//为了增强code可读性：3表示第三种情况，1表示这种情况的第一个参数
                           int clicktime_3_2 = clicktime - 2;
                           point pt_info_1 = pointList.get(clicktime_3_1);
                           point pt_info_2 = pointList.get(clicktime_3_2);
                             if (i == 0) {
                               nowSearchType = 0;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           } if (i == 1) {
                               nowSearchType = 1;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }if (i == 2) {
                               nowSearchType = 2;//??????????????????????? 注意不同
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }if (i == 3) { //此处为公交换乘
                               nowSearchType = 3;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }
                       }

                       if(clicktime==0&& i_lastpointtransit_flag_unpass ==1&&pointList.size()>=2){//2个以上点过渡情况
                           baiduMap.clear();
                           int clicktime_4_1 = pointList.size()- 1;//为了增强code可读性：3表示第三种情况，1表示这种情况的第一个参数
                           int clicktime_4_2 = pointList.size() - 2;
                           point pt_info_1 = pointList.get(clicktime_4_1);
                           point pt_info_2 = pointList.get(clicktime_4_2);
                             if (i == 0) {
                               nowSearchType = 0;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           } if (i == 1) {
                               nowSearchType = 1;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }if (i == 2) {
                               nowSearchType = 2;//??????????????????????? 注意不同
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }if (i == 3) { //此处为公交换乘
                               nowSearchType = 3;
                               travel_search(pt_info_2.getX(), pt_info_2.getY(), pt_info_1.getX(), pt_info_1.getY());
                           }
                           }

                       if(clicktime==0&& i_lastpointtransit_flag_unpass ==1&&pointList.size()==1){//一个点过渡情况
                           baiduMap.clear();
                           point pt_info = pointList.get(clicktime);
                             if (i == 0) {
                               nowSearchType = 0;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }  if (i == 1) {
                               nowSearchType = 1;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 2) {
                               nowSearchType = 2;//??????????????????????? 注意不同
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           } if (i == 3) { //此处为公交换乘
                               nowSearchType = 3;
                               travel_search(test_lat_1, test_lon_1, pt_info.getX(), pt_info.getY());
                           }
                       }
                       }
                      isfirstspinner_f = true;
                       break;

               case R.id.drive_strategy:

                   if (i == 0) {
                       drivingResultIndex = 0;
                       Toast.makeText(bai_routeplan.this, "避免拥堵", Toast.LENGTH_SHORT).show();
                   }
                   if (i == 1) {
                       drivingResultIndex = 1;
                       Toast.makeText(bai_routeplan.this, "最短距离", Toast.LENGTH_SHORT).show();
                   }
                   if (i == 2) {
                       drivingResultIndex = 2;
                       Toast.makeText(bai_routeplan.this, "费用最少", Toast.LENGTH_SHORT).show();
                   }
                   if (i == 3) {
                       drivingResultIndex = 3;
                       Toast.makeText(bai_routeplan.this, "最短优先", Toast.LENGTH_SHORT).show();
                   }
                   break;
           }
        }

    public void nodeClick(View v) {

        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;
        if (nowSearchType!=4&&nowSearchType != -1) {
            // 非跨城综合交通注：012分别表示自驾、打车、租车，与原工程不同,且令4表示跨城操作
            if (route == null || route.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }

            
            // 获取节结果信息
            step = route.getAllStep().get(nodeIndex);
            if (step instanceof DrivingRouteLine.DrivingStep) {
                nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
            }
             else if (step instanceof TransitRouteLine.TransitStep) {//其余的travel_way基本原理相同
                nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
            }
        }

        else if (nowSearchType == 4) { // 跨城综合交通  综合跨城公交的结果判断方式不一样
            if (massroute == null || massroute.getNewSteps() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
            int size = 0;
            if (isSamecity) {
                size = massroute.getNewSteps().size();
            } else {
                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
                    size += massroute.getNewSteps().get(i).size();
                }
            }
            if (v.getId() == R.id.next) {// 设置节点索引
                if (nodeIndex < size - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            if (isSamecity) {   // 同城
                step = massroute.getNewSteps().get(nodeIndex).get(0);
            } else { // 跨城
                int num = 0;
                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
                    num += massroute.getNewSteps().get(j).size();
                    if (nodeIndex - num < 0) {
                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
                        step = massroute.getNewSteps().get(j).get(k);
                        break;
                    }
                }
            }

            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        // 移动节点至中心
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(bai_routeplan.this);
        popupText.setBackgroundResource(R.drawable.ic_showroute);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        baiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    /*
    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this,
                    "将使用系统起终点图标",
                    Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this,
                    "将使用自定义起终点图标",
                    Toast.LENGTH_SHORT).show();

        }
        useDefaultIcon = !useDefaultIcon;
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
*/
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class MyLocationListener1 implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            baiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder().
                    latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();//生成定位数据
            baiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        //    MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);  //第三个参数是位置图片没有就默认
         //   baiduMap.setMyLocationConfigeration(config);
            city = bdLocation.getCity();
            startpoint=bdLocation.getAddrStr();
            mylocation_lat=bdLocation.getLatitude();
            mylocation_lon=bdLocation.getLongitude();
            myCurrentposition = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());//以我的位置为中心
            if (isfirstlocate) {
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(myCurrentposition));
                isfirstlocate = false;
            }
        }
    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;
        bai_routeplan.OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
            public MyWalkingRouteOverlay(BaiduMap baiduMap) {
                super(baiduMap);
            }

            @Override
            public BitmapDescriptor getStartMarker() {
                if (useDefaultIcon) {
                    return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);//???????????????????????暂先认为该图标用不到
                }
                return null;
            }

            @Override
            public BitmapDescriptor getTerminalMarker() {
                if (useDefaultIcon) {
                    return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
                }
                return null;
            }
        }

        @Override
        public void setOnDismissListener(OnDismissListener listener) {
            super.setOnDismissListener(listener);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick(position);
                    next_node.setVisibility(View.VISIBLE);
                    last_node.setVisibility(View.VISIBLE);
                    dismiss();
                    hasShownDialogue = false;
                }
            });
        }

        public void setOnItemInDlgClickLinster(bai_routeplan.OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }
}
