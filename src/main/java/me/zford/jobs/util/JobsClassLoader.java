package me.zford.jobs.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.plugin.java.JavaPlugin;

public class JobsClassLoader extends URLClassLoader {
    
    public JobsClassLoader(JavaPlugin plugin) {
        super(new URL[0], plugin.getClass().getClassLoader());
    }

    public void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }
    
    @Override
    public void addURL(URL url) {
        for (URL u : getURLs())
            if (url.sameFile(u)) return;
        
        super.addURL(url);
    }
}
