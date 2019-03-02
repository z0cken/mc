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

    public int firstFree(){
        for(int i = 0; i < favorites.length; i++){
            if(favorites[i] == null){
                return i;
            }
        }
        return -1;
    }

    public ShoutFavorite[] getFavorites(){
        return this.favorites;
    }

    public boolean hasFavorite(int groupID, int shoutID){
        for(ShoutFavorite favorite : favorites){
            if(favorite != null && favorite.getGroupID() == groupID && favorite.getShoutID() == shoutID){
                return true;
            }
        }
        return false;
    }

    public void removeFavorite(int groupID, int shoutID){
        for(int i = 0; i < favorites.length; i++){
            ShoutFavorite favorite = favorites[i];
            if(favorite != null && favorite.getGroupID() == groupID && favorite.getShoutID() == shoutID){
                favorites[i] = null;
            }
        }
    }
}
