package com.hayseed.ahp.youtubehelper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelContentDetails.RelatedPlaylists;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import com.hayseed.ahp.youtubehelper.defs.RunparmDefs;
import com.hayseed.ahp.youtubehelper.utils.HandyTools;
import com.hayseed.ahp.youtubehelper.utils.Runparms;

/**
 * Retrieve a YouTube video list from the MyUploads playlist associated with a
 * given channel. Two YouTube v3 functions are used: (1)
 * YouTube.PlaylistItems.List, (2) YouTube.Search.List.
 * 
 * The first retrieves all of the videos in the uploads playlist, whereas the
 * second retrieves only those additions to uploads since a given date.
 * 
 * @author RuralHayseed
 *
 */
public class GetVideoList
{
    private YouTube  uTube;
    private Runparms parms;
    private String   apiKey;
    private String   channelID;
    private String   uploadID;

    /**
     * 
     * @param parms
     *            Runparms for runtime options
     */
    public GetVideoList (Runparms parms)
    {
        uTube = new YouTube.Builder (new NetHttpTransport (), new JacksonFactory (),
                new HttpRequestInitializer ()
                {
                    public void initialize (HttpRequest request) throws IOException
                    {
                    }
                }).setApplicationName ("YouTubeHelper").build ();

        this.parms = parms;
    }

    /**
     * 
     * @param uploads
     *            Playlist to dump uploads playlist to std out
     */
    public void dumpUploads (List<PlaylistItem> uploads)
    {
        if (uploads == null) System.out.println ("Empty playlist");

        int count = 0;
        for (Iterator<PlaylistItem> it = uploads.iterator (); it.hasNext ();)
        {
            PlaylistItem item = it.next ();
            count++;
            System.out.println (count + " " + item.getSnippet ().getTitle ());
        }
    }

    /**
     * Implementation of v3 uploads retrieval, in two steps: (1) get the uploads
     * playlist id; (2) retrieve the videos in the playlist using the playlist
     * id
     * 
     * @return list of PlaylistItems
     */
    public List<PlaylistItem> getUploads ()
    {
        if (uploadID == null) getUploadPlayListID ();

        try
        {
            YouTube.PlaylistItems.List playlistRequest = uTube.playlistItems ().list (
                    parms.getProperty (RunparmDefs.YOUTUBE_PLAYLIST_ITEMS_LIST_PART));
            playlistRequest.setKey (apiKey);
            playlistRequest.setPlaylistId (uploadID);
            playlistRequest.setMaxResults ((long) 50);
            playlistRequest.setFields (parms
                    .getProperty (RunparmDefs.YOUTUBE_PLAYLIST_ITEMS_LIST_SETFIELDS));

            ArrayList<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem> ();
            String nextToken = "";

            do
            {
                playlistRequest.setPageToken (nextToken);
                PlaylistItemListResponse playlistItemResult = playlistRequest.execute ();
                playlistItemList.addAll (playlistItemResult.getItems ());

                nextToken = playlistItemResult.getNextPageToken ();
            }
            while (nextToken != null);

            System.out.println ("Size of playlistItemList = " + playlistItemList.size ());

            if (playlistItemList.size () > 0)
            {
                parms.setProperty (RunparmDefs.MOST_RECENT_DATETIME, playlistItemList.get (0)
                        .getSnippet ().getPublishedAt ().toString ());
            }

            return playlistItemList;

        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }

        return null;
    }

    /**
     * Implementation of the v3 uploads retrieval, with results written to a csv
     * file
     * 
     * @param fqpFileName
     *            Name of output file
     */
    public void getUploads (String fqpFileName)
    {
        List<PlaylistItem> uploads = getUploads ();

        try
        {
            if (uploads == null) throw new RuntimeException ("Empty playlist");

            String delimiter = parms.getProperty (RunparmDefs.DELIMITER);

            PrintWriter writer = new PrintWriter (new FileWriter (fqpFileName));

            writer.println (parms.getProperty (RunparmDefs.CSV_COLUMN_NAMES));
            for (Iterator<PlaylistItem> it = uploads.iterator (); it.hasNext ();)
            {
                PlaylistItem item = it.next ();
                writer.print (item.getContentDetails ().getVideoId () + delimiter);
                writer.print (item.getSnippet ().getTitle ().replaceAll (",", " &") + delimiter);

                DateTime dt = item.getSnippet ().getPublishedAt ();
                writer.print (HandyTools.getLocalTime (dt.getValue ()) + delimiter);

                writer.println ("Add");
            }

            writer.flush ();
            writer.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    /**
     * Implementation of v3 search feature to retrieve uploads on a given
     * channel since a specified date
     * 
     * @param date
     *            Specified date
     * @return List of SearchResults
     */
    public List<SearchResult> searchUploadListByDate (String date)
    {
        try
        {
            YouTube.Search.List searchRequest = uTube.search ().list (
                    parms.getProperty (RunparmDefs.YOUTUBE_SEARCH_LIST_PART));

            searchRequest.setKey (apiKey);
            if (channelID != null) searchRequest.setChannelId (channelID);

            searchRequest.setMaxResults ((long) 50);
            searchRequest.setOrder ("date");
            searchRequest.setType ("video");
            searchRequest.setFields (parms.getProperty (RunparmDefs.YOUTUBE_SEARCH_LIST_SETFIELDS));

            if (date != null)
            {
                DateTime dt = new DateTime (date);
                searchRequest.setPublishedAfter (dt);
            }

            SearchListResponse response = searchRequest.execute ();
            List<SearchResult> items = response.getItems ();

            boolean b = parms.getBooleanProperty (RunparmDefs.SEARCH_DELETE_LAST_ITEM);
            if (b && items != null)
            {
                items.remove (items.size () - 1);
            }

            if (items.size () > 0)
            {
                parms.setProperty (RunparmDefs.MOST_RECENT_DATETIME, items.get (0).getSnippet ()
                        .getPublishedAt ().toString ());
            }

            System.out.println (items.size ());

            return items;
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }

        return null;
    }

    /**
     * Implementation of v3 search feature to retrieve uploads on a given
     * channel since a specified date, with results written to csv file
     * 
     * @param fqpFileName
     *            Output file name
     * @param date
     *            Specified date
     */
    public void searchUploadListByDate (String fqpFileName, String date)
    {
        List<SearchResult> results = searchUploadListByDate (date);

        try
        {
            if (results == null) throw new RuntimeException ("Empty search");

            if (results.isEmpty ()) return;

            String delimiter = parms.getProperty (RunparmDefs.DELIMITER);

            PrintWriter writer = new PrintWriter (new FileWriter (fqpFileName));

            writer.println (parms.getProperty (RunparmDefs.CSV_COLUMN_NAMES));
            for (Iterator<SearchResult> it = results.iterator (); it.hasNext ();)
            {
                SearchResult item = it.next ();
                writer.print (item.getId ().getVideoId () + delimiter);
                writer.print (item.getSnippet ().getTitle ().replaceAll (",", " &") + delimiter);

                DateTime dt = item.getSnippet ().getPublishedAt ();
                writer.print (HandyTools.getLocalTime (dt.getValue ()) + delimiter);

                writer.println ("Add");
            }

            writer.flush ();
            writer.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    /**
     * Sets API key to use with the YouTube v3 requests
     * 
     * @param apiKey
     *            Key
     */
    public void setApiKey (String apiKey)
    {
        this.apiKey = apiKey;
    }

    /**
     * Set the channel id to use with the YouTube v3 requests
     * 
     * @param channelID
     *            Channel id
     */
    public void setChannelID (String channelID)
    {
        this.channelID = channelID;
    }

    /**
     * Retrieves the upload playlist id, necessary for retrieving all of the
     * uploaded videos
     */
    private void getUploadPlayListID ()
    {
        try
        {
            YouTube.Channels.List channelsList = uTube.channels ().list (
                    parms.getProperty (RunparmDefs.YOUTUBE_CHANNELS_LIST_PART));
            
            /*
             * YouTube documentation says that YouTube user name can be used to
             * retrieve the playlist, but I could only get the upload id using
             * the channel id
             */
            if (channelID != null) channelsList.setId (channelID);
            
            channelsList.setKey (apiKey);

            ChannelListResponse channelListResponse = channelsList.execute ();

            List<Channel> channelItems = channelListResponse.getItems ();

            if (channelItems == null) throw new RuntimeException ("Null channel items");

            // Expected channel size is 1;
            if (channelItems.size () != 1) { throw new RuntimeException (
                    "Unexpected channel size of " + channelItems.size ()); }

            Channel c = channelItems.get (0);
            ChannelContentDetails details = c.getContentDetails ();
            RelatedPlaylists playlists = details.getRelatedPlaylists ();

            uploadID = playlists.getUploads ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }
}
