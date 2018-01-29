package com.example.iftach.doodi;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by iftach on 16/11/17.
 */

public class Alarm {
    private String name;
    private long startTime;
    private int duration;

    Alarm(String input) {
        this.set(input);
    }

    private void set(String input) {
        String data[] = input.split(",");
        name = data[0];
        startTime = Long.parseLong(data[1]);
        duration = Integer.parseInt(data[2]);
    }

    Alarm(String name, long startTime, int duration) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    String getTimeString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/YY", new Locale("en"));
        return simpleDateFormat.format(calendar.getTime());
    }

    public String toString() {
        return name + "," + startTime + "," + duration;
    }
}
