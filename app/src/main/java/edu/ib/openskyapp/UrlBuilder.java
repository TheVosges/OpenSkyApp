package edu.ib.openskyapp;

import java.text.ParseException;

/**
 * Klasa UrlBuilder służy do tworzenia odpowiednich zapytań dla OpenSky API.
 */
public class UrlBuilder {


    public UrlBuilder() {
    }

    /**
     * Returns url za pomocą, którego można pobrać wybrane dane z OpenSky API.
     * @param time
     * @param icao
     * @return url
     */
    public String createUrl(Long time, String icao) {

        String base = "https://Najda:lastminute11@opensky-network.org/api/states/all?time=";

        StringBuilder url = new StringBuilder();
        url.append(base);
        url.append(time.toString());
        url.append("&icao24=");
        url.append(icao);
        return url.toString();
    }


    /**
     * Returns przekonwertowana data na format epoch.
     * @param date
     * @return epoch
     */
    public long convertDateToEpoch(String date){
        long epoch = 0;
        date = date.replace("T", " ");
        try {
            epoch = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date + " 00:00:00").getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return epoch;
    }


}
