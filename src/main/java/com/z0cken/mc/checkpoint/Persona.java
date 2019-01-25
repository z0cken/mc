package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;

class Persona {

    private final String username;
    private long bannedUntil = 0;

    Persona(String username) {
        this.username = username;
        try {
            bannedUntil = RequestHelper.getBannedUntil(username);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public long getBannedUntil() {
        return bannedUntil;
    }
}
