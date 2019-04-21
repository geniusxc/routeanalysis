package xr.example.com.routeplan.routeinfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import xr.example.com.routeplan.R;

/**
 * Created by Administrator on 2018/6/27.
 */

public class onroute_expenseinfo extends Activity {
    EditText travel_expense;
   // StringBuilder sb=new StringBuilder();
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routeinfo_expense);
        init_all();

    }

    private void init_all() {
        String s_travel_expense=getIntent().getStringExtra("travel_expense");
        String s_travel_expense_zijia=getIntent().getStringExtra("travel_expense_zijia");
        String s_travel_expense_dache=getIntent().getStringExtra("travel_expense_dache");
        String s_travel_expense_zuche=getIntent().getStringExtra("travel_expense_zuche");

        String s_nowSearchType=getIntent().getStringExtra("nowSearchType");
        int i_nowSearchType=Integer.parseInt(s_nowSearchType);

        travel_expense=(EditText) findViewById(R.id.travel_expense);
        switch (i_nowSearchType){
            case 0:
                travel_expense.setText(s_travel_expense_zijia+"元");
                break;
            case 1:
                travel_expense.setText(s_travel_expense_dache+"元");
                break;
            case 2:
                travel_expense.setText(s_travel_expense_zuche+"元");
                break;
            case 3:
                travel_expense.setText(s_travel_expense+"元");//公交费用显示
                break;
        }

    }
}
