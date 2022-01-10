package com.knightboot.spwaitkiller;

import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public class ProxyFinishersLinkedList<T> extends ConcurrentLinkedQueue<T> {

    private final ConcurrentLinkedQueue<T> sPendingWorkFinishers;
    public ProxyFinishersLinkedList(ConcurrentLinkedQueue<T> sPendingWorkFinishers){
        this.sPendingWorkFinishers = sPendingWorkFinishers;
    }

    /**
     * always return null
     * @return
     */
    @Nullable
    @Override
    public T poll() {
        return null;
    }

    @Override
    public boolean add(T t) {
        return sPendingWorkFinishers.add(t);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return sPendingWorkFinishers.remove(o);
    }
}
