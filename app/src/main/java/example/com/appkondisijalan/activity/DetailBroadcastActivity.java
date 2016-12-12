package example.com.appkondisijalan.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import example.com.appkondisijalan.R;

public class DetailBroadcastActivity extends AppCompatActivity {

    private TextView txtjudul,txtisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_broadcast);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });fab.hide();

        txtisi = (TextView)findViewById(R.id.txtIsi);
        txtjudul = (TextView)findViewById(R.id.txtJudul);

        if(getIntent().getStringExtra("message") != null){
            txtisi.setText(getIntent().getStringExtra("message"));
            txtjudul.setText(getIntent().getStringExtra("judul"));

        }
    }

}
