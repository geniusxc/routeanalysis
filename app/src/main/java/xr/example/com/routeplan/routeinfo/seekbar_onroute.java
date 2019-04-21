package xr.example.com.routeplan.routeinfo;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xr.example.com.routeplan.R;

public class seekbar_onroute extends RxAppCompatActivity {
    private TextView seekCurTime, curTime, totalTime;
    private SeekBar seekBar;
    private float moveStep;  //移动步长
    private int screenWidth;
    private static final int TOTALTIME = 300;//5分钟,单位秒
    private int i_curtime;//单位秒
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seekbarprogress);
      //  initView();
      //  initData();
    }

    private void initView() {
        seekCurTime = (TextView) findViewById(R.id.curSeekTime);
        curTime = (TextView) findViewById(R.id.curTime);
        totalTime = (TextView) findViewById(R.id.totalTime);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {//b为true时表示由用户拖动触发，
                                                                                     //b为false是代码更新seekbar位置造成的
                i_curtime = progress * TOTALTIME / 100;
                curTime.setText(Utils.getAudioTime(i_curtime));
                seekCurTime.setText(Utils.getAudioTime(i_curtime));
                setSeekCurTimeLocation(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initData() {
        curTime.setText(Utils.getAudioTime(0));
        totalTime.setText(Utils.getAudioTime(TOTALTIME));
        seekBar.setMax(100);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth() - 60;
        moveStep = ((float) screenWidth / 100) * 1.0f;
        Observable.interval(1, TimeUnit.SECONDS)
                .compose(this.<Long>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long aLong) {
                        i_curtime++;
                        int progress = (int) ((float) i_curtime / TOTALTIME * 100);
                        seekBar.setProgress(progress);
                        curTime.setText(Utils.getAudioTime(i_curtime));
                        seekCurTime.setText(Utils.getAudioTime(i_curtime));
                        setSeekCurTimeLocation(progress);
                    }
                });
    }
    private void setSeekCurTimeLocation(int progress) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout
                .LayoutParams) seekCurTime.getLayoutParams();
        int marginStart = (int) (progress * moveStep - seekCurTime.getWidth() / 2);
        if (marginStart <= seekBar.getWidth() -seekCurTime.getWidth())
        {
            layoutParams.setMarginStart(marginStart);
        }
        seekCurTime.setLayoutParams(layoutParams);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
