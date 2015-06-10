package zpi.squad.app.grouploc;

/**
 * Created by karol_000 on 2015-05-12.
 */
public class Notification {

    private String senderId;
    private String senderName;
    private String senderEmail;
    private String receiverId;
    private String type;
    private String groupId;
    private String messageId;
    private String createdAt;
    private int checked;

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
        else if(type.equals("friendshipAgreed")){
            response = "Użytkownik " + senderName + " zaakceptował Twoje zaproszenie";
        }
        else if(type.equals("friendshipDisagreed")){
            response = "Użytkownik " + senderName + " odrzucił Twoje zaproszenie";
        }
        else if(type.equals("friendshipCanceled")){
            response = "Użytkownik " + senderName + " wyrzucił Cię ze znajomych";
        }
        else if(type.equals("shareMarker")){
            response = "Użykownik " + senderName + " udostępnił Ci nowy punkt na mapie";
        }


        return response;
    }


    public String getSenderName(){
        return senderName;
    }

    public String getSenderId(){
        return senderId;
    }

    public String getSenderEmail(){
        return senderEmail;
    }

    public String getReceiverId(){
        return receiverId;
    }

    public String getType() {
        return type;
    }

    public boolean isChecked() {
        if(checked == 0)
            return false;
        else
            return true;
    }

    public String getCreatedAt() {
        return createdAt;
    }


}
