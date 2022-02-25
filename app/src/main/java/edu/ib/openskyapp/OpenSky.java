package edu.ib.openskyapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Klasa OpenSky służy do przechowania informacji, które zwraca zapytanie z API.
 */
public class OpenSky implements Serializable {

    private Long time; //Czas pobrania danych
    private ArrayList<?> states; //Dane samolotu
    private String icao24;
    private String callsign;
    private String origin_country;
    private int time_position;
    private float longitude;
    private float latitude;
    private boolean on_ground;
    private float velocity;

    private float destLongitude;
    private float destLatitude;
    private int notifyMin;

    private boolean notified = false;

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setData(float destLatitude, float destLongitude, int notifyMin) {
        this.destLongitude = destLongitude;
        this.destLatitude = destLatitude;
        this.notifyMin = notifyMin;
    }

    public float getDestLongitude() {
        return destLongitude;
    }

    public float getDestLatitude() {
        return destLatitude;
    }

    public int getNotifyMin() {
        return notifyMin;
    }

    public OpenSky() {
        this.time = time;
        this.states = states;
    }

    @Override
    public String toString() {
        return "OpenSky{" +
                "time=" + time +
                ", states=" + states +
                '}';
    }

    public void setDestLongitude(float destLongitude) {
        this.destLongitude = destLongitude;
    }

    public void setDestLatitude(float destLatitude) {
        this.destLatitude = destLatitude;
    }

    public void setNotifyMin(int notifyMin) {
        this.notifyMin = notifyMin;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public ArrayList<?> getStates() {
        return states;
    }

    public void setStates(ArrayList<?> states) {
        this.states = states;
    }

    /**
     * Returns przekonwertowane dane
     * @return states, icao24, callsign, origin_country, time_position, longitude, latitude, on_ground, velocity
     */
    public void transformStates(){
        String tempTransform = getStates().toString();
        tempTransform = tempTransform.replace("[", "");
        tempTransform = tempTransform.replace("]", "");
        ArrayList<String> tempArray = new ArrayList<>(Arrays.asList(tempTransform.split(",")));
        tempArray.set(3, tempArray.get(3).replace(".","").replace("E9", "").replace(" ", ""));
        this.states = tempArray;
        this.icao24 = ((String) getStates().get(0));
        this.callsign = ((String) getStates().get(1));
        this.origin_country = (String) getStates().get(2);
        this.time_position = Integer.parseInt((String) getStates().get(3)); // in epoch secs
        this.longitude = Float.parseFloat((String) getStates().get(5)); //  in decimal degree
        this.latitude = Float.parseFloat((String) getStates().get(6)); //  in decimal degree
        this.on_ground = Boolean.parseBoolean((String) getStates().get(8));
        this.velocity = Float.parseFloat((String) getStates().get(9));  // m/s

    }

    public String getIcao24() {
        return icao24;
    }

    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getOrigin_country() {
        return origin_country;
    }

    public void setOrigin_country(String origin_country) {
        this.origin_country = origin_country;
    }

    public int getTime_position() {
        return time_position;
    }

    public void setTime_position(int time_position) {
        this.time_position = time_position;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public boolean isOn_ground() {
        return on_ground;
    }

    public void setOn_ground(boolean on_ground) {
        this.on_ground = on_ground;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    /**
     * Returns pozostały dystans do lądowania
     * @param lat1
     * @param lon1
     * @return distance
     */
    public double distance(double lat1, double lon1) {
        double lat2 = this.getLatitude();
        double lon2 = this.getLongitude();
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    /**
     * Returns pozostały czas do lądowania
     * @param lat1
     * @param lon1
     * @return estimatedTimeToArrival
     */
    public double timeToPoint(double lat1, double lon1){
        return distance(lat1, lon1)/this.velocity;
    }

    /**
     * Returns pozostały dystans do lądowania
     * @return distance
     */
    public double distance() {
        double lat2 = this.getLatitude();
        double lon2 = this.getLongitude();
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - destLatitude);
        double lonDistance = Math.toRadians(lon2 - destLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(destLatitude)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    /**
     * Returns pozostały czas do lądowania
     * @return estimatedTimeToArrival
     */
    public double timeToPoint(){
        return distance(destLatitude, destLongitude)/this.velocity;
    }
}
