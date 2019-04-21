package xr.example.com.routeplan.explanation_class;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import xr.example.com.routeplan.R;

public class about extends Activity {//定义使用说明类

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.activity_about);
    }
    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
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
}
