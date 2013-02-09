package controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import play.Logger;
import play.Play;
import play.mvc.Controller;

public class Test extends Controller {
    
    public static void test(int idx){
        File[] files = Play.getFile("public/test").listFiles();
        File f = files[idx > files.length ? idx % files.length : idx];
        Logger.info("f.getAbsolutePath()",f.getAbsolutePath());
        String name = f.getName();
        render(name);
    }
    
    public static void rendering(String name) throws FileNotFoundException{
        File file = Play.getFile("public/test/"+name);
        response.contentType = "audio/mpeg";
        response.setHeader("Content-Disposition", "attachment; filename="
            + file.getName());
        renderBinary(file);
    }
}
