package com.knightboot.spwaitkiller;


import android.content.Context;
import android.os.Build;

import static android.os.Build.VERSION.SDK_INT;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

/**
 * created by Knight-ZXW on 2021/9/14
 */
class DefaultHiddenApiExempter  implements HiddenApiExempter{

    @Override
    public boolean exempt(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.P) {
            return HiddenApiBypass.addHiddenApiExemptions("Landroid/app/QueuedWork;");
        }
        return true;
    }


}
