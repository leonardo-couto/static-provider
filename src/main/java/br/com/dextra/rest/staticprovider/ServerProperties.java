package br.com.dextra.rest.staticprovider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ServerProperties {

    private static final String NAME = "webapp.properties";
    private static final Pattern REDIRECT = Pattern.compile("^(source|target)\\..+\\.path$");
    private static final ServerProperties SINGLETON = new ServerProperties();


    private final Properties properties;

    private ServerProperties() {
        this.properties = new Properties();
        try {
            this.properties.load(this.getClass().getClassLoader().getResourceAsStream(NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String property(String name) {
        return SINGLETON.properties.getProperty(name);
    }

    public static List<Redirect> redirects() {
        
        Map<String, Redirect> redirects = new HashMap<>();
        
        for (Enumeration<?> e = SINGLETON.properties.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String value = property(name);
            
            if (REDIRECT.matcher(name).matches()) {
                addRedirect(redirects, name, value);
            }
        }
        
        return validRedirects(redirects.values());
    }
    
    private static List<Redirect> validRedirects(Collection<Redirect> redirects) {
    	List<Redirect> valids = new ArrayList<>();
    	
    	for (Redirect redirect : redirects) {
			if (redirect.isValid()) {
				valids.add(redirect);
			}
		}
    	
    	return valids; 
    }

    private static void addRedirect(Map<String, Redirect> redirects, String name, String value) {
        boolean source = name.startsWith("source");
        String key = name.substring(7, name.length() - 5);
        Redirect redirect = findRedirect(redirects, key);
        
        if (source) {
            redirect.setSource(value);
        } else {
            redirect.setTarget(value);
        }
    }

    private static Redirect findRedirect(Map<String, Redirect> redirects, String key) {
        Redirect redirect = redirects.get(key);
        if (redirect == null) {
            redirect = new Redirect();
            redirects.put(key, redirect);
        }
        return redirect;
    }
    
}
