package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;

class Persona {

    private String username;
    private boolean banned = false;

    Persona(String username) {
        this.username = username;
        try {
            banned = RequestHelper.isBanned(username);
        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }


    public String getUsername() {
        return username;
    }

    public boolean isBanned() {
        return banned;
    }
}
