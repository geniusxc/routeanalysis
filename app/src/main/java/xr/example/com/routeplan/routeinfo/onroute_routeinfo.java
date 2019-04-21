package xr.example.com.routeplan.routeinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import xr.example.com.routeplan.R;

/**
 * Created by Administrator on 2018/6/27.
 */

public class onroute_routeinfo extends Activity {
    TextView onroute_routeinfo;
    StringBuilder sb=new StringBuilder();
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routeinfo_route);
        init_all();
    }

    private void init_all() {
        Bundle b=this.getIntent().getExtras();
        String[]array=b.getStringArray("b");
        onroute_routeinfo=(TextView)findViewById(R.id.onroute_routeinfo);
        if(array[1].substring(0,1).equals("请")){
            onroute_routeinfo.setText("请先单击下一基站点！");
        }
        else {
            if ("d".equals(array[0].substring(array[0].length() - 1, array[0].length()))) {
                sb.append("已经勘察完的站点有：\n");
                sb.append("起点：");
                sb.append(array[0].substring(0, array[0].length() - 8) + "\n");
                int i = 1;
                do {
                    sb.append("第" + i + "个站点：");
                    sb.append(array[i] + "\n");
                    i++;
                } while (array[i] != null);
                sb.append("已全部完成任务！");
                onroute_routeinfo.setText(sb);
            } else {
                sb.append("已经勘察完的站点有：\n");
                sb.append("起点：");
                sb.append(array[0].substring(0, array[0].length() - 1) + "\n");// 此处仍有瑕疵，但实际过程中一天虽理论上能够维修两位数的站点，但不现实!
                int i = 1;
                do {
                    sb.append("第" + i + "个站点：");//因为第一个站点并没有检车是否已经单击了下一个基站点，所有对于数组的初始化应予以说明
                    sb.append(array[i] + "\n");
                    i++;
                } while (array[i] != null);
                String s_stationleft = array[0].substring(array[0].length() - 1, array[0].length());
                double i_stationleft = Double.parseDouble(s_stationleft) - i - 1;
                if (i_stationleft == 0) {
                    sb.append("已全部完成任务！但是还未返回");
                    onroute_routeinfo.setText(sb);
                } else {
                    sb.append("还剩" + i_stationleft + "个站点尚未完成！");
                    onroute_routeinfo.setText(sb);
                }
            }
        }
    }
    public void ok(View v){
        startActivity(new Intent(onroute_routeinfo.this,seekbar_onroute.class));
    }

}
