package com.tmxweather.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity
{
    private TextView infoTitle;

    private TextView infoText;

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_about );

        infoTitle = findViewById( R.id.info_title );
        infoText = findViewById( R.id.info_text );
        backButton = findViewById( R.id.back_button );

        Intent intent = getIntent();
        String flag = intent.getStringExtra( "extra_data" );
        if (flag.equals( "AppInfo" ))
        {
            showAppInfo();
        }
        else if (flag.equals( "AuthorInfo" ))
        {
            showAuthorInfo();
        }

        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( AboutActivity.this, SettingActivity.class );
                startActivity( intent );
            }
        });
    }

    public void showAppInfo()
    {
        infoTitle.setText( "关于氢天气v1.0" );
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append( "这款App主要实现了预报天气的功能\n" );
        stringBuilder.append( "第一次打开App会记录您选择的城市，作为缓存存入手机\n" );
        stringBuilder.append( "在天气界面右划或者点击“房子”按钮可以打开抽屉，重新选择需要预报的城市\n" );
        stringBuilder.append( "App的后台服务每隔8小时更新一次天气情况，每隔24小时自动更换一次界面背景\n" );

        infoText.setText( stringBuilder );
    }

    public void showAuthorInfo()
    {
        infoTitle.setText( "关于我" );
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append( "很高兴您能下载并使用这款App\n" );
        stringBuilder.append( "这是我利用课余时间学习并编写的第一款功能简单的天气App\n" );
        stringBuilder.append( "同时也是我上线的第一款App\n" );
        stringBuilder.append( "虽然不得不承认与市面上成熟的App相比仍有较大差距\n" );
        stringBuilder.append( "但作为初学者来说，我已经很心满意足了，算是对一段时间的学习的总结吧\n" );
        stringBuilder.append( "继续学习ing……\n" );
        stringBuilder.append( "本人QQ：741950370，有想法，建议什么的欢迎联系:-)\n" );

        infoText.setText( stringBuilder );
    }
}
