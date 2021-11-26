package cn.skyui.library.chart.kline.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_style).performClick();
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId())
        {
            case R.id.btn_style:
                intent.setClass(this, MyExampleActivity.class);
                break;
            case R.id.btn_style1:
                intent.setClass(this, ExampleActivity.class);
                intent.putExtra("type",0);
                break;
            case R.id.btn_style2:
                intent.setClass(this,ExampleActivity.class);
                intent.putExtra("type",1);
                break;
            case R.id.btn_loadmore:
                intent.setClass(this, LoadMoreActivity.class);
                break;
            case R.id.btn_minute:
                intent.setClass(this, MinuteChartActivity.class);
                break;
        }
        startActivity(intent);
    }
}
