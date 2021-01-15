package com.z0cken.mc.shout.gui;

import com.z0cken.mc.shout.Shout;

public class ShoutFavorite {
    private int groupID;
    private int shoutID;

    public ShoutFavorite(int groupID, int shoutID){
        this.groupID = groupID;
        this.shoutID = shoutID;
    }

    public ShoutFavorite(Shout shout){
        this.groupID = shout.getGroupID();
        this.shoutID = shout.getId();
    }

    public int getGroupID(){
        return this.groupID;
    }

    public int getShoutID() {
        return shoutID;
    }
}
