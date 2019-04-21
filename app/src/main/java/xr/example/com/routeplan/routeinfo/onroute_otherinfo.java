package xr.example.com.routeplan.routeinfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import xr.example.com.routeplan.R;

/**
 * Created by Administrator on 2018/6/27.
 */

public class onroute_otherinfo extends Activity {
    TextView onroute_other_explanation;
    String on_routeinfo_explanation=
                             "1,使用时，先选择自己需要出行的方式，包括自驾、打车、租车、公交，先判断是否能查找到路线，然后能则选择“下一基站点”"
                             +"，则第一次显示的是从起点到第一个按工程优先（默认）的权值最大的站点的路线，然后一次按权值减小规划下一个路线，同时"
                             + "一次只显示两点之间的路线。"
                             +"\n2,在两点之间规划路线时，会弹出一个Dialog，上面显示的是多条可供选择的路线信息，公交规划考虑实际的意义，仅显示两点" +
                             "之间的时间与按月份所需的费用，drive规划则显示两点之间的路程与红绿灯数还有时间，路程则为下面详情了解截面计算费用提供了" +
                             "依据，而时间则为一天的工程进度提供了参考（具体怎么应用尚在思考）"
                             +"\n3,一次循环后如果不查看路线信息费用信息，则默认是费用接着上个循环的费用累加，如果查看则按照两点之间的实际费用从新计费。" +
                             "路线信息则清空从第一个点开始从新记录，原始起点不清空"
                             +"\n4,驾车策略默认是躲避拥堵"
                             +"\n5,下面的左右的两个箭头则可以显示所规划的两点之间的各个站点的信息";

    StringBuilder sb=new StringBuilder();
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routeinfo_other);
        init_all();
    }

    private void init_all() {
        onroute_other_explanation=findViewById(R.id.onroute_other_explanation);
        onroute_other_explanation.setText("该软件的使用说明如下：\n"+on_routeinfo_explanation);
    }
}
