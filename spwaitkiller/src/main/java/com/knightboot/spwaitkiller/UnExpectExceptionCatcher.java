package com.knightboot.spwaitkiller;

import android.util.Log;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public interface UnExpectExceptionCatcher {

    public static UnExpectExceptionCatcher Print =new UnExpectExceptionCatcher() {
        @Override
        public void onException(Throwable ex) {
            Log.e("SpWaitKillerException","catch Exception \n"
            +Log.getStackTraceString(ex));
        }
    };
    public void onException(Throwable ex);

}
