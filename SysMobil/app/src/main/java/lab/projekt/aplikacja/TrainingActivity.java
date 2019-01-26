package lab.projekt.aplikacja;

import android.app.Activity;
import android.database.Cursor;
import android.icu.text.DecimalFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TrainingActivity extends Activity {

    TextView trackView;
    Button backBtn;

    //uchwyt do obiektu bazy danych SQLite
    GpsDBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        trackView = (TextView) findViewById(R.id.textView);
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //uruchomienie obsługi bazy danych SQLite
        db = new GpsDBHelper(this);

        int trackNumber;
        double timeVal, tValue;
        double distVal, dValue;

        DecimalFormat decimalFormatDistance = new DecimalFormat("#.###");
        DecimalFormat decimalFormatTime = new DecimalFormat("#.##");


        Cursor cur = db.getAllStatistic();

        trackView.append("Nr trasy\t\tCzas [min]\t\tDystans [km]\n");
        while (cur.moveToNext()) {
            trackNumber = cur.getInt(1);
            timeVal = cur.getDouble(2);
            distVal = cur.getDouble(3);


            dValue = Double.valueOf(decimalFormatDistance.format(distVal));
            tValue = Double.valueOf(decimalFormatTime.format(timeVal));

            trackView.append(Integer.toString(trackNumber));
            trackView.append("                ");
            trackView.append(Double.toString(tValue));
            trackView.append("                ");
            trackView.append(Double.toString(dValue));
            trackView.append("\n\n");
        }
    }
}
