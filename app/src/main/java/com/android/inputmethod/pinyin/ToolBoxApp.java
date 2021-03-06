package com.android.inputmethod.pinyin;

import android.app.Application;

import com.getkeepsafe.relinker.ReLinker;
import com.tencent.mmkv.MMKV;

import java.io.File;

/**
 * Created by sunhang on 10/03/2018.
 * WeShineApp可以看出是一个单例
 */
public class ToolBoxApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        initMMKV();

    }

    private void initMMKV() {
        String dir = getFilesDir().getAbsolutePath() + File.separator + "mmkv";
        MMKV.initialize(dir, libName -> ReLinker.loadLibrary(this, libName));
    }


}
