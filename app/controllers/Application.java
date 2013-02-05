package controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.LastFmUser;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import play.Logger;
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
        redirect("http://www.last.fm/api/auth/?api_key=ef1b8ec486144479aea70cc1bb73a7d5&cb=http://localhost:9000/application/lcallback");
    }

    @Util
    private static LastFmUser getUser(){
        return Cache.get(session.getId(),LastFmUser.class);
    }
    public static void index() {
        final LastFmUser user = getUser();
        final String station = "lastfm://user/last.hq/library";
        final String method = "radio.tune";
        String url = "http://ws.audioscrobbler.com/2.0/";
        TreeMap<String, String> map = new TreeMap<String, String>(){{
           put("station", station);
           put("method",method);
           put("sk",user.getKey());
           put("api_key",api_key);
        }};
        
        Map<String, Object> params = new HashMap();
        params.put("api_sig", api_sig(map));
        params.put("station", station);
        params.put("method", method);
        params.put("sk", user.getKey());
        params.put("api_key", api_key);
        WSRequest wsRequest = WS.withEncoding("utf-8").url(url);
        wsRequest.parameters = params;
        HttpResponse httpResponse = wsRequest.post();
        Logger.info("radio.tune : %s", httpResponse.getString());
    }

}