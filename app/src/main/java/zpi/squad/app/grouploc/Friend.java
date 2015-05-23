package zpi.squad.app.grouploc;

/**
 * Created by karol_000 on 2015-05-11.
 */
public class Friend {

    int uid;
    String name;
    String email;

    public Friend(){

    }

    public Friend(int uid, String name, String email){
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    public int getFriendID(){
        return uid;
    }
    public void setFriendID(int uid){
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


}
