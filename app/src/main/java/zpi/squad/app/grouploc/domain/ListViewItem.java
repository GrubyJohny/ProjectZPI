package zpi.squad.app.grouploc.domain;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    public final String uid;
    public final Drawable image;
    public final String name;
    public final String email;

    public ListViewItem(String uid, Drawable image, String name, String email) {
        this.uid = uid;
        this.image = image;
        this.name = name;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public Drawable getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}