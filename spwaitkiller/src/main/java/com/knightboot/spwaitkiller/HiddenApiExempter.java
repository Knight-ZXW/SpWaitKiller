package com.knightboot.spwaitkiller;

import android.content.Context;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public interface HiddenApiExempter {

    /**
     * 设置 允许调用HiddenApi
     * @return
     */
    public boolean exempt(Context context);

}
