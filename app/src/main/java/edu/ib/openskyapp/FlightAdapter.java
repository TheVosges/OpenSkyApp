package edu.ib.openskyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Klasa służąca do inicjowania obiektów RecycleView w celu ich wyświetlenia.
 */
public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {
    private ArrayList<Flight> flights;
    public Profile profile;

    public FlightAdapter(ArrayList<Flight> flights, Profile profile) {
        this.flights = flights;
        this.profile = profile;
    }

    /**
     * Metoda inicjująca stworzenie holdera.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.flight_row, parent, false);
        FlightViewHolder flightViewHolder = new FlightViewHolder(listItem);
        return flightViewHolder;
    }

    /**
     * Metoda inicująca wartości obiektów klasy FlightViewHolder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {

        int time = (int) Math.round(flights.get(position).getTime() / 60); 
        double distance = Math.round(flights.get(position).getDistance() / 1000 * 10.0) / 10.0;

        holder.imPhoto.setImageResource(flights.get(position).getImgID());
        holder.tvLabel.setText(flights.get(position).getLabel().trim());
        holder.tvDistance.setText(String.valueOf(distance) + " km");
        holder.tvTime.setText(time + " min");
        holder.flight = flights.get(position).getOpenSky();
    }

    /**
     * Metoda zwracająca długość tablicy lotów do wyświetlenia
     * @return
     */
    @Override
    public int getItemCount() {
        return flights.size();
    }

    /**
     * Obiekt RecycleView - flight_row.xml.
     * Do obiektu jezt przypisana referencja przypisanego lotu - OpenSky flight
     */
    public class FlightViewHolder extends RecyclerView.ViewHolder{
        ImageView imPhoto;
        TextView tvLabel;
        TextView tvDistance;
        TextView tvTime;
        ImageView imgDelete;
        OpenSky flight;


        public FlightViewHolder(@NonNull View itemView){
            super(itemView);
            imPhoto = itemView.findViewById(R.id.imPhoto);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTime = itemView.findViewById(R.id.tvTime);

            /**
             * Metoda opisująca działanie przycisku usuwania dodanego lotu
             */
            itemView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profile.removeItem(flight);
                }
                });
                }
        }

    }

