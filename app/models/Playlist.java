package models;

import java.io.Serializable;
import java.util.ArrayList;

import play.Logger;

public class Playlist implements Serializable{
    public String title;
    public String creator;
    public ArrayList<Track> tracks = new ArrayList<Track>();
    private int idx = -1;
    
    public Track next() throws IndexOutOfBoundsException{
        if(tracks.size() < idx + 1){
            throw new IndexOutOfBoundsException();
        }else{
            idx ++; 
            return tracks.get(idx);
        }
    }

    public Track getCurrentTrack() {
        return tracks.get(idx);
    }

    public Track prevTrack() {
        Track track = null;
        try {
            track = tracks.get(idx - 1);
        } catch (Exception e) {
            Logger.error("prevTrack error. idx : %i tracks.size() : %i", idx, tracks.size());
        }
        return track;
    }

    public void clear() {
        this.tracks.clear();
        this.idx = -1;
    }
}
/*
 * <playlist version="1" xmlns="http://xspf.org/ns/0/">
 <title>+Cher+Similar+Artists</title>
 <creator>Last.fm</creator>
 <date>2007-11-26T17:34:38</date>
 <link rel="http://www.last.fm/expiry">3600</link>
 <trackList>
  <track>
   <location>http://play.last.fm/ ... .mp3</location>
   <title>Two People (Live)</title>
   <identifier>8212510</identifier>
   <album>Tina Live In Europe</album>
   <creator>Tina Turner</creator>
   <duration>265000</duration>
   <image>http:// ... .jpg</image>
   <extension application="http://www.last.fm">
     <artistpage></artistpage>
     <albumpage>...</albumpage>
     <trackpage>...</trackpage>
     <buyTrackURL>...</buyTrackURL>
     <buyAlbumURL>...</buyAlbumURL>
     <freeTrackURL>...</freeTrackURL>
   </extension>
  </track>
   ...
 </trackList>
</playlist>
 * 
 * */
