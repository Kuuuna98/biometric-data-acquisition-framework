package com.kaist.iclab.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class PersistentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //애니메이션 효과없이 액티비티 간의 전환

        Log.d("Ria", ">>> PersistentActivity > onCreate : 켜졌다가 꺼집니다.");

        finish(); //꺼짐
    }
}
