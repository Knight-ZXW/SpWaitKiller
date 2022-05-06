package com.knightboot.spwaitkiller;

import androidx.annotation.Nullable;

import java.util.LinkedList;

/**
 * created by Knight-ZXW on 2021/9/14
 */
public class ProxyFinishersList<T> extends LinkedList<T> {

    private final LinkedList<T> sFinishers;
    public ProxyFinishersList(LinkedList<T> sFinishers){
        this.sFinishers = sFinishers;
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
        return sFinishers.add(t);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return sFinishers.remove(o);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
