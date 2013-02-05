package models;

import java.io.Serializable;


public class LastFmUser implements Serializable{

    String name;
    String key;
    boolean subscriber;
    Playlist playlist;
    
    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public boolean isSubscriber() {
        return subscriber;
    }

    private LastFmUser(String name, String key, boolean subscriber) {
        super();
        this.name = name;
        this.key = key;
        this.subscriber = subscriber;
    }

    public static LastFmUser create(String name2, String key2, int subs) {
        return new LastFmUser(name2, key2, subs == 1);
    }

}
