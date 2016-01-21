package zpi.squad.app.grouploc;

/**
 * Created by karol_000 on 2015-05-11.
 */
public class Friend {

    String uid;
    String name;
    String email;
    String photo;

    public Friend(){

    }

    public Friend(String uid, String name, String email, String photo){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
    }

    public String  getFriendID(){
        return uid;
    }
    public void setFriendID(String uid){
        this.uid = uid;
    }

    public String getFriendName(){
        return name;
    }
    public void setFriendName(String name){
        this.name = name;
    }

    public String getFriendEmail(){
        return email;
    }
    public void setFriendEmail(String email){
        this.email = email;
    }

    public String getFriendPhoto(){
        return photo;
    }
    public void setFriendPhoto(String email){
        this.photo = photo;
    }


}