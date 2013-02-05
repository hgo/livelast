package models;

public class Track {
    public String location;
    public String title;
    public String album;
    public String image;
    public long duration;
}


/*
 *<playlist version="1" xmlns="http://xspf.org/ns/0/">
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
 * */
 