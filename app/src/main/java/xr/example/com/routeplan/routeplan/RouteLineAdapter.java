package xr.example.com.routeplan.routeplan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;

import java.util.List;

import xr.example.com.routeplan.R;

public class RouteLineAdapter extends BaseAdapter {

    private List<? extends RouteLine> routeLines;
    private LayoutInflater layoutInflater;
    private Type mtype;

    public RouteLineAdapter(Context context, List<? extends RouteLine> routeLines, Type type) {
        this.routeLines = routeLines;
        layoutInflater = LayoutInflater.from(context);
        mtype = type;
    }
    private class NodeViewHolder {
        private TextView name;
        private TextView lightNum;
        private TextView dis;
    }
    public enum Type {
        MASS_TRANSIT_ROUTE, // 综合交通
        TRANSIT_ROUTE, // 公交
        DRIVING_ROUTE, // 驾车
        WALKING_ROUTE, // 步行
      //  BIKING_ROUTE // 骑行
    }
    @Override
    public int getCount() {//暂先认为返回值不为0时执行getview()
        return routeLines.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NodeViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_transit_item, null);
            holder = new NodeViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.transitName);
            holder.lightNum = (TextView) convertView.findViewById(R.id.lightNum);
            holder.dis = (TextView) convertView.findViewById(R.id.dis);
            convertView.setTag(holder);
        }
        else
        {
            holder = (NodeViewHolder) convertView.getTag();
        }

        switch (mtype) {
            case TRANSIT_ROUTE:
                holder.name.setText("路线" + (position + 1));
                int bus_time = routeLines.get(position).getDuration();//没有费用参数，默认为2元
                if (bus_time / 3600 == 0) {
                    holder.lightNum.setText("大约需要：" + bus_time / 60 + "分钟");//对公交与步行而言是获得时间，也为后面与时间影响因子相结合使用
                }
                else {
                    holder.lightNum.setText("大约需要：" + bus_time / 3600 + "小时" + (bus_time % 3600) / 60 + "分钟");
                }
                holder.dis.setText("除了旺季6，7，8月份2元，其余1元");//在此app中路程暂定与时间具有直接关系，由实际app需求知bus路程多元无意义
                break;

            case DRIVING_ROUTE:
                DrivingRouteLine drivingRouteLine = (DrivingRouteLine) routeLines.get(position);//纯粹多此一举
                holder.name.setText("线路" + (position + 1));
                int drive_time = drivingRouteLine.getDuration();//有其实际的意义
                if (drive_time / 3600 == 0) {
                    holder.lightNum.setText("大约需要：" + drive_time / 60 + "分钟"+"红绿灯数：" + drivingRouteLine.getLightNum());
                }
                else {
                    holder.lightNum.setText("大约需要：" + drive_time / 3600 + "小时" + (drive_time % 3600) / 60 + "分钟"+"红绿灯数：" + drivingRouteLine.getLightNum());
                }
                //getDistance()与getcongestionDistance()有区别
                holder.dis.setText("拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米"+"  距离为：" + drivingRouteLine.getDistance() + "米");
                break;
            case WALKING_ROUTE:
            case MASS_TRANSIT_ROUTE:
            //    MassTransitRouteLine massTransitRouteLine = (MassTransitRouteLine) routeLines.get(position);
          //      holder.name.setText("线路" + (position + 1));
           //     holder.lightNum.setText("预计达到时间：" + massTransitRouteLine.getArriveTime());
           //    holder.dis.setText("总票价：" + massTransitRouteLine.getPrice() + "元");
                break;

            default:
                break;
        }

        return convertView;
    }
}
