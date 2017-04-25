package com.hayseed.ahp.youtubehelper.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author RuralHayseed
 *
 */
public class Runparms
{
    private Properties properties;
    private File       propertiesFile;

    public Runparms (String fileName) throws FileNotFoundException, IOException
    {
        loadPropertiesFile (fileName);
    }

    public void dump ()
    {
        System.out.println (properties.toString ());
    }

    public boolean getBooleanProperty (String property)
    {
        String s = properties.getProperty (property);

        if (s.equalsIgnoreCase ("yes")) return true;
        if (s.equalsIgnoreCase ("true")) return true;
        
        return false;
    }
    
    public char getCharProperty (String property)
    {
        String s = properties.getProperty (property);
        if (s == null) return Character.MIN_VALUE;

        s = s.trim ();
        return s.charAt (0);
    }

    public String getProperty (String property)
    {
        String s = properties.getProperty (property);
        if (s == null) throw new RuntimeException ("Property not found " + property);

        return s;
    }

    public String getProperty (String property, String defaultValue)
    {
        String s = properties.getProperty (property);
        if (s == null) return defaultValue;

        return s;
    }

    public void setProperty (String property, String value)
    {
        properties.setProperty (property, value);
    }

    private void loadPropertiesFile (String runParmsFileName) throws FileNotFoundException,
            IOException
    {
        properties = new Properties ();
        propertiesFile = new File (runParmsFileName);

        properties.load (new FileInputStream (propertiesFile));
    }
}
