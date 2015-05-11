package com.example.marcin.lokalizator;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    public final int uid;
    public final Drawable image;
    public final String name;
    
    public ListViewItem(int uid, Drawable image, String name) {
        this.uid = uid;
        this.image = image;
        this.name = name;
    }
}