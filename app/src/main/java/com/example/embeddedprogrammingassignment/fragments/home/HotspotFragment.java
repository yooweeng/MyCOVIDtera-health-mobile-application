package com.example.embeddedprogrammingassignment.fragments.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.engine.Resource;
import com.example.embeddedprogrammingassignment.R;
import com.example.embeddedprogrammingassignment.modal.RedZoneLocation;
import com.example.embeddedprogrammingassignment.modal.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotspotFragment extends Fragment {

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    TextView tvCases;
    User user;
    private static final int LOCATION_PERMISSION_CODE = 101;
    ArrayList<RedZoneLocation> zoneList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_hotspot, container, false);

        tvCases = view.findViewById(R.id.tvZoneCases);

        user = Parcels.unwrap(getArguments().getParcelable("activeUser"));

        tvCases.setText("Hi " + user.getName() + ", there has been 28 reported case(s) of COVID-19 within a 1 km radius from your current position in the last 14 days.");

        // Initialize map fragment
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMaps);

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        getPermission();

        return view;
    }

    public void getPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getCurrentLocation();
                }
            },2000);
        }
    }
    private void getCurrentLocation(){
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                // Async map
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        // Initialize lat lug
                        LatLng latLng;
                        if (location==null)
                            latLng = new LatLng(2.8325, 101.70694);
                         else
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Initialize marker options
                        MarkerOptions markerOptions = new MarkerOptions();
                        // Set position of marker
                        markerOptions.position(latLng);
                        // Set title of marker
                        markerOptions.title(latLng.latitude + ": " + latLng.longitude);
                        // Remove all marker
                        googleMap.clear();
                        // Animating to zoom the marker
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        // Add marker on map
                        googleMap.addMarker(markerOptions);

                        getRedZone(googleMap, latLng.latitude, latLng.longitude);

                    }
                });
            }
        });
    }

    private void getRedZone(GoogleMap googleMap, double latitude, double longitude) {

        FirebaseDatabase.getInstance().getReference("hotspots").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    Log.i("MapError snapshot", String.valueOf(snapshot.getChildren()));
                    for(DataSnapshot child: snapshot.getChildren()) {
                        String cases = Objects.requireNonNull(child.child("cases").getValue()).toString();
                        String lg = Objects.requireNonNull(child.child("longitude").getValue()).toString();
                        String lt = Objects.requireNonNull(child.child("latitude").getValue()).toString();
                        Log.i("MapError loop", "cases: " + cases + " lat: " + lt + " long:" +  lg);
                        RedZoneLocation zone = new RedZoneLocation(Integer.parseInt(cases), Double.parseDouble(lt), Double.parseDouble(lg));
                        Log.i("MapError object", zone.toString());
                        zoneList.add(zone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("MapError Zone List", zoneList.toString());
                for (int i=0; i<zoneList.size(); i++) {
                    googleMap.addCircle(new CircleOptions()
                            .center(new LatLng(zoneList.get(i).getLatitude(), zoneList.get(i).getLongitude()))
                            .radius(500)
                            .strokeWidth(3f)
                            .strokeColor(Color.RED)
                            .fillColor(Color.argb(70,150,50,50)));
                }
            }
        },1500);

    }

}