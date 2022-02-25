package edu.ib.openskyapp;

/**
 * Klasa Ariports służy do prezchowania informacji o lotniskach.
 */
public enum Ariports {
    WROCLAW(0, 51.102, 16.885, "Wrocław"),
    WARSZAWA(1,52.165, 20.967, "Warszawa");
    
    public final int id;
    public final double latitude;
    public final double longitude;
    public final String city;

    Ariports(int id, double latitude, double longitude, String city) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public int getId() {
        return id;
    }
}
