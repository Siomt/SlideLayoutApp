package sun.com.slidelayoutapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import sun.com.slipelayoutlibrary.SlideLayout;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private TextView gone_view;
    private SlideLayout slide;
    private boolean isGone = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    changeState();
                    break;
            }
        }
    };
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        gone_view = (TextView) findViewById(R.id.gone_view);
        slide = (SlideLayout) findViewById(R.id.slide);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();
            }
        });

        timer = new Timer();
        long delay = 0;
        long intevalPeriod = 200;
        timer.scheduleAtFixedRate(timerTask, delay, intevalPeriod);  //执行的时间毫秒

        slide.setOnSlideStatusListener(new SlideLayout.OnSlideStatusListener() {
            @Override
            public void slideOutComplete() {
                Log.d("SHF", "slideOutComplete");
            }

            @Override
            public void slideInComplete() {
                Log.d("SHF", "slideInComplete");
            }
        });
    }

    TimerTask timerTask = new TimerTask() {

        @Override

        public void run() {
// 执行的方法
            handler.sendEmptyMessage(0);
        }

    };

    private void changeState() {
        if (!isGone) {
            gone_view.setVisibility(View.GONE);
        } else {
            gone_view.setVisibility(View.VISIBLE);
        }
        isGone = !isGone;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
        handler.removeCallbacksAndMessages(null);
    }
}
