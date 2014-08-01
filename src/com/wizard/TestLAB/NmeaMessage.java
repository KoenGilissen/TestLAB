package com.wizard.TestLAB;

import android.util.Log;

/**
 * Created by Koen on 7/31/2014.
 */
public abstract class NmeaMessage
{
    protected boolean checksumValid;
    protected String originalMessage;

    public NmeaMessage(String nmeaMsg)
    {
        originalMessage = nmeaMsg;
        checksumValid = verifyChecksum(nmeaMsg);
    }

    protected boolean isChecksumValid()
    {
        return checksumValid;
    }

    protected String getOriginalMessage()
    {
        return originalMessage;
    }


    public boolean verifyChecksum(String nmea) //e.g.: "$GPGLL,5300.97914,N,00259.98174,E,125926,A*28<CR><LF>"
    {
        String checksumValue = nmea.substring(nmea.indexOf('*')+1, nmea.indexOf('*')+3); // "28"
        int checksum = 0xFF;
        int checksumCalculated = 0x00;
        try
        {
            checksum = Integer.parseInt(checksumValue, 16);
        }
        catch(Exception e)
        {
            Log.d("NMEAMESSAGE", "Error parsing checksum: " + e.toString());
        }
        //Get inner string between $ and *
        String strippedMsg = nmea.substring(nmea.indexOf('$')+1, nmea.indexOf('*')); // begin index, inclusive -- end index, exclusive  "GPGLL,5300.97914,N,00259.98174,E,125926,A"
        for(int i = 0; i < strippedMsg.length(); i++)
        {
            int characterValue = strippedMsg.charAt(i);
            checksumCalculated = checksumCalculated ^ characterValue; //XOR the characters
        }
        if(checksumCalculated == checksum)
            return true;
        else
            return false;
    }


}
