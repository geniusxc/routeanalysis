package xr.example.com.routeplan.manager_mode;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import xr.example.com.routeplan.R;

public class manager_LoginActivity extends Activity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText accountEdit;
    private EditText passwordEdit;
    private CheckBox rememberPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.activity_login);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);

        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            // 将账号和密码都设置到文本框中
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
    }
        public void login (View v){
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        // 如果账号是admin且密码是123456，就认为登录成功
        if (account.equals("admin") && password.equals("123456")) {
            editor = pref.edit();
            if (rememberPass.isChecked()) { // 检查复选框是否被选中
                editor.putBoolean("remember_password", true);
                editor.putString("account", account);
                editor.putString("password", password);
            }
            else
            {
                editor.clear();
            }
            editor.apply();
            Intent intent = new Intent(manager_LoginActivity.this, manager_mode.class);
            startActivity(intent);
            finish();
        }
        else
            {
            Toast.makeText(manager_LoginActivity.this, "account or password is invalid",
                    Toast.LENGTH_SHORT).show();
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
