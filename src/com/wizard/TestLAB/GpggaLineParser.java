package com.wizard.TestLAB;

/**
 * Created by Koen on 04-Jul-14.
 * Class to parse String lines of GPGGA Global Positioning System Fixed Data
 *
 */
public class GpggaLineParser
{
    private boolean valid;
    private String utcTime;
    private double latitude;
    private String northSouthIndicator;
    private double longitude;
    private String eastWestIndicator;
    private int positionFixIndicator;
    private int satellitesUsed;
    private double hdop;
    private double mslAltitude;
    private String unitAltitude;
    private String geoIdSeperation;
    private String unitGeoId;

    public GpggaLineParser(String gpggaLine)
    {
        String[] split = gpggaLine.split(",");
        for(int i=0; i < split.length; i++)
        {
            split[i] = split[i].trim();
        }
        positionFixIndicator = Integer.parseInt(split[6]);
        if(split[0].equals("$GPGGA") && positionFixIndicator > 0) // Check protocolHeader
        {
            valid = true;
            utcTime = split[1];
            latitude = this.convertLatitudeDMStoDecimalDegrees(split[2], split[3]);
            northSouthIndicator = split[3];
            longitude = this.convertLongitudeDMStoDecimalDegrees(split[4], split[5]);
            eastWestIndicator = split[5];
            satellitesUsed = Integer.parseInt(split[7]);
            hdop = Double.parseDouble(split[8]);
            mslAltitude = Double.parseDouble(split[9]);
            unitAltitude = split[10];
            geoIdSeperation = split[11];
            unitGeoId = split[12];
        }
        else
        {
            valid = false;
            utcTime = "invalid";
            latitude = -1.0;
            northSouthIndicator = "invalid";
            longitude = -1.0;
            eastWestIndicator = "invalid";
            positionFixIndicator = -1;
            satellitesUsed = -1;
            hdop = -1.0;
            mslAltitude = -1.0;
            unitAltitude = "invalid";
            geoIdSeperation = "invalid";
            unitGeoId = "invalid";
        }
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getNorthSouthIndicator() {
        return northSouthIndicator;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getEastWestIndicator() {
        return eastWestIndicator;
    }

    public int getPositionFixIndicator() {
        return positionFixIndicator;
    }

    public int getSatellitesUsed() {
        return satellitesUsed;
    }

    public double getHdop() {
        return hdop;
    }

    public double getMslAltitude() {
        return mslAltitude;
    }

    public String getUnitAltitude() {
        return unitAltitude;
    }

    public String getGeoIdSeperation() {
        return geoIdSeperation;
    }

    public String getUnitGeoId() {
        return unitGeoId;
    }

    /**
     * Method to convert geographical Latitude DMS format ddmm.mmmm to DD (decimal degrees)
     * @param ddmmmmmm DMS formatted Latitude
     * @param orientation N,E,S,W indication
     * @return (double) decimal degrees Latitude
     */
    private double convertLatitudeDMStoDecimalDegrees(String ddmmmmmm, String orientation)
    {
        /*
        Split your GPS values into dd and mm.mmmm.
        Divide the mm.mmmm by 60 and add it to the dd.
        Then multiply the result by -1 if the direction is S or W.
         */
        if (orientation.equals("N") || orientation.equals("S"))
        {
            String degrees = ddmmmmmm.substring(0,2); // begin index, inclusive -- end index, exclusive
            double dd = Double.parseDouble(degrees);
            String minutes = ddmmmmmm.substring(2, ddmmmmmm.length());
            double mm = Double.parseDouble(minutes);
            double decimalDegrees = dd + (mm / 60);
            if (orientation.equals("S"))
                decimalDegrees = decimalDegrees * -1;
            return decimalDegrees;
        }
        else
        {
            return -1.0;
        }
    }

    /**
     * Method to convert geographical Longitude DMS format dddmm.mmmm to DD (decimal degrees)
     * @param dddmmmmmm DMS formatted Longitude
     * @param orientation N,E,S,W indication
     * @return (double) decimal degrees Longitude
     */
    private double convertLongitudeDMStoDecimalDegrees(String dddmmmmmm, String orientation)
    {
        /*
        Split your GPS values into ddd and mm.mmmm.
        Divide the mm.mmmm by 60 and add it to the dd.
        Then multiply the result by -1 if the direction is S or W.
         */
        if(orientation.equals("E") || orientation.equals("W"))
        {
            String degrees = dddmmmmmm.substring(0,3); // begin index, inclusive -- end index, exclusive
            double ddd = Double.parseDouble(degrees);
            String minutes = dddmmmmmm.substring(3, dddmmmmmm.length());
            double mm = Double.parseDouble(minutes);
            double decimalDegrees = ddd + (mm / 60);
            if (orientation.equals("S"))
                decimalDegrees = decimalDegrees * -1;
            return decimalDegrees;
        }
        else
        {
            return -1.0;
        }
    }


}

