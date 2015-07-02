package zpi.squad.app.grouploc;

public class GroupList {

    private int groupID;
    String groupName;
    private int adminID;
    String adminName;
    String date;

    public GroupList(){
    }

    public GroupList(int groupID, String groupName, int adminID, String adminName, String date){
        this.groupID = groupID;
        this.groupName = groupName;
        this.adminID = adminID;
        this.adminName = adminName;
        this.date = date;
    }

    public int getGroupID(){
        return groupID;
    }
    public void setGroupID(int groupID){
        this.groupID = groupID;
    }

    public String getGroupName(){
        return groupName;
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }


}
