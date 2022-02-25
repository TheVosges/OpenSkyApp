package edu.ib.openskyapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.opensky.example.ExampleDecoder;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Główne okno aplikacji.
 */
public class MainActivity extends AppCompatActivity {
    private EditText flight_id;
    private EditText edtMins;
    private Spinner spinner;
    private boolean running = true;
    public Profile profile;
    public Ariports ariports;

    /**
     * Metoda wywoływana podczas tworzenia głównego okna oplikacji
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
         * Inicjajca kanału powiadomień ze względu na wymagania Androida od wersji 8.0
         *
         * https://developer.android.com/guide/topics/ui/notifiers/notifications#bundle
         * Starting in Android 8.0 (API level 26), all notifications must be assigned to a channel or it will not appear.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My notification", "OpenSkyApp", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);}

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Utworzenie profilu użytkownika
        profile = new Profile();


        flight_id = (EditText) findViewById(R.id.edtFlightID);
        edtMins = (EditText) findViewById(R.id.edtNotifyMin);
        spinner = (Spinner) findViewById(R.id.spinner);

        //Inicjajca spinnera
        String[] elements = {"Wrocław", "Warszawa"};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, elements);
        spinner.setAdapter(adapter);


        //Przechodzenie do okna profilu
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            /**
             * Metoda odpowiedzialna za wyświetlenie okna profilu
             * @param v
             */
            @Override
            public void onClick(View v) {
                //Zatrzymanie działania pętli aktualizowania danych w oknie MainActivity
                running = false;

                Intent intent2 = new Intent(getApplicationContext(), ProfileActivity.class);
                intent2.putExtra("Profile", profile);
                //Metoda pozwalająca połączenie referencji profliu z okna głównego do okna profilu
                startActivityForResult(intent2,101);
                }
        });

        //Metoda odpowiedzialna za aktualizowanie informacji i wysyłanie powiadomień
        runTimer();
    }

    /**
     * Metoda opisujac działanie przycisku dodającego nowy obserwowany lot do profilu.
     * Zebezpieczono przed nieprawidłowym użyciem:
     * - wprowadzenie danych ze spacjami
     * - wprowadzenie danych w złym formacjie
     * - wprowadzenie złego id lotu
     *
     * @param view
     */
    public void SearchClicked(View view){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();

            if (flight_id.getText().toString().matches("[a-zA-Z0-9]*") && !flight_id.getText().toString().equals("")
                && !edtMins.getText().toString().equals("")) {

                //Tworzenie ciągu zapytania
                UrlBuilder builder = new UrlBuilder();
              String url = builder.createUrl(builder.convertDateToEpoch(now.toString()), flight_id.getText().toString());
                Log.i("mes", url);

                //Wysłanie zapytanie i oczekiwanie na odpowiedź w OnResponse i OnErrorResponse
                RequestQueue queue = Volley.newRequestQueue(this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this::onResponse, this::onErrorResponse);
                queue.add(stringRequest);

            } else Toast.makeText(this, "Invalid data.", Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Metoda inicjowania przez pozytywne uzyskanie informacji z API OpenSky
     * @param response
     */
    private void onResponse(String response) {

        //Sprawdzenie połączenia z API
        Log.i("OpenSkyAPI", "connected");
        Log.i("OpenSkyAPI", response);

        //Rozpakowanie lotu jeżeli jest w trakcie
        Gson gson =  new GsonBuilder().setPrettyPrinting().create();
        OpenSky openSky = gson.fromJson(response, OpenSky.class);

        //Sprawdzenie czy otrzymano inforamcje o stanie lotu
       if (!(openSky.getStates() == null)) {

            //Wczytanie danych do pól
           openSky.transformStates();

           //Sprawdzenie pozycji wybranego lotniska
           double longitude = 0;
           double latitude = 0;
           for (Ariports airport : Ariports.values()) {
               if (airport.getCity().equals(spinner.getSelectedItem().toString())) {
                   latitude = airport.getLatitude();
                   longitude = airport.getLongitude();
               }
           }

           //Ustawienie zmiennych lotu (czas powiadomienia, miejsce pomiaru odległości)
           openSky.setData(Float.parseFloat(String.valueOf(latitude)), Float.parseFloat(String.valueOf(longitude)), Integer.parseInt(edtMins.getText().toString()));

           //Sprawdzenie, czy lot został już dodany
           boolean containsFlight = false;
           for (OpenSky flight : profile.getFavFlights()) {
               if (openSky.getCallsign().equals(flight.getCallsign())) containsFlight = true;
           }

           if (!containsFlight) {
               profile.addFav(openSky);
               Toast.makeText(this, "Flight added to your profile.", Toast.LENGTH_LONG).show();
           } else
               Toast.makeText(this, "This flight has been already added.", Toast.LENGTH_LONG).show();
       }
       else{
           Toast.makeText(this, "Invalid flight ID", Toast.LENGTH_LONG).show();
       }
    }

    /**
     * Metoda inicjowana przez nieudaną próbę połączenia z API OpenSky
     * @param error
     */
    private void onErrorResponse(VolleyError error) {
        Log.i("mes", "failed");
//        Log.i("mes", String.valueOf(error.networkResponse.statusCode));
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_LONG).show();
    }

    /**
     * Metoda budująca powiadomienie o zbliżajacym się samolocie do wybranego lotniska
     * @param flight
     */
    public void buildNotification(OpenSky flight){

        int time = (int) Math.round(flight.timeToPoint() / 60);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MainActivity.this, "My notification")
                            .setSmallIcon(R.drawable.plane1)
                            .setContentTitle("Your flight " + flight.getCallsign() + "will arrive soon!")
                            .setContentText("Time of arrival: " + time + " minutes")
                            .setAutoCancel(true);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            managerCompat.notify(1, mBuilder.build());
        }
    }

    /**
     * Metoda budująca powiadomienie o zakończonym locie
     * @param flight
     */
    public void buildNotificationOnGround(String flight){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MainActivity.this, "My notification")
                            .setSmallIcon(R.drawable.plane1)
                            .setContentTitle("Your flight " + flight + "arrived!")
                            .setAutoCancel(true);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            managerCompat.notify(1, mBuilder.build());
        }


    }


    public OpenSky temp;

    /**
     * Metoda odpowiedzialna za aktualizowanie danych obserwowanych lotów
     * @param flight
     */
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
     * Metoda inicjowana przez pozytywne otrzymanie odpowiedzi od API w trakcie aktualizowanie danych obserowanych lotów
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
     * Metoda inicjowana przez negatywne otrzymanie odpowiedzi od API w trakcie aktualizowanie danych obserowanych lotów
     * @param volleyError
     */
    private void onErrorResponseUpdate (VolleyError volleyError){
        Log.i("mes", "failed");
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_LONG).show();
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
                    for (OpenSky flight: profile.getFavFlights()){
                        //Aktualizowanie danych
                        updateData(flight);

                        //Powiadomienie po przekroczeniu oczekiwanego czasu
                        if (flight.timeToPoint() < flight.getNotifyMin()*60 && !flight.isNotified()){
                            buildNotification(flight);
                            flight.setNotified(true);
                        }

                        //Powiadomienie gdy samolot ląduje
                        if (flight.timeToPoint() < 60){
                            buildNotificationOnGround(flight.getCallsign());
                            profile.removeItem(flight);
                        }
                    }
                }

                //Pętla powtarzana co 10 sekund
                handler.postDelayed((this::run), 10000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Metoda opisujaca działanie, gdy użytkownik powróci z okna profilu.
     * Ponowna inicjacja działania metody runTimer poprzez ustawienie zmiennej running
     * Aktualizowanie danych profilu
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                profile = (Profile) data.getSerializableExtra("Profile");
                running = true;
            }

        }
    }
}