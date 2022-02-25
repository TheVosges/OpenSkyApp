package edu.ib.openskyapp;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Klasa Profile służy do przechowywania informacji o dodanych lotach i ich obsługi.
 */
public class Profile implements Serializable  {


    public ArrayList<OpenSky> favFlights;


    public Profile() {
        this.favFlights = new ArrayList<>();
    }

    public ArrayList<OpenSky> getFavFlights() {
        return favFlights;
    }

    public void setFavFlights(ArrayList<OpenSky> favFlights) {
        this.favFlights = favFlights;
    }

    /**
     * Metoda dodająca nowy lot do profilu.
     * @param flight
     */
    public void addFav(OpenSky flight){
        this.favFlights.add(flight);
    }



    @Override
    public String toString() {
        return "Profile{" +
                "favFlights=" + favFlights +
                '}';
    }

    /**
     * Metoda usuwająca wybrany lot z profilu.
     * @param item
     */
    public void removeItem(OpenSky item){
        int i = favFlights.indexOf(item);
        System.out.println(i);
        if (i>-1)
        this.favFlights.remove(i);
    }


}
