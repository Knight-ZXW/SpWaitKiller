package com.knightboot.spwaitkiller;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.LinkedList;

/**
 * created by Knight-ZXW on 2021/9/14
 */
class ProxySWork<T> extends LinkedList<T> {
    private LinkedList<T> proxy;

    private Handler sHandler;

    public ProxySWork(LinkedList<T> proxy, Looper looper) {
        this.proxy = proxy;
        sHandler = new Handler(looper);
    }

    // is thread safe
    @NonNull
    @Override
    public Object clone() {
        LinkedList<Runnable> works = (LinkedList<Runnable>) super.clone();
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Runnable w : works) {
                    w.run();
                }
            }
        });
        return new LinkedList<T>();
    }

    // is thread safe
    @Override
    public void clear() {
        proxy.clear();
    }

    @Override
    public boolean isEmpty() {
        return proxy.isEmpty();
    }
}

