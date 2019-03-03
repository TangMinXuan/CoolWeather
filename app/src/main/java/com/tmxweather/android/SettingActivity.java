package com.tmxweather.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity
{
    private List<String> setList;

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_setting );

        initSetList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                SettingActivity.this,android.R.layout.simple_list_item_1,setList );
        ListView listView = findViewById( R.id.set_list );
        listView.setAdapter( adapter );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String chooseItem = setList.get( position );
                if (chooseItem.equals( "关于氢天气" ))
                {
                    Intent intent = new Intent( SettingActivity.this,AboutActivity.class );
                    intent.putExtra( "extra_data","AppInfo" );
                    startActivity( intent );
                }
                else if (chooseItem.equals( "关于作者" ))
                {
                    Intent intent = new Intent( SettingActivity.this,AboutActivity.class );
                    intent.putExtra( "extra_data","AuthorInfo" );
                    startActivity( intent );
                }
            }
        } );

        backButton = findViewById( R.id.back_button );
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent( SettingActivity.this,WeatherActivity.class );
                startActivity( intent );
            }
        } );
    }

    public void initSetList()
    {
        setList = new ArrayList<>();

        setList.add( "关于氢天气" );
        setList.add( "关于作者" );
    }
}
