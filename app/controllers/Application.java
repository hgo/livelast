package controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.LastFmUser;
import models.Playlist;
import models.Track;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;

public class Application extends Controller {

    final static String api_key = "ef1b8ec486144479aea70cc1bb73a7d5";
    final static String api_secret = "96c36d6fb5bd26c2b7ed4a3e2b4b04bf";
    static String _token;
    
    @Before(unless={"login","lcallback"})
    static void before(){
        Logger.info("request.action: %s",request.action);
        Logger.info("request.actionMethod: %s",request.actionMethod);
        if(Cache.get(session.getId()) == null){
            Logger.info("redirectiong to login() ...");
            login();
        }
        request.args.put("user",Cache.get(session.getId()));
    }
    
    @Util 
    private static String api_sig(TreeMap<String, String> orderedMap){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry: orderedMap.entrySet()) {
            Logger.info("enty - val -> %s", entry.toString());
            builder.append(entry.getKey()).append(entry.getValue());
        }
        String format = builder.append(api_secret).toString();
        Logger.info("before hash = %s", format);
        String api_sig = Codec.hexMD5(format);
        Logger.info("api_sig = "+api_sig);
        return api_sig;
    }
    
    public static void lcallback() throws NoSuchAlgorithmException, IOException {
        final String token = params.get("token");
        _token = token; 
        final String method = "auth.getSession";
        TreeMap<String, String> map = new TreeMap<String , String>(){{
          put("api_key", api_key);
          put("method", method);
          put("token", token);
        }};
        WSRequest wsRequest = WS.withEncoding("UTF-8").url(
                String.format("http://ws.audioscrobbler.com/2.0/?method=%s&api_key=%s&api_sig=%s&token=%s", method,api_key, api_sig(map), token,api_secret));
        Logger.info("url: "+wsRequest.url);
        HttpResponse httpResponse = wsRequest.get();
        Logger.info(httpResponse.getString());
        Document xml = httpResponse.getXml();
        if("failed".equalsIgnoreCase(xml.getElementsByTagName("lfm").item(0).getAttributes().getNamedItem("status").getNodeValue())){
            flash.error("login failed");
        }else{
            Node sessionNode = xml.getElementsByTagName("session").item(0);
            String name =((Element) sessionNode).getElementsByTagName("name").item(0).getTextContent();
            String key=((Element) sessionNode).getElementsByTagName("key").item(0).getTextContent();
            int subs =Integer.parseInt(((Element) sessionNode).getElementsByTagName("subscriber").item(0).getTextContent());
            LastFmUser user = LastFmUser.create(name,key,subs);
            Cache.add(session.getId(), user);
        }
        index();
    }
    
    public static void login() {
        redirect("http://www.last.fm/api/auth/?api_key=ef1b8ec486144479aea70cc1bb73a7d5&cb="+getCallBack("/application/lcallback"));
    }
    
    @Util
    private static String getCallBack(String resource){
        return (Play.id == null || Play.id.isEmpty()) ? "http://localhost:9000"+resource : "http://livelast.herokuapp.com"+resource;
    }

    @Util
    private static LastFmUser getUser(){
        return (LastFmUser) request.args.get("user");
    }
    public static void index() {
        final LastFmUser user = getUser();
        final String station = "lastfm://user/" + user.getName() + "/library";
        final String method = "radio.tune";
        final String rtp = "1";
        String url = "http://ws.audioscrobbler.com/2.0/";
        TreeMap<String, String> map = new TreeMap<String, String>(){{
           put("station", station);
           put("method",method);
           put("sk",user.getKey());
           put("api_key",api_key);
//           put("rpt",rtp);
        }};
        
        Map<String, Object> params = new HashMap();
        params.put("api_sig", api_sig(map));
        params.put("station", station);
        params.put("method", method);
        params.put("sk", user.getKey());
        params.put("api_key", api_key);
//        params.put("rtp", rtp);
        WSRequest wsRequest = WS.withEncoding("utf-8").url(url);
        wsRequest.parameters = params;
        HttpResponse httpResponse = wsRequest.post();
        Logger.info("radio.tune : %s", httpResponse.getString());
        //TODO:error
//        api_key (Required) : A Last.fm API key.
//        api_sig (Required) : A Last.fm method signature. See authentication for more information.
//        sk (Required) : A session key generated by authenticating a user via the authentication protocol.
        Playlist playlist = getPlayList(user.getKey());
        user.setPlaylist(playlist);
        render(user);
    }
    @Util
    private static Playlist getPlayList(final String sk){
        String url = "http://ws.audioscrobbler.com/2.0/";
        TreeMap<String, String> map = new TreeMap<String, String>(){{
            put("method","radio.getPlaylist");
            put("sk",sk);
            put("api_key",api_key);
         }};
        Map<String, Object> params = new HashMap();
        params.put("method", "radio.getPlaylist");
        params.put("sk", sk);
        params.put("api_key", api_key);
        params.put("api_sig", api_sig(map));
        WSRequest wsRequest = WS.withEncoding("utf-8").url(url);
        wsRequest.parameters = params;
        HttpResponse httpResponse = wsRequest.post();
        Logger.info("radio.getPlayList:" + httpResponse.getString());
        //
        Playlist playlist = new Playlist();
        
        Document xml = httpResponse.getXml();
        Element _playlist = ((Element)xml.getElementsByTagName("playlist").item(0));
        playlist.title = (_playlist.getElementsByTagName("title").item(0)).getTextContent();
        playlist.creator = (_playlist.getElementsByTagName("creator").item(0)).getTextContent();
        
        Element _trackList = ((Element)xml.getElementsByTagName("trackList").item(0));
        NodeList tracks = _trackList.getElementsByTagName("track");
        for (int i = 0; i < tracks.getLength(); i++) {
            Track track = new Track();
            track.location = ((Element)tracks.item(i)).getElementsByTagName("location").item(0).getTextContent();
            track.title =((Element)tracks.item(i)).getElementsByTagName("title").item(0).getTextContent();
            track.album = ((Element)tracks.item(i)).getElementsByTagName("album").item(0).getTextContent();
            track.image = ((Element)tracks.item(i)).getElementsByTagName("image").item(0).getTextContent();
            track.duration = Long.valueOf(((Element)tracks.item(i)).getElementsByTagName("duration").item(0).getTextContent());
            track.creator = ((Element)tracks.item(i)).getElementsByTagName("creator").item(0).getTextContent();
            playlist.tracks.add(track);
        }
        
        //
        return playlist;
    }

    public static void nextTrack(){
        if( ! request.isAjax()){
            notFound();
        }
        LastFmUser user = getUser();
        Track track = null;
        try{
            if(user.getPlaylist() == null){
                user.setPlaylist(getPlayList(user.getKey()));
            }
            track = user.getPlaylist().next();
        }catch(IndexOutOfBoundsException e){
            user.setPlaylist(getPlayList(user.getKey()));
            track = user.getPlaylist().next();
        }finally{
            nowPlaying();
        }
        renderJSON(track);
    }
    @Util
    private static void nowPlaying(){
        LastFmUser user = getUser();
        Track track = user.getPlaylist().getCurrentTrack();
//        artist (Required) : The artist name.
//        track (Required) : The track name.
//        api_key (Required) : A Last.fm API key.
//        api_sig (Required) : A Last.fm method signature. See authentication for more information.
//        sk (Required) : A session key generated by authenticating a user via the authentication protocol.
        final String method = "track.updateNowPlaying";
        final String artist = track.creator;
        final String album = track.album;
        final String sk = user.getKey();
        final String _track = track.title;
        TreeMap<String, String> map = new TreeMap<String, String>(){{
            put("method",method);
            put("sk",sk);
            put("artist",artist);
            put("album",album);
            put("title",_track);
            put("api_key",api_key);
         }};
         
         String url = "http://ws.audioscrobbler.com/2.0/";
         
         Map<String, Object> params = new HashMap();
         params.put("method", method);
         params.put("sk", sk);
         params.put("artist",artist);
         params.put("album",album);
         params.put("api_key", api_key);
         params.put("track", _track);
         params.put("api_sig", api_sig(map));
         
         
         WSRequest wsRequest = WS.withEncoding("utf-8").url(url);
         wsRequest.parameters = params;
         HttpResponse httpResponse = wsRequest.post();
         Logger.info(method + " :" + httpResponse.getString());
        
    }
}