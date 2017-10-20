package com.example.loadmoredemo.model;

/**
 * Created by dumingwei on 2017/10/20.
 */
public class CountDownModel {

    //活动开始时间
    private String startTime;
    //活动结束时间
    private String endTime;
    //系统时间
    private String nowTime;

    //活动结束时间和当先系统时间的时间差 用ms表示
    private long countTime;
    //要显示的时间 如2天3时57分20秒。
    private String time;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNowTime() {
        return nowTime;
    }

    public void setNowTime(String nowTime) {
        this.nowTime = nowTime;
    }

    public long getCountTime() {
        return countTime;
    }

    public void setCountTime(long countTime) {
        this.countTime = countTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
