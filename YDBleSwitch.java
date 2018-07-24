package com.example.mysmall.newelasticballview.elastic;

/**
 * Created by 49479 on 2018/7/24.
 */

public interface YDBleSwitch<T> {
    boolean connecting();
    boolean connected();
    boolean disconnected();
    boolean openSuccess();
    void showMsg(String content);
    void setOpenMotionListener(T listener);
}
