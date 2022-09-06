package com.knightboot.spwaitkiller;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.LinkedList;

/**
 * created by Knight-ZXW on 2021/9/14
 */
class ProxySWork<T> extends LinkedList<T> {
    private final LinkedList<T> proxy;

    private final Handler sHandler;

    private final AboveAndroid12Processor aboveAndroid12Processor;

    public ProxySWork(LinkedList<T> proxy,
                      Looper looper, AboveAndroid12Processor aboveAndroid12Processor) {
        this.proxy = proxy;
        sHandler = new Handler(looper);
        this.aboveAndroid12Processor = aboveAndroid12Processor;
    }

    // is thread safe
    @NonNull
    @Override
    public Object clone() {
        // <=31
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.S){
            delegateWork();
            return new LinkedList<T>();
        }else {
            return proxy.clone();
        }
    }

    private void delegateWork() {
        if (proxy.size()==0){
            return;
        }
        LinkedList<Runnable> works = (LinkedList<Runnable>)  proxy.clone();
        proxy.clear();
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Runnable w : works) {
                    w.run();
                }
            }
        });
    }

    @Override
    public boolean add(T t) {
        return proxy.add(t);
    }

    @Override
    public int size() {
        //Android 12 change:
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            delegateWork();
            this.aboveAndroid12Processor.reProxySWork();
            return 0;
        }else {
            return proxy.size();
        }
    }

    // is thread safe
    @Override
    public void clear() {
        proxy.clear();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Android 12及以上版本的特殊回调处理
     */
    interface AboveAndroid12Processor {

        /**
         * 重新代理 sWork字段
         */
        public void reProxySWork();

    }
}

