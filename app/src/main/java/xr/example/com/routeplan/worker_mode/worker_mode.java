package xr.example.com.routeplan.worker_mode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xr.example.com.routeplan.R;
import xr.example.com.routeplan.routeplan.bai_busroute;
import xr.example.com.routeplan.routeplan.bai_driveroute;
import xr.example.com.routeplan.routeplan.bai_routeplan;
import xr.example.com.routeplan.station_info.station_info;

public class worker_mode extends Activity implements View.OnClickListener{

    private JSONObject jsonObject;//把全国的省市区的信息以json的格式保存，解析完成后赋值为null

    private Spinner spinner_sheng;//省
    private Spinner spinner_shi;//市
    private Spinner spinner_qu;//区
    private Spinner spinner_dian;//站点
    private TextView tv_station_explanation;
    private ImageView add_problem;
    private Button drive_search,bus_search,start_worktime,show_station;
    private RadioButton rb_money,rb_project;
    private EditText edt_start_worktime,edt_travel_way;
    private FloatingActionButton floatingActionButton;

    private String provinceName;//省的名字
    public  String dianname_0;
    public  static String convey_baseinfo="";
    private String[] allProv;//所有的省
    private String[] station1;
    private String[] station2;
    private String station2_back="";
    private String[]repeatinfo=new String[100];
    private int repeatstation=0;//选中的站点的个数
    private boolean isfirststation_f=false;

    private StringBuilder sb_showinfo=new StringBuilder();;
    private Map<String, String[]> cityMap = new HashMap<String, String[]>();//key:省p---value:市n
    private Map<String, String[]> areaMap = new HashMap<String, String[]>();//key:市n---value:区s
    private Map<String, String[]> stationMap = new HashMap<String,String[]>();//key:区s-----value：站点d
    private ArrayAdapter<String> provinceAdapter;//省份数据适配器
    private ArrayAdapter<String> cityAdapter;//城市数据适配器
    private ArrayAdapter<String> areaAdapter;//区县数据适配器
    private ArrayAdapter<String> stationAdapter;//位置中的所有站点（包括经纬度信息与业主模式）

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.worker_mode);
        initJsonData();//初始化json数据
        initDatas();//初始化省市区数据
        initView();//初始化控件
        setSpinnerData();//为spinner设置值
    }

    private void initJsonData() {
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = getAssets().open("city.json");//打开json数据
            byte[] by = new byte[is.available()];//转字节,is.available()是获取文件的总大小，是通过文件描述符获取文件的总大小，而并不是事先将磁盘上的文件数据全部读入流中，再获取文件总大小。
            int len = -1;
            while ((len = is.read(by)) != -1) {//在网络中下载文件read（）是阻塞的，read()按字节来读取，不常用，到最后会返回int的-1表示到末尾。
                //read（by）表示读取一定的字节并放在字节数据中
                sb.append(new String(by, 0, len, "gb2312"));//根据字节长度设置编码
            }
            is.close();//关闭流
            jsonObject = new JSONObject(sb.toString());//为json赋值
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //初始化省市区数据
    private void initDatas() {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("citylist");//获取整个json数据
            allProv = new String[jsonArray.length()];//封装数据
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);//jsonArray转jsonObject
                allProv[i] = jsonP.getString("p");  //获取所有的省并封装所有的省
                JSONArray jsonCity = null;
                try {
                    jsonCity = jsonP.getJSONArray("c");//在所有的省中取出所有的市，转jsonArray
                } catch (Exception e) {
                    continue;
                }
                //所有的市
                String[] allCity = new String[jsonCity.length()];//所有市的长度
                for (int j = 0; j < jsonCity.length(); j++) {
                    JSONObject jsonCy = jsonCity.getJSONObject(j);//转jsonObject
                    allCity[j] = jsonCy.getString("n");//取出所有的市,封装市集合
                    JSONArray jsonArea = null;
                    try {
                        jsonArea = jsonCy.getJSONArray("a");//在从所有的市里面取出所有的区,转jsonArray
                    } catch (Exception e) {
                        continue;
                    }
                    String[] allArea = new String[jsonArea.length()];//所有的区
                    for (int k = 0; k < jsonArea.length(); k++) {
                        JSONObject jsonAa = jsonArea.getJSONObject(k);
                        allArea[k] = jsonAa.getString("s");//获取所有的区,封装起来

                         JSONArray jsonstation = null;
                        try {
                            jsonstation = jsonAa.getJSONArray("d");
                        } catch (Exception e) {
                            continue;
                        }
                        String[] allstation = new String[jsonstation.length()];
                        for (int x = 0; x < jsonstation.length(); x++) {
                            JSONObject jsonst = jsonstation.getJSONObject(x);
                            allstation[x] = jsonst.getString("uid");
                        }
                        stationMap.put(allArea[k], allstation);
                    }
                    areaMap.put(allCity[j], allArea);//某个市取出所有的区集合
                }
                cityMap.put(allProv[i], allCity);//某个省取出所有的市,
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonObject = null;//清空所有的数据
    }
    //初始化控件
    @SuppressLint("WrongViewCast")
    private void initView() {
        spinner_sheng = (Spinner) findViewById(R.id.spinner_sheng);
        spinner_shi = (Spinner) findViewById(R.id.spinner_shi);
        spinner_qu = (Spinner) findViewById(R.id.spinner_qu);
        spinner_dian =(Spinner)findViewById(R.id.spinner_dian);
        tv_station_explanation = (TextView) findViewById(R.id.tv_station_explanation);
        add_problem=(ImageView)findViewById(R.id.tv_add_problems);
        add_problem.setOnClickListener(this);

        bus_search=(Button)findViewById(R.id.bus_search);
        bus_search.setOnClickListener(this);
        drive_search=(Button)findViewById(R.id.drive_search);
        drive_search.setOnClickListener(this);
        show_station=findViewById(R.id.show_station);
        show_station.setOnClickListener(this);
        start_worktime=(Button)findViewById(R.id.start_worktime);
        start_worktime.setOnClickListener(this);
        edt_start_worktime=(EditText)findViewById(R.id.edt_start_worktime);
        edt_travel_way=(EditText)findViewById(R.id.edt_travel_way);
        rb_money=findViewById(R.id.rb_money);
        rb_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_project.isChecked()) {
                    rb_project.setChecked(false);
                }
            }
        });
        rb_project=findViewById(R.id.rb_project);
        rb_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rb_money.isChecked()){
                    rb_money.setChecked(false);
                }
            }
        });
        //floatingActionButton=findViewById(R.id.show_station);
     //   floatingActionButton.setOnClickListener(new View.OnClickListener() {
     //       @Override
     //       public void onClick(View view) {
    //        }
    //    });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bus_search:
                startActivity(new Intent(worker_mode.this, bai_busroute.class));
                break;
            case R.id.drive_search:
                startActivity(new Intent(worker_mode.this, bai_driveroute.class));
                break;
            case R.id.tv_add_problems://?????????????????????????????????????????????????????????????????????????????????????????????
                Toast.makeText(worker_mode.this,"问题描述",Toast.LENGTH_SHORT).show();
                break;
            case R.id.start_worktime:
                Intent it=new Intent(worker_mode.this,time_select.class);
                startActivityForResult(it,202);
                break;
         case R.id.show_station:
              if(repeatinfo[0]==null){
                 Snackbar.make(v,"请输入今天要维修的站点",Toast.LENGTH_SHORT).setAction("ok", new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Toast.makeText(worker_mode.this,"ok",Toast.LENGTH_SHORT).show();
                     }
                 }).show();
            }
             else {
                  if(!edt_travel_way.getText().toString().trim().equals("")&&!edt_start_worktime.getText().toString().trim().equals("")) {
                             convey_baseinfo = edt_start_worktime.getText() + "." + (rb_money.isChecked() ? rb_money.getText() :
                              rb_project.getText()) + "." + edt_travel_way.getText();//将传递的基本信息静态变量传给onroute_basicinfo类
                  }
                  station_info si;
                  si = new station_info(station2_back);
                  sb_showinfo.delete(0, sb_showinfo.length());
                  repeatinfo = new String[100];
                  repeatstation = 0;
                  station2_back="";
                  Intent it2 = new Intent(worker_mode.this, bai_routeplan.class);
                  it2.putExtra("repeatinfo", si);
                  startActivity(it2);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent intent){
        if(resultCode==RESULT_OK){
          String date= intent.getStringExtra("date");
          String minute=intent.getStringExtra("minute");
          String hour=intent.getStringExtra("hour");
          String show=date+"   "+hour+"时"+minute+"分";
          edt_start_worktime.setText(show);
        }
    }

    private void setSpinnerData() {
        int selectPosition = 0;//有数据传入时
        provinceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);//系统默认的！知识补充：如何在定义ArrayAdapter<string>对象时，不指定，Amenorrhea，
        //则需要在后面add Ameno字符串数组的内容，实例如下：
        for (int i = 0; i < allProv.length; i++) {
            //给spinner省赋值,设置默认值
            provinceAdapter.add(allProv[i]);//添加每一个省
        }
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//按下的效果
        spinner_sheng.setAdapter(provinceAdapter);
        spinner_sheng.setSelection(selectPosition);//设置选中的省，默认

        cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item); //市
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_shi.setAdapter(cityAdapter);

        areaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);//区县
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_qu.setAdapter(areaAdapter);

        stationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);//站点
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_dian.setAdapter(stationAdapter);
        setListener();//设置spinner的点击监听
    }
    //设置spinner的点击监听
    private void setListener() {
        //省
        spinner_sheng.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                provinceName = parent.getSelectedItem() + "";//获取点击列表spinner item的省名字
                updateprovincebehind(provinceName, null, null,null);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_shi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {        //市
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatecitybehind(parent.getSelectedItem() + "", null,null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_qu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {  //区
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                station2=null;
                station1=null;
                isfirststation_f=false;//保证再次选择其他区的站点时不默认选上
                updateareabehind(parent.getSelectedItem() + "", null);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_dian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//配置编辑框显示
                if (isfirststation_f) {
                // dianname_0 = parent.getSelectedItem() + "";
                    dianname_0=station1[position].substring(0,station1[position].length()-19);
                    String dianname = dianname_0.substring(0, dianname_0.length() - 1);
                    String dianlevel= dianname_0.substring(dianname_0.length()-1,dianname_0.length());
                  if (isrepeatinfo(dianname, repeatinfo)) {
                    Toast.makeText(worker_mode.this,"您所选的站点重复了",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        station2_back=station2_back.concat(dianlevel+station2[position]+dianname+" ");/////////改１
                        sb_showinfo.append("站点名：");
                        repeatinfo[repeatstation] = dianname;
                        repeatstation++;
                        sb_showinfo.append(dianname + " ");
                        sb_showinfo.append("模式：");
                        String detail = show_station_detail(dianname_0);
                        sb_showinfo.append(detail);
                        sb_showinfo.append('\n');
                        tv_station_explanation.setText("各站点的信息如下：\n" + sb_showinfo);
                  }
                }
                isfirststation_f=true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_dian.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(spinner_dian, AdapterView.INVALID_POSITION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
    private String show_station_detail(String detail){
        String R_detail=null;
        String a=detail.substring(detail.length()-1,detail.length());
        int b=Integer.parseInt(a);
        switch (b){
            case 1:
               R_detail= "新建基站模式";
                break;
            case 2:
                R_detail= "代维模式";
                break;
            case 3:
                R_detail= "业主模式";
                break;
            case 4:
                R_detail= "固定模式";
                break;
        }
        return R_detail;
 }

    private boolean isrepeatinfo(String a,String[]b){
        boolean repeat=false;
        if(b==null){repeat=false;}
        else {
            for (int i = 0; i < b.length; i++) {
                if (a.equals(b[i])) {
                    repeat = true;
                    break;
                }
            }
        }
        return repeat;
 }

    private void updateprovincebehind(Object object, Object city, Object area,Object station) {//根据当前的省，更新市和区的信息
        int selectPosition = 0;//有数据时，进行匹配城市，默认选中
        String[] cities = cityMap.get(object);
        cityAdapter.clear();//清空adapter的数据
        for (int i = 0; i < cities.length; i++) {
            if (city != null && city.toString().equals(cities[i])) {//判断传入的市在集合中匹配
                selectPosition = i;
            }
            cityAdapter.add(cities[i]);//将这个列表“市”添加到adapter中
        }
        cityAdapter.notifyDataSetChanged();//刷新
        if (city == null) {
            updatecitybehind(cities[0], null,null);//更新区,没有市则默认第一个给它
        } else {
            spinner_shi.setSelection(selectPosition);
            updatecitybehind(city, area, station);
        }
        }

    private void updatecitybehind(Object object, Object myArea, Object station) {  //根据当前的市，更新区的信息
        int selectPosition = 0;//有数据时，进行匹配城市，默认选中
        String[] area = areaMap.get(object);
        areaAdapter.clear();//清空
        if (area != null) {
            for (int i = 0; i < area.length; i++) {
                if (myArea != null && myArea.toString().equals(area[i])) {
                    selectPosition = i;
                }
                areaAdapter.add(area[i]);//将这个列表“市”添加到adapter中
            }
            areaAdapter.notifyDataSetChanged();//刷新
            if (myArea == null) {
                  updateareabehind(area[0],null);//更新站点,没有区则默认第一个给它
             }
            else{
               spinner_qu.setSelection(selectPosition);
               updateareabehind(myArea,station);
                 }
        }
    }

    private void updateareabehind(Object object, Object station) {
        int selectPosition = 0;//当有数据时，进行匹配地区，默认选中
        station1 = stationMap.get(object);
        stationAdapter.clear();
        if (station1 != null) {//为空没有操作即可
           station2=new String[station1.length];//用于存储各站点的准确位置
            for (int i = 0; i < station1.length; i++) {
                String a = station1[i].substring(0, station1[i].length() -20);
                stationAdapter.add(a);//填入到这个列表
                Pattern pattern = Pattern.compile("[0-9]{2}+(.[0-9]{5})+(,[0-9]{0,3})+(.[0-9]{5})");
                Matcher matcher = pattern.matcher(station1[i]);
               if (matcher.find()) {
                  station2[i]=matcher.group();
              }
            }
            stationAdapter.notifyDataSetChanged();//刷新
            spinner_dian.setSelection(selectPosition);//默认选中

        }
    }
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
