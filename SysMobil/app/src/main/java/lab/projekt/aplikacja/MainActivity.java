package lab.projekt.aplikacja;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button startBtn;
    Button closeBtn;
    Button histBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);
        histBtn = (Button) findViewById(R.id.displayHistoryBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent StatIntent = new Intent(MainActivity.this, StatActivity.class);
               startActivity(StatIntent);
           }
        });

        histBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent histIntent = new Intent(MainActivity.this,
                                                   TrainingActivity.class);
                startActivity(histIntent);
            }
        });


        closeBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
