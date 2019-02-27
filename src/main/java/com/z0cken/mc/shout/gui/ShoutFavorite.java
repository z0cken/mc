package com.z0cken.mc.shout.gui;

public class ShoutFavorite {
    private int groupID;
    private int shoutID;

    public ShoutFavorite(int groupID, int shoutID){
        this.groupID = groupID;
        this.shoutID = shoutID;
    }

    public int getGroupID(){
        return this.groupID;
    }

    public int getShoutID() {
        return shoutID;
    }
}
