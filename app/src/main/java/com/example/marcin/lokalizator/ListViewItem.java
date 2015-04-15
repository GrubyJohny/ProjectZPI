package com.example.marcin.lokalizator;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    public final Drawable image;
    public final String name;
    
    public ListViewItem(Drawable image, String name) {
        this.image = image;
        this.name = name;
    }
}