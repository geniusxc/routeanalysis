package xr.example.com.routeplan.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import xr.example.com.routeplan.R;
import xr.example.com.routeplan.manager_mode.manager_LoginActivity;
import xr.example.com.routeplan.worker_mode.worker_LoginActivity;

/**
 * Created by Administrator on 2018/5/20.
 */

public class Main extends Activity implements View.OnClickListener{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View headView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        navigationView =  findViewById(R.id.navigationView);
        disableNavigationViewScrollbars(navigationView);  //去掉navigationView的滚动条
        headView = navigationView.getHeaderView(0);
        headView.setOnClickListener(this);
        drawerLayout =findViewById(R.id.drawerLayout);
        navigationView.setItemIconTintList(null); //设置menu中的图标是正常的颜色
       // 设置导航菜单宽度(设置为屏幕的二分之一)
       // ViewGroup.LayoutParams params = navigationView.getLayoutParams();
      //  params.width = getResources().getDisplayMetrics().widthPixels * 1 / 2;
      //  navigationView.setLayoutParams(params);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { //navigationView的菜单条目的点击监听 
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.my_parse:
                    Toast.makeText(Main.this, "我的钱包", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.my_performance:
                    Toast.makeText(Main.this, "我的精彩", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.com_info:
                    Toast.makeText(Main.this, "公司消息", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.use_explanation:
                    Toast.makeText(Main.this, "使用说明", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.version_improve:
                    Toast.makeText(Main.this, "版本更新", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.run1:
                    Toast.makeText(Main.this, "点击了run1", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.run2:
                    Toast.makeText(Main.this, "点击了run2", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.run3:
                    Toast.makeText(Main.this, "点击了run3", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.run_more:
                    Toast.makeText(Main.this, "点击了run_more", Toast.LENGTH_SHORT).show();
                    break;
            }
            //设置item选中
            item.setCheckable(true);
            //关闭抽屉,或者drawerLayout.closeDrawer(navigationView);
            drawerLayout.closeDrawers();
            return true;
        }
    });
}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_view:
                Toast.makeText(Main.this, "点击了头布局", Toast.LENGTH_SHORT).show();
                //关闭抽屉,或者drawerLayout.closeDrawer(navigationView);
                drawerLayout.closeDrawers();
                break;
        }
    }
    //去掉navigationView的滚动条（这个滚动条不在NavigationView中，而是在他的child—NavigationMenuView中，所以解决办法就是对NavigationView调用 下面这个方法：）
    private void disableNavigationViewScrollbars(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

//    //配置ActionBar的home键点击监听
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                //打開左側的抽屜
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    drawerLayout.openDrawer(GravityCompat.START);
//                }
//        }
//        return super.onOptionsItemSelected(item);
//    }
    public void worker_mode(View view) {
        startActivity(new Intent(Main.this, worker_LoginActivity.class));
    }

    public void manager_mode(View view) {
        startActivity(new Intent(Main.this, manager_LoginActivity.class));
    }

   }



