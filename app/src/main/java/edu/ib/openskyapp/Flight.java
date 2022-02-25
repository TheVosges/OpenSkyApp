package edu.ib.openskyapp;

/**
 * Klasa Flight służy do przechowania informacji o locie w celu ich wyświetlenia w RecycleView w kalsie FlightAdapter.
 */
public class Flight {

    private String label;
    private int imgID;
    private double distance;
    private double time;
    private OpenSky openSky;

    public Flight(String label, int imgID, double distance, double time, OpenSky openSky) {
        this.label = label;
        this.imgID = imgID;
        this.distance = distance;
        this.time = time;
        this.openSky = openSky;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getImgID() {
        return imgID;
    }

    public void setImgID(int imgID) {
        this.imgID = imgID;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public OpenSky getOpenSky() {
        return openSky;
    }

    public void setOpenSky(OpenSky openSky) {
        this.openSky = openSky;
    }
}
