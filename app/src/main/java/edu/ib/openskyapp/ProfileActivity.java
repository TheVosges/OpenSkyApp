package edu.ib.openskyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Profil użytkownika.
 */
public class ProfileActivity extends AppCompatActivity {

    private Profile profile;
    private boolean running = true;

    /**
     * Tworzenia głównego okna aplikacji
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profile = (Profile) getIntent().getSerializableExtra("Profile");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My notification", "OpenSkyApp", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);}

        showData();

        ImageView imgRefresh = (ImageView) findViewById(R.id.btnRefresh);
        imgRefresh.setOnClickListener(new View.OnClickListener() {
            /**
             * Metoda opisująca działanie przycisku odświeżenia danych
             * @param v
             */
            @Override
            public void onClick(View v) {
                System.out.println(profile);
                showData();
            }
        });
        runTimer();
    }

    /**
     * Metoda tworząca i wyświetlająca listę ulubionych lotów.
     @return arrayFlightsList
     */
    public void showData(){
        //Tu się dodaje loty do listy wyświetlanych lotów
        ArrayList<Flight> arrayFlights = new ArrayList<>();
        //Zmienna odpowiedzialna za zmianę ikonu każdego wiersza lotu
        int plane = 0;

        //Pętla tworząca obiekty kalsy Flight w liście ArrayList
        for (OpenSky flight: profile.getFavFlights()) {

            String label = flight.getCallsign();
            double distance = flight.distance();
            double time = flight.timeToPoint();

            //DODAWANIE LOTÓW Z RÓŻNYMI IKONAMI
            if (plane == 0) {
                Flight flight1 = new Flight(label, R.drawable.plane1, distance, time, flight);
                plane += 1;
                arrayFlights.add(flight1);
            } else {
                Flight flight1 = new Flight(label, R.drawable.plane2, distance, time, flight);
                plane = 0;
                arrayFlights.add(flight1);
            }

        }

        //Tu się inicjuje adapter wyświetlający loty
        RecyclerView rvFlights = findViewById(R.id.RVFavFlights);
        FlightAdapter adapter = new FlightAdapter(arrayFlights, profile);
        adapter.notifyDataSetChanged();
        rvFlights.setHasFixedSize(true);
        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        rvFlights.setAdapter(adapter);
    }

    /**
     * Metoda odpowiadająca za aktualizowanie listy wybranych lotów.
     @return stringRequest
     */
    public OpenSky temp;
    public void updateData(OpenSky flight) {
        LocalDateTime now = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
            UrlBuilder builder = new UrlBuilder();
            String url = builder.createUrl(builder.convertDateToEpoch(now.toString()), flight.getIcao24());
            temp = flight;

            //Wysłanie zapytanie i oczekiwanie na odpowiedź w OnResponse i OnErrorResponse
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this::onResponseUpdate, this::onErrorResponseUpdate){
            };

            queue.add(stringRequest);
        }
    }


    /**
     * Metoda przetwarzająca pozytywną odpowiedź z OpenSky API.
     * @param response
     */
    private void onResponseUpdate (String response) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        OpenSky newOpenSky = gson.fromJson(response, OpenSky.class);
        if (!(newOpenSky.getStates() == null)) {
            newOpenSky.transformStates();
            temp.setLongitude(newOpenSky.getLongitude());
            temp.setLatitude(newOpenSky.getLatitude());
            temp.setOn_ground(newOpenSky.isOn_ground());
            temp.setVelocity(newOpenSky.getVelocity());
        }
        else{
            Toast.makeText(this, "Error while downloading data", Toast.LENGTH_LONG).show();
        }
    }

     /**
     * Metoda przetwarzająca negatywną odpowiedź z OpenSky API.
     * @param volleyError
     */
    private void onErrorResponseUpdate (VolleyError volleyError){
        Log.e("mes", "failed");
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_LONG).show();
    }

     /**
     * Metoda przekazująca dane między aktywnościami po wciśnięciu przycisku powrotu
     * @return profile
     */
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Profile",profile);
        setResult(Activity.RESULT_OK,returnIntent);
        running =false;
        finish();
    }

    /**
     * Metoda działająca w tle odpowiedzialna za aktualizowanie danych obserowanych lotów oraz za
     * budowanie powiadmień gdy:
     * - czas lotu będzie mniejszy niż oczekiwany
     * - samolot wyląduje (czas lotu będzie mniejszy niż 1 minuta)
     */
    public void runTimer() {

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (running) {
                    //System.out.println(profile);
                    for (OpenSky flight: profile.getFavFlights()){
                        updateData(flight);
                        if (flight.timeToPoint() < flight.getNotifyMin()*60 && !flight.isNotified()){
                            buildNotification(flight);
                            flight.setNotified(true);
                        }
                        if (flight.timeToPoint() < 60){
                            buildNotificationOnGround(flight.getCallsign());
                            profile.removeItem(flight);
                        }
                    }
                }

                handler.postDelayed((this::run), 1000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Metoda tworząca i wywołująca powiadomienie o pozostałym czasie lotu i odległości.
     * @return notification
     */
    public void buildNotification(OpenSky flight){

        int time = (int) Math.round(flight.timeToPoint() / 60);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ProfileActivity.this, "My notification")
                            .setSmallIcon(R.drawable.plane1)
                            .setContentTitle("Your flight " + flight.getCallsign() + "will arrive soon!")
                            .setContentText("Time of arrival: " + time + " minutes")
                            .setAutoCancel(true);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ProfileActivity.this);
            managerCompat.notify(1, mBuilder.build());
        }
    }

    /**
     * Metoda tworząca i wywołująca powiadomienie o wylądowaniu samolotu.
     * @return notification
     */
    public void buildNotificationOnGround(String flight){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ProfileActivity.this, "My notification")
                            .setSmallIcon(R.drawable.plane1)
                            .setContentTitle("Your flight " + flight + "arrived!")
                            .setAutoCancel(true);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ProfileActivity.this);
            managerCompat.notify(1, mBuilder.build());
        }
    }

}