package com.knightboot.spwaitkiller.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.knightboot.spwaitkiller.SpWaitKiller;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {


    private static final String TAG ="SpWaitKillerTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
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
            @SuppressLint("SoonBlockedPrivateApi") Field sWorkField = QueuedWorkClass.getDeclaredField("sWork");
            sWorkField.setAccessible(true);

            LinkedList<Runnable> sWork = (LinkedList) sWorkField.get(null);
            Field sFinishersField = QueuedWorkClass.getDeclaredField("sFinishers");
            sFinishersField.setAccessible(true);
            Collection finishers = (Collection) sFinishersField.get(null);

            final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);
            Runnable wait = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG,"wait runnable run on thread "+Thread.currentThread().getId()+", is MainThread ? "+
                            ((Thread.currentThread().getId() ==Looper.getMainLooper().getThread().getId())?" true":" false"));
                    try {
                        writtenToDiskLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG,"wait runnable end");
                }
            };
            finishers.add(wait);

            sWork.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e(TAG,"run work "+this+" on Thread "+Thread.currentThread().getName()+" begin");
                        Thread.sleep(blockSeconds*1000);
                        writtenToDiskLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG,"run work "+this+" on Thread "+Thread.currentThread().getName() +" finish");
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