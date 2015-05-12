package com.example.marcin.lokalizator;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    public final int uid;
    public final Drawable image;
    public final String name;
    public final String email;
    
    public ListViewItem(int uid, Drawable image, String name, String email) {
        this.uid = uid;
        this.image = image;
        this.name = name;
        this.email = email;
    }
}