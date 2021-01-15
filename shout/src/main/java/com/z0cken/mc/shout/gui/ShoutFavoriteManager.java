package com.z0cken.mc.shout.gui;

import java.util.HashMap;

public class ShoutFavoriteManager {
    private HashMap<String, ShoutFavorites> favorites = new HashMap<>();

    public ShoutFavoriteManager(){}

    public void addFavorites(String uuid, ShoutFavorites pFavorites){
        favorites.put(uuid, pFavorites);
    }

    public ShoutFavorites getFavorites(String uuid){
        return favorites.get(uuid);
    }
}
