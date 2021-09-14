package com.knightboot.spwaitkiller.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.knightboot.spwaitkiller.R;
import com.knightboot.spwaitkiller.Reflection;
import com.knightboot.spwaitkiller.SpWaitKiller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Reflection.unseal(this);
        init();
    }

    private void init(){

        findViewById(R.id.btn_mode_case1)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SpWaitKiller.builder(getApplication())
                                .build()
                                .work();
                    }
                });


        findViewById(R.id.mock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mockInsertHeavyWorkToQueuedWork(5);
            }
        });


        findViewById(R.id.jump_to_secondActivity)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void mockInsertHeavyWorkToQueuedWork(int blockSeconds){
        try {
            Class QueuedWorkClass  = Class.forName("android.app.QueuedWork");
            Method method = QueuedWorkClass.getDeclaredMethod("getHandler");
            method.setAccessible(true);
            Handler handler = (android.os.Handler) method.invoke(null);
            Looper looper = handler.getLooper();
            Field sWorkField = QueuedWorkClass.getDeclaredField("sWork");
            sWorkField.setAccessible(true);

            LinkedList<Runnable> sWork = (LinkedList) sWorkField.get(null);
            Field sFinishersField = QueuedWorkClass.getDeclaredField("sFinishers");
            sFinishersField.setAccessible(true);
            Collection finishers = (Collection) sFinishersField.get(null);

            final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);
            Runnable wait = new Runnable() {
                @Override
                public void run() {
                    Log.e("zxw","wait runnable run on thread "+Thread.currentThread().getId()+", is MainThread ? "+
                            ((Thread.currentThread().getId() ==Looper.getMainLooper().getThread().getId())?" true":" false"));

                    try {
                        writtenToDiskLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            finishers.add(wait);
            sWork.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("zxw","run work on Thread "+Thread.currentThread().getId());
                        Thread.sleep(blockSeconds*1000);
                        writtenToDiskLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("zxw","run work on Thread "+Thread.currentThread().getId() +" finish");
                }
            });
            //触发任务执行
            getSharedPreferences("test",MODE_PRIVATE)
                    .edit().putString("k","v")
                    .apply();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}