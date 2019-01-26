package lab.projekt.aplikacja;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;



public class StatActivity extends Activity {

    BroadcastReceiver receiver;

    //uchwyt do obiektu bazy danych SQLite
    GpsDBHelper db;

    Button stopBtn;
    Button closeBtn;

    Chronometer timeMeasurement;
    TextView distanceText;

    //szerokość geograficzna
    double actLatitude = 0.0;
    //długość geograficzna
    double actLongitude = 0.0;

    //szerokość geograficzna
    double prevLatitude = 0.0;
    //długość geograficzna
    double prevLongitude = 0.0;

    //określa dystans trasy
    double distance = 0.0;

    //określa numer ścieżki
    int trackNumber = 1;

    String startDistText = "0.0 km";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);
        timeMeasurement = (Chronometer) findViewById(R.id.time);
        distanceText = (TextView) findViewById(R.id.distText);

        distanceText.setText(startDistText);
        timeMeasurement.start();

        //uruchomienie obsługi bazy danych SQLite
        db = new GpsDBHelper(this);

        trackNumber = getPreviousTrackNumber();
        trackNumber += 1;

        //sprawdzenie czy wyrażono zgodę na dostęp do lokalizacji
        if (!check_permissions()) {
            enableContent();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String distanceStr = "";

                    // ustawienie formatu czasu zegara
                    DecimalFormat decimalFormat = new DecimalFormat("#.###");
                    double dValue;

                    actLatitude = intent.getDoubleExtra("latitude", -1);
                    actLongitude = intent.getDoubleExtra("longitude", -1);
                    // zapis współrzędnych do tabeli coordinates
                    db.insertIntoCoordinates(trackNumber, actLatitude, actLongitude);

                    if (prevLongitude != 0) {
                        // obliczenie dystansu
                        distance += calculateDistance(prevLatitude, prevLongitude, actLatitude,
                                actLongitude);
                    }

                    prevLatitude = actLatitude;
                    prevLongitude = actLongitude;

                    dValue = Double.valueOf(decimalFormat.format(distance));
                    distanceStr = Double.toString(dValue);
                    distanceStr += " km";
                    distanceText.setText(distanceStr);
                }
            };
        }
        registerReceiver(receiver, new IntentFilter("updateLocation"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //zakończenie obsługi usługi GPS
        Intent gpsStopIntent = new Intent(getApplicationContext(), GpsService.class);
        stopService(gpsStopIntent);
        if (receiver != null){
            unregisterReceiver(receiver);
        }
    }

    /**
     * Pobiera wartość określającą poprzednio wyznaczoną trasę
     * @return numer poprzedniej trasy
     */
    public int getPreviousTrackNumber() {
        return db.getMaxTrackData();
    }

    private boolean check_permissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    enableContent();
                }
            } else {
                check_permissions();
            }
        }
    }

    /**
     * Metoda wywoływana wtedy uzytkownik wyraził zgodę na dostęp do lokalizacji
     */
    private void enableContent() {
        //uruchomienie obsługi usługi GPS
        Intent gpsStartIntent = new Intent(getApplicationContext(), GpsService.class);
        startService(gpsStartIntent);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat, lng;

                timeMeasurement.stop();

                long msec = SystemClock.elapsedRealtime() - timeMeasurement.getBase();   //[ms]
                double sec = msec / 1000;
                double minutes = sec / 60;

                //zatrzymanie odczytywania danych
                Intent gpsStopIntent = new Intent(getApplicationContext(), GpsService.class);
                stopService(gpsStopIntent);

                //zapisanie obliczonych danych: czasu i pokonanego dystansu
                db.insertIntoStatistics(trackNumber, minutes, distance);

                Intent drawMapIntent = new Intent(StatActivity.this,
                        MapsActivity.class);

                ArrayList<String> latArray = new ArrayList<String>();
                ArrayList<String> lngArray = new ArrayList<String>();
                Cursor cur = db.getTrackData(trackNumber);
                while (cur.moveToNext()) {
                    lat = cur.getDouble(2);
                    lng = cur.getDouble(3);
                    latArray.add(Double.toString(lat));
                    lngArray.add(Double.toString(lng));
                }
                cur.close();

                Bundle dataBundle = new Bundle();
                dataBundle.putStringArrayList("latitudeArray", latArray);
                dataBundle.putStringArrayList("longitudeArray", lngArray);

                // Przesłanie danych i uruchomienie Aktywności z mapą
                drawMapIntent.putExtras(dataBundle);
                startActivity(drawMapIntent);
            }
        });
    }

    /**
     * Oblicza dystans na podstawie podanych wartości współrzędnych geograficznych
     * @param pLatitude poprzednia wartość szerokości geograficznej
     * @param pLongitude poprzednia wartość długości geograficznej
     * @param aLatitude bieżąca wartość szerokości geograficznej
     * @param aLongitude bieżaca wartość długości geograficznej
     * @return obliczona wartość dystansu
     */
    public double calculateDistance(double pLatitude, double pLongitude, double aLatitude,
                                 double aLongitude) {
        //na podstawie wzorów ze strony https://www.movable-type.co.uk/scripts/latlong.html
        int R = 6371; // średni promień Ziemi [km]
        double phi1 = Math.toRadians(pLatitude);//lat1.toRadians();
        double phi2 = Math.toRadians(aLatitude);
        double deltaPhi = Math.toRadians(aLatitude - pLatitude);
        double deltaLambda = Math.toRadians(aLongitude - pLongitude);

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda/2) * Math.sin(deltaLambda/2);
        double part = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * part;
    }
}