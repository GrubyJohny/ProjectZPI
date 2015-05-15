package com.example.marcin.lokalizator;

/**
 * Created by karol_000 on 2015-05-12.
 */
public class Notification {

    private String senderId;
    private String senderName;
    private String senderEmail;
    private String receiverId;

    public String getType() {
        return type;
    }

    private String type;
    private String groupId;
    private String messageId;

    public String getCreatedAt() {
        return createdAt;
    }

    private String createdAt;

    public boolean isChecked() {
        if(checked == 0)
            return false;
        else
            return true;
    }

    private int checked;
    private SQLiteHandler db;


    public Notification(){

    }

    public Notification(String senderId, String senderName, String senderEmail, String receiverId, String type, String messageId, String groupId, String createdAt, int checked){
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverId = receiverId;
        this.type = type;
        this.messageId = messageId;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.checked = checked;

    }


    public String toString(){
        String response = null;

        if(type.equals("friendshipRequest")){
            response = "Użytkownik " + senderName + " zaprosił Cię do znajomych";
        }
        if(type.equals("friendshipAgree")){
            response = "Użytkownik " + senderName + " zaakceptował Twoje zaproszenie";
        }

        return response;
    }



}
