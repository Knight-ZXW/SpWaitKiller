package com.knightboot.spwaitkiller;


import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import dalvik.system.DexFile;

import static android.os.Build.VERSION.SDK_INT;

import java.lang.reflect.Method;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;

/**
 * created by Knight-ZXW on 2021/9/14
 */
class DefaultHiddenApiExempter  implements HiddenApiExempter{

    @Override
    public boolean exempt(Context context) {
        return Reflection.unseal(context) ==0;
    }


}
