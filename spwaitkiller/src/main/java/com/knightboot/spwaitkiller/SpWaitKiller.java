package com.knightboot.spwaitkiller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public class SpWaitKiller {

    private HiddenApiExempter hiddenApiExempter;

    private boolean working;

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
            if (working) {
                return;
            }
            realWork();
            working = true;
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
            } else{
                Field sFinishersField = QueuedWorkClass.getDeclaredField("sFinishers");
                sFinishersField.setAccessible(true);
                LinkedList sFinishers = (LinkedList) sFinishersField.get(null);
                ProxyFinishersList proxyedSFinishers = new ProxyFinishersList(sFinishers);
                sFinishersField.set(null, proxyedSFinishers);
            }
        }

        if (neverProcessWorkOnMainThread) {
            // 通过调用 getHandler函数
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

            if (targetSdkVersion>=Build.VERSION_CODES.R){
                this.hiddenApiExempter.exempt(mContext);
            }
            QueueWorksWorkFieldHooker queueWorksWorkFieldHooker = new QueueWorksWorkFieldHooker();
            queueWorksWorkFieldHooker.proxyWork();
        }
    }

    private static class QueueWorksWorkFieldHooker implements ProxySWork.AboveAndroid12Processor {

        private boolean reflectionFailed =false;
        /**
         * sLock对象， 操作
         */
        private Object sLock = null;
        private  Field sWorkField;
        private Looper looper;

        @SuppressLint("SoonBlockedPrivateApi")
        public QueueWorksWorkFieldHooker(){
            try {
                Class QueuedWorkClass = Class.forName("android.app.QueuedWork");
                Method method = QueuedWorkClass.getDeclaredMethod("getHandler");
                method.setAccessible(true);

                Handler handler = (android.os.Handler) method.invoke(null);
                looper = handler.getLooper();

                sWorkField = QueuedWorkClass.getDeclaredField("sWork");
                sWorkField.setAccessible(true);
                Field sLockField = QueuedWorkClass.getDeclaredField("sLock");
                sLockField.setAccessible(true);
                sLock = sLockField.get(null);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
                reflectionFailed = true;
            }

        }

        private void proxyWork(){
            if (reflectionFailed){
                return;
            }
            synchronized (sLock){
                //Android12以下，sWork自始至终是同一个对象
                try {
                    LinkedList sWork = (LinkedList) sWorkField.get(null);
                    ProxySWork sWorkProxy = new ProxySWork(sWork, looper, this);
                    sWorkField.set(null, sWorkProxy);
                } catch (IllegalAccessException e) {
                    reflectionFailed =true;
                }
            }
        }


        @Override
        public void reProxySWork() {
            //Android12开始,sWork字段在每次执行ProcessPendingWork时，sWork字段都会重新指向一个新的集合对象
            //因此需要重新代理
            proxyWork();
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
