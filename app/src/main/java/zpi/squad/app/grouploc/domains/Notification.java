package zpi.squad.app.grouploc.domains;

import android.graphics.Bitmap;

public class Notification {

    private String notificationId;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String receiverId;
    private int type;
    private String groupId;
    private String message; //tutaj na razie będę przechowywać id do obiektów, których tyczy się powiadomienie
    private String createdAt;
    private int checked;
    private Bitmap photo;

    private boolean markedAsRead;

    public Notification() {
    }

    /*public Notification(String senderId, String senderName, String senderEmail, String receiverId, String type, String message, String groupId, String createdAt, int checked) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.checked = checked;
    }*/

    public Notification(String notificationId, String senderName, String senderEmail, int type, String message, String createdAt, boolean markedAsRead) {
        this.notificationId = notificationId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.markedAsRead = markedAsRead;
    }


    public String toString() {
        String response = "";

        switch (type) {
            case 101: {
                response = senderName + " has invited you to friends";
                break;
            }
            case 102: {
                response = senderName + " has accepted your friends request";
                break;
            }
            default: {
                response = "This should never happen";
                break;
            }
        }


        return response;
    }


    public boolean isChecked() {
        if (checked == 0)
            return false;
        else
            return true;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isMarkedAsRead() {
        return markedAsRead;
    }

    public void setMarkedAsRead(boolean markedAsRead) {
        this.markedAsRead = markedAsRead;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap b) {
        this.photo = b;
    }
}
