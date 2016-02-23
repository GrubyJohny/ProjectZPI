package zpi.squad.app.grouploc.domain;

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
    private String message; //tutaj na razie będę przechowywać id do obiektów, których tyczy się powiadomienie
    private String createdAt;
    private int checked;

    public Notification() {
    }

    public Notification(String senderId, String senderName, String senderEmail, String receiverId, String type, String message, String groupId, String createdAt, int checked) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.checked = checked;

    }


    public String toString() {
        String response = null;

        if (type.equals("friendshipRequest")) {
            response = "Użytkownik " + senderName + " zaprosił Cię do znajomych";
        } else if (type.equals("friendshipAgreed")) {
            response = "Użytkownik " + senderName + " zaakceptował Twoje zaproszenie";
        } else if (type.equals("friendshipDisagreed")) {
            response = "Użytkownik " + senderName + " odrzucił Twoje zaproszenie";
        } else if (type.equals("friendshipCanceled")) {
            response = "Użytkownik " + senderName + " wyrzucił Cię ze znajomych";
        } else if (type.equals("shareMarker")) {
            response = "Użykownik " + senderName + " udostępnił Ci nowy punkt na mapie";
        } else if (type.equals("groupRequest")) {
            response = "Użykownik " + senderName + " poprosił Cię o dodanie do grupy";
        } else if (type.equals("userAddedToGroup")) {
            response = "Zostałeś dodany do grupy nr " + groupId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
}
