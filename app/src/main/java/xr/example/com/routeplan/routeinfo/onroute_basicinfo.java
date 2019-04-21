package xr.example.com.routeplan.routeinfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import xr.example.com.routeplan.R;
import xr.example.com.routeplan.worker_mode.worker_mode;

/**
 * Created by Administrator on 2018/6/27.
 */

public class onroute_basicinfo extends Activity {
    TextView routeinfo;
   private String[]sz_basic_info;
   StringBuilder sb_basic_info=new StringBuilder();
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routeinfo);
        init_all();

    }

    private void init_all() {
        routeinfo=(TextView)findViewById(R.id.onroute_basicinfo);
        String copy_convey_baseinfo=worker_mode.convey_baseinfo;
        String convey_nowSearchType=getIntent().getStringExtra("nowSearchType");
        if(!copy_convey_baseinfo.equals("")) {//一定要注意null仍然会有空指针引用错误
            sz_basic_info=copy_convey_baseinfo.split("\\.");
            sb_basic_info.append("时间信息为：");
            sb_basic_info.append(sz_basic_info[0]+"\n");
            sb_basic_info.append("出行模式选择为：");
            sb_basic_info.append(sz_basic_info[1]+"\n");
            sb_basic_info.append("实际驾车方式为：");
            int i_nowSearchType=Integer.parseInt(convey_nowSearchType);
            switch (i_nowSearchType) {
                case 0:
                    sb_basic_info.append("自驾");
                    routeinfo.setText(sb_basic_info);
                    break;
                case 1:
                    sb_basic_info.append("打车");
                    routeinfo.setText(sb_basic_info);
                    break;
                case 2:
                    sb_basic_info.append("租车");
                    routeinfo.setText(sb_basic_info);
                    break;
                case 3:
                    sb_basic_info.append("公交");
                    routeinfo.setText(sb_basic_info);//公交费用显示
                    break;
            }
        }
    }
}
