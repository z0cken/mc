package com.pr0gramm.mc.verify;

import com.mashape.unirest.http.exceptions.UnirestException;

public class Persona {

    private String username;
    private boolean banned;

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
