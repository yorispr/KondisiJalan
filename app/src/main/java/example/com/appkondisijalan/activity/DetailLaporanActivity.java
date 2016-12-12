package example.com.appkondisijalan.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import example.com.appkondisijalan.R;
import example.com.appkondisijalan.helper.Config;
import example.com.appkondisijalan.helper.PrefManager;
import example.com.appkondisijalan.model.KondisiJalan;

public class DetailLaporanActivity extends AppCompatActivity {
    String id_laporan;
    String nama;

    ImageView img_jalan;
    private TextView pengirim, lokasi, tanggal;
    private Button btnMap;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_laporan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = new PrefManager(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });fab.hide();
        img_jalan = (ImageView)findViewById(R.id.img_jalan);
        pengirim = (TextView)findViewById(R.id.txtPengirim);
        lokasi = (TextView)findViewById(R.id.txtLokasi);
        tanggal = (TextView)findViewById(R.id.txtTanggal);

        btnMap = (Button)findViewById(R.id.btnPeta);


        if(getIntent().getExtras() != null){
            id_laporan = getIntent().getStringExtra("id_laporan");
            nama = getIntent().getStringExtra("pengirim");

            Log.d("idlaporan",id_laporan);
            GetLaporanByID();
        }
    }

    private void GetLaporanByID(){

        String url;
        if(!nama.equals("Masyarakat")){
            url = Config.URL_KONDISI_JALAN_BY_ID_LAPORAN_PENGGUNA;
        }else{
            url = Config.URL_KONDISI_JALAN_BY_ID_LAPORAN_GUEST;
        }

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                if (response.toString().equalsIgnoreCase("[]")) {
                    Toast.makeText(getApplicationContext(), "Tidak ada daftar laporan!", Toast.LENGTH_LONG).show();
                }
                // Parsing json
                try {
                    JSONArray jArr = new JSONArray(response);
                    for(int i=0;i<jArr.length();i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        final KondisiJalan kondisiJalan = new KondisiJalan();
                        kondisiJalan.setLokasi(obj.getString("lokasi"));
                        kondisiJalan.setLatitude(obj.getString("latitude"));
                        kondisiJalan.setLongitude(obj.getString("longitude"));
                        kondisiJalan.setFoto(obj.getString("foto"));
                        if(obj.isNull("nama")){
                            kondisiJalan.setPengunggah("Masyarakat");
                        }else{
                            kondisiJalan.setPengunggah(obj.getString("nama"));
                        }
                        kondisiJalan.setTgl_unggah(obj.getString("create_at"));
                        kondisiJalan.setStatus(obj.getString("status"));

                        Glide.with(DetailLaporanActivity.this).load(kondisiJalan.getFoto()).into(img_jalan);
                        pengirim.setText(kondisiJalan.getPengunggah());
                        lokasi.setText(kondisiJalan.getLokasi());
                        tanggal.setText(kondisiJalan.getTgl_unggah());
                        btnMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f", Double.parseDouble(kondisiJalan.getLatitude()), Double.parseDouble(kondisiJalan.getLongitude()),Double.parseDouble(kondisiJalan.getLatitude()), Double.parseDouble(kondisiJalan.getLongitude()));

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(intent);
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Gagal koneksi server, coba lagi!", Toast.LENGTH_LONG).show();


            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_laporan", id_laporan);

                Log.e("DetailLaporanActivity", "Posting params: " + params.toString());

                return params;
            }

        };

        strReq.setRetryPolicy( new DefaultRetryPolicy(
                15000 , 0,
                DefaultRetryPolicy . DEFAULT_BACKOFF_MULT ));

        // Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
    }


}
