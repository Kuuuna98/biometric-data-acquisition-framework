package com.kaist.iclab.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.kaist.iclab.R;

import java.io.File;
import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class FileChooseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        Button choiceBut = (Button)findViewById(R.id.choiceBut);
        Button cancleBut = (Button)findViewById(R.id.cancleBut);

        // 빈 데이터 리스트 생성.
        final ArrayList<String> items = new ArrayList<String>() ;

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items) ;

        // listview 생성 및 adapter 지정.
        final ListView listview = (ListView) findViewById(R.id.fileList) ;
        listview.setAdapter(adapter) ;

        File[] listFiles = (new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/E4_sensing")).listFiles();
        for (int i=0; i< listFiles.length; i++){
            Log.d("testFile", "testFile: "+listFiles[i].getName());
            items.add(i+1 + ". " + listFiles[i].getName());// 아이템 추가.
        }
        adapter.notifyDataSetChanged();// listview 갱신

        choiceBut.setOnClickListener(new Button.OnClickListener() { //선택
            public void onClick(View v) {
                SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
                int count = adapter.getCount() ;
                int checked = 0;


                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)){ checked ++; }
                }
                String[] checkedItem = new String[checked];
                checked = 0;

                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        Log.d(TAG, "onClick: "+items.get(i));
                        checkedItem[checked] = items.get(i);
                        checked ++;
                    }
                }
                // 모든 선택 상태 초기화.
                listview.clearChoices() ;
                adapter.notifyDataSetChanged();

                getIntent().putExtra("transferList", checkedItem);
                //getIntent().putExtra("test", "why not?"); //please return your value! You can do it!!#!#(@#$@Q
                setResult(RESULT_OK, getIntent());
                finish();
            }
        }) ;

        cancleBut.setOnClickListener(new Button.OnClickListener() { //취소
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }) ;
    }
}
