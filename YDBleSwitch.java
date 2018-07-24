package com.example.mysmall.newelasticballview.elastic;

/**
 * Created by 49479 on 2018/7/24.
 */

public interface YDBleSwitch<T> {
    void connecting();
    void connected();
    void disconnected();
    void openSuccess();
    void setOpenMotionListener(T listener);
}
