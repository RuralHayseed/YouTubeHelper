package com.hayseed.ahp.youtubehelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.hayseed.ahp.youtubehelper.defs.RunparmDefs;
import com.hayseed.ahp.youtubehelper.utils.Runparms;

/**
 * 
 * @author RuralHayseed
 *
 */
public class YouTubeHelper
{
    private final static String GETLISTCSV = "GETLISTCSV";
    private final static String RECENT     = "MOSTRECENT";

    public static void main (String[] args)
    {
        if (args.length != 1) throw new RuntimeException ("Missing runparms");

        Runparms parms = null;
        Runparms apiParms = null;
        String function = null;
        try
        {
            parms = new Runparms (args[0]);

            function = parms.getProperty (RunparmDefs.FUNCTION).toUpperCase ();
            apiParms = new Runparms (parms.getProperty (RunparmDefs.API_KEY));
        }
        catch (IOException e)
        {
            e.printStackTrace ();
            System.exit (1);
        }

        GetVideoList getlist = new GetVideoList (parms);
        getlist.setApiKey (apiParms.getProperty (RunparmDefs.API_KEY));
        getlist.setChannelID (apiParms.getProperty (RunparmDefs.CHANNEL_ID));
        switch (function)
        {
            case GETLISTCSV:
                getlist.getUploads (parms.getProperty (RunparmDefs.OUPUT_FILE_NAME));
                break;

            case RECENT:
                String sinceDate = getMostRecentTimestamp (parms.getProperty (RunparmDefs.TIME_STAMP_FILE_NAME));
                if (sinceDate == null) throw new RuntimeException ("No History");
                
                String fqp = parms.getProperty (RunparmDefs.OUTPUT_DIR) + "/" + sinceDate + ".csv";
                getlist.searchUploadListByDate (fqp, null);
                break;

            default:
                throw new RuntimeException ("Unknown function " + function);
        }

        updateTimeStamp (parms);
    }

    private static String getMostRecentTimestamp (String fileName)
    {
        File f = new File (fileName);

        BufferedReader in;
        String line = null;
        
        try
        {
            in = new BufferedReader (new FileReader (f));
            line = in.readLine ();
            in.close ();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return line;                
    }
    
    private static void updateTimeStamp (Runparms parms)
    {
        try
        {
            String time = parms.getProperty (RunparmDefs.MOST_RECENT_DATETIME, null);
            if (time == null) return;
            
            File f = new File (parms.getProperty (RunparmDefs.TIME_STAMP_FILE_NAME));
            f.createNewFile ();
            
            BufferedReader in = new BufferedReader (new FileReader (f));
            StringBuffer sb = new StringBuffer ();
            String line = in.readLine ();

            if (line != null)
            {
                if (line.equals (time))
                {
                    System.out.println ("No time update");
                    in.close ();
                    return;
                }
            }

            while (line != null)
            {
                sb.append (line+"\n");
                line = in.readLine ();
            }

            in.close ();

            PrintWriter out = new PrintWriter (f);
            out.println (time);
            out.print (sb.toString ());

            out.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }

    }
}
