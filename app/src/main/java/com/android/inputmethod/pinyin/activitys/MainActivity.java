package com.android.inputmethod.pinyin.activitys;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.android.inputmethod.pinyin.R;
import com.android.inputmethod.pinyin.adapters.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewpager = (ViewPager) findViewById(R.id.view_pager);
        //预加载
        mViewpager.setOffscreenPageLimit(2);
        //设置适配器
        mViewpager.setAdapter(new SectionsPagerAdapter(this,getSupportFragmentManager()));
        //将TabLayout 与ViewPager进行绑定
        mTabLayout.setupWithViewPager(mViewpager);

    }
}