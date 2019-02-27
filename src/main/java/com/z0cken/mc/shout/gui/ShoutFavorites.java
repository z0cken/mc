package com.z0cken.mc.shout.gui;

public class ShoutFavorites {
    private ShoutFavorite[] favorites;

    public ShoutFavorites(){
        favorites = new ShoutFavorite[9];
    }

    public void setFavorite(int i, ShoutFavorite favorite){
        if(i >= 0 && i < favorites.length){
            favorites[i] = favorite;
        }
    }

    public ShoutFavorite getFavorite(int i){
        if(i >= 0 && i < favorites.length){
            return favorites[i];
        }
        return null;
    }

    public ShoutFavorite[] getFavorites(){
        return this.favorites;
    }
}
