package lab.projekt.aplikacja;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener {

    private GoogleMap mMap;

    Button Btn;

    TextView texttest;
    Button backBtn;

    ArrayList<String> latArray;
    ArrayList<String> lngArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        Btn = (Button) findViewById(R.id.stopBtnLocate);


        mapFragment.getMapAsync(this);


        Intent intentFromActivity = getIntent();

        Bundle nBundle = intentFromActivity.getExtras();


        latArray = nBundle.getStringArrayList("latitudeArray");
        lngArray = nBundle.getStringArrayList("longitudeArray");


        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        finish();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double latitude;
        double longitude;
        String markerNum;

        List<LatLng> points = new ArrayList<LatLng>();

        //Polyline polyline1 = mMap.addPolyline()
        for (int i = 0; i < latArray.size(); i++) {
            // Add a marker
            latitude = Double.parseDouble(latArray.get(i));
            longitude = Double.parseDouble(lngArray.get(i));


            points.add(new LatLng(latitude, longitude));

            markerNum = Integer.toString(i);
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title(markerNum));
        }
        mMap.addPolyline(new PolylineOptions()
                                                .clickable(true)
                                                .addAll(points))
                                                .setWidth(2f);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(points.get(points.size() - 1), 15);
        mMap.animateCamera(zoom);
    }
}
