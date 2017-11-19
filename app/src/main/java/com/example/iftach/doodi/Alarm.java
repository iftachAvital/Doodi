package com.example.iftach.doodi;

import android.content.Intent;

/**
 * Created by iftach on 16/11/17.
 */

public class Alarm {
    int hour;
    int minute;
    int duration;
    String name;

    public Alarm (String input) {
        this.set(input);
    }

    public void set (String input) {
        String data[] = input.split(",");
        name = data[0];
        hour = Integer.parseInt(data[1]);
        minute = Integer.parseInt(data[2]);
        duration = Integer.parseInt(data[3]);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }
}
