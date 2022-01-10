package com.knightboot.spwaitkiller;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public class SpWaitKiller {

    private HiddenApiExempter hiddenApiExempter;

    private boolean worked;

    private final boolean neverWaitingFinishQueue;
    private final boolean neverProcessWorkOnMainThread;

    private final UnExpectExceptionCatcher unExpectExceptionCatcher;
    private int targetSdkVersion =0;
    private Context mContext;

    private SpWaitKiller(SpWaitKiller.Builder builder) {
        if (builder.hiddenApiExempter == null) {
            builder.hiddenApiExempter = new DefaultHiddenApiExempter();
        }
        if (builder.unExpectExceptionCatcher ==null){
            builder.unExpectExceptionCatcher = new UnExpectExceptionCatcher() {
                @Override
                public void onException(Throwable ex) {
                    Log.e("SpWaitKillerException","catch Exception \n"
                            +Log.getStackTraceString(ex));
                }
            };
        }
        this.hiddenApiExempter = builder.hiddenApiExempter;
        this.neverProcessWorkOnMainThread = builder.neverProcessWorkOnMainThread;
        this.neverWaitingFinishQueue = builder.neverWaitingFinishQueue;
        this.mContext = builder.context;
        this.unExpectExceptionCatcher =builder.unExpectExceptionCatcher;
        this.targetSdkVersion =  this.mContext.getApplicationInfo().targetSdkVersion;


    }

    public static SpWaitKiller.Builder builder(Context context) {
        return new Builder(context);
    }


    public void work() {
        try {
            if (worked) {
                return;
            }
            realWork();
            worked = true;
        } catch (Exception e) {
            unExpectExceptionCatcher.onException(e);
        }
    }

    private void realWork() throws Exception {
        Class QueuedWorkClass = Class.forName("android.app.QueuedWork");

        if (neverWaitingFinishQueue) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Field sPendingWorkFinishersField = QueuedWorkClass.getDeclaredField("sPendingWorkFinishers");
                sPendingWorkFinishersField.setAccessible(true);

                ConcurrentLinkedQueue sPendingWorkFinishers = (ConcurrentLinkedQueue) sPendingWorkFinishersField.get(null);
                ProxyFinishersLinkedList proxyedSFinishers = new ProxyFinishersLinkedList(sPendingWorkFinishers);
                sPendingWorkFinishersField.set(null, proxyedSFinishers);
            } else if (Build.VERSION.SDK_INT <=Build.VERSION_CODES.R){
                Field sFinishersField = QueuedWorkClass.getDeclaredField("sFinishers");
                sFinishersField.setAccessible(true);

                LinkedList sFinishers = (LinkedList) sFinishersField.get(null);
                ProxyFinishersList proxyedSFinishers = new ProxyFinishersList(sFinishers);
                sFinishersField.set(null, proxyedSFinishers);
            }
        }

        if (neverProcessWorkOnMainThread) {
            // 通过调用 getHandler函数
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
             || Build.VERSION.SDK_INT >Build.VERSION_CODES.R) return;

            if (targetSdkVersion>=Build.VERSION_CODES.R){
                this.hiddenApiExempter.exempt(mContext);
            }

            Method method = QueuedWorkClass.getDeclaredMethod("getHandler");
            method.setAccessible(true);

            Handler handler = (android.os.Handler) method.invoke(null);
            Looper looper = handler.getLooper();

            Field sWorkField = QueuedWorkClass.getDeclaredField("sWork");
            sWorkField.setAccessible(true);

            Field sProcessingWorkField = QueuedWorkClass.getDeclaredField("sProcessingWork");
            sProcessingWorkField.setAccessible(true);
            Object lock = sProcessingWorkField.get(null);

            LinkedList sWork = (LinkedList) sWorkField.get(null);
            ProxySWork proxyedsWork = new ProxySWork(sWork, looper);
            synchronized (lock) {
                sWorkField.set(null, proxyedsWork);
            }

        }

    }

    public static class Builder {
        private boolean neverWaitingFinishQueue;
        private boolean neverProcessWorkOnMainThread;
        private UnExpectExceptionCatcher unExpectExceptionCatcher;


        private Builder(Context context) {
            this.context = context;
            this.neverWaitingFinishQueue = true;
            this.neverProcessWorkOnMainThread = true;
        }

        Context context;
        HiddenApiExempter hiddenApiExempter;

        public Builder hiddenApiExempter(HiddenApiExempter hiddenApiExempter) {
            this.hiddenApiExempter = hiddenApiExempter;
            return this;
        }

        public Builder neverWaitingFinishQueue(boolean neverWaitingFinishQueue) {
            this.neverWaitingFinishQueue = neverWaitingFinishQueue;
            return this;
        }

        public Builder unExpectExceptionCatcher(UnExpectExceptionCatcher unExpectExceptionCatcher){
            this.unExpectExceptionCatcher =unExpectExceptionCatcher;
            return this;
        }

        public Builder neverProcessWorkOnMainThread(boolean neverProcessWorkOnMainThread) {
            this.neverProcessWorkOnMainThread = neverProcessWorkOnMainThread;
            return this;
        }


        public SpWaitKiller build() {
            return new SpWaitKiller(this);
        }

    }


}
