package xr.example.com.routeplan.worker_mode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import xr.example.com.routeplan.R;

/**
 * Created by Administrator on 2018/6/8.
 */
public class time_select extends Activity implements View.OnClickListener {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btn_ok;
    private int hour=0,minute=0;
    private String date="2018年6月1号";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.time_select);
        btn_ok=(Button)findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        datePicker = (DatePicker) findViewById(R.id.dpPicker);
        timePicker = (TimePicker) findViewById(R.id.tpPicker);
        datePicker.init(2018, 6, 1, new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                 // 获取一个日历对象，并初始化为当前选中的时间
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
                    date=format.format(calendar.getTime());
                }

       });
              timePicker.setIs24HourView(true);
              timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                   @Override
                   public void onTimeChanged(TimePicker view, int hourOfDay, int minuteofhour) {//自己设置的时间
                  hour=hourOfDay;
                  minute=minuteofhour;
                                 }
               });
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

    @Override
    public void onClick(View v) {
        Intent it=new Intent();
        it.putExtra("hour",hour+"");
        it.putExtra("minute",minute+"");
        it.putExtra("date",date);
        this.setResult(RESULT_OK,it);
        this.finish();

    }
}
