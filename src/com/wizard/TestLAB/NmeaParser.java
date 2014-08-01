package com.wizard.TestLAB;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Koen on 7/31/2014.
 */
public class NmeaParser extends AsyncTask<String, Integer, NmeaMessage>
{
    private final static boolean DEBUGMODE = true;
    private final static String DEBUGTAG = "ASYNCTASK_NMEAPARSER";
    private Context ctx;
    private NmeaLineParsingEvent eventListener;


    public interface NmeaLineParsingEvent
    {
        void parsingCompleted(NmeaMessage msg);
    }


    public NmeaParser(Context ctx, NmeaLineParsingEvent nmeaLineParsingEventListener)
    {
        this.ctx = ctx;
        this.eventListener = nmeaLineParsingEventListener;

    }


    @Override
    protected void onPreExecute()
    {}

    @Override
    protected NmeaMessage doInBackground(String... nmeaLine)
    {
        if(nmeaLine.length == 1) // Varargs or ellipsis can be zero so should be tested for this
        {
            if(nmeaLine[0].contains("$GPGGA"))
            {
                GpggaLineParser gpggaData = new GpggaLineParser(nmeaLine[0]);
                return gpggaData;
            }
            else
            {
                return null;
            }
        }
        else //No arguments passed so nothing to do...
        {
            if(DEBUGMODE)
                Log.d(DEBUGTAG, "Arguments passed < 1");
            return null;
        }
    }

    @Override
    protected void onPostExecute(NmeaMessage nmeaMessage)
    {
        eventListener.parsingCompleted(nmeaMessage);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {}

    @Override
    protected void onCancelled(NmeaMessage nmeaMessage)
    {}
}
