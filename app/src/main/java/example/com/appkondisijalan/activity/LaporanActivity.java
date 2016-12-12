package example.com.appkondisijalan.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import example.com.appkondisijalan.R;
import example.com.appkondisijalan.adapter.KondisiJalanAdapter;
import example.com.appkondisijalan.helper.Config;
import example.com.appkondisijalan.helper.PrefManager;
import example.com.appkondisijalan.model.KondisiJalan;

public class LaporanActivity extends AppCompatActivity {
    private static String TAG = LaporanActivity.class.getSimpleName();
    private List<KondisiJalan> kondisiJalanList = new ArrayList<KondisiJalan>();
    private ListView listView;
    private KondisiJalanAdapter adapter;
    private PrefManager pref;
    ProgressBar progbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        pref = new PrefManager(this);
        listView = (ListView) findViewById(R.id.listKondisiJalan);
        adapter = new KondisiJalanAdapter(this, kondisiJalanList);
        progbar = (ProgressBar)findViewById(R.id.progressBar2) ;
        listView.setAdapter(adapter);

        if(pref.isLoggedIn()){
            if(pref.GetUserID().contains("petugas_")){
                GetAllLaporan();
            }else{
                GetLaporanByID();
            }
        }else{
            GetLaporanByID();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(LaporanActivity.this,DetailLaporanActivity.class);
                i.putExtra("id_laporan",kondisiJalanList.get(position).getId_laporan());
                i.putExtra("pengirim",kondisiJalanList.get(position).getPengunggah());

                Log.d("idlap",kondisiJalanList.get(position).getId_laporan());
                startActivity(i);
            }
        });
        Log.d(TAG, "Device ID  : " +GetDeviceID());
        FirebaseMessaging.getInstance().subscribeToTopic("global");
    }

    private void GetLaporanByID(){
        progbar.setVisibility(View.VISIBLE);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_KONDISI_JALAN_BY_ID, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {

                Log.d("Response",response);
                if (response.toString().equalsIgnoreCase("[]")) {
                    Toast.makeText(getApplicationContext(), "Tidak ada daftar laporan!", Toast.LENGTH_LONG).show();

                    String arr[] = {"Kosong Njing"};
                    ArrayAdapter<String> adapterkosong = new ArrayAdapter<String>(LaporanActivity.this,R.layout.list_kosong_layout,arr);
                    listView.setAdapter(adapterkosong)
                    ;
                    progbar.setVisibility(View.GONE);
                }
                // Parsing json
                try {
                    JSONArray jArr = new JSONArray(response);
                    for(int i=0;i<jArr.length();i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        KondisiJalan kondisiJalan = new KondisiJalan();
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
                        kondisiJalan.setId_laporan(obj.getString("id_laporan"));

                        kondisiJalanList.add(kondisiJalan);
                    }
                    progbar.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    progbar.setVisibility(View.GONE);
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Gagal koneksi server, coba lagi!", Toast.LENGTH_LONG).show();
                progbar.setVisibility(View.GONE);

                String arr[] = {"Koneksi Error"};
                ArrayAdapter<String> adapterkosong = new ArrayAdapter<String>(LaporanActivity.this,R.layout.list_kosong_layout,arr);
                listView.setAdapter(adapterkosong);

            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if(pref.isLoggedIn()) {
                    params.put("username", pref.GetUserID());
                }
                else {
                    params.put("username", GetDeviceID());
                }
                Log.e(TAG, "Posting params: " + params.toString());

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
    private void GetAllLaporan(){
        progbar.setVisibility(View.VISIBLE);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_ALL_KONDISI_JALAN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response.toString().equalsIgnoreCase("[]")) {
                    Toast.makeText(getApplicationContext(), "Tidak ada daftar laporan!", Toast.LENGTH_LONG).show();

                    String arr[] = {"Kosong Njing"};
                    ArrayAdapter<String> adapterkosong = new ArrayAdapter<String>(LaporanActivity.this,R.layout.list_kosong_layout,arr);
                    listView.setAdapter(adapterkosong)
                    ;
                    progbar.setVisibility(View.GONE);
                }
                // Parsing json
                try {
                    JSONArray jArr = new JSONArray(response);
                    for(int i=0;i<jArr.length();i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        KondisiJalan kondisiJalan = new KondisiJalan();
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
                        kondisiJalan.setId_laporan(obj.getString("id_laporan"));

                        kondisiJalanList.add(kondisiJalan);
                    }
                    progbar.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    progbar.setVisibility(View.GONE);
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Gagal koneksi server, coba lagi!", Toast.LENGTH_LONG).show();
                progbar.setVisibility(View.GONE);

                String arr[] = {"Koneksi Error"};
                ArrayAdapter<String> adapterkosong = new ArrayAdapter<String>(LaporanActivity.this,R.layout.list_kosong_layout,arr);
                listView.setAdapter(adapterkosong);

            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */


        };

        strReq.setRetryPolicy( new DefaultRetryPolicy(
                15000 , 0,
                DefaultRetryPolicy . DEFAULT_BACKOFF_MULT ));

        // Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
    }


    public String GetDeviceID() {

         String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;

    }

    private void keluar() {
        AlertDialog.Builder dialogExit = new AlertDialog.Builder(this);
        dialogExit.setTitle("Konfirmasi");
        dialogExit
                .setMessage("Apakah anda yakin keluar dari akun?")
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int d) {
                        dialog.cancel();
                        pref = new PrefManager(getApplicationContext());
                        pref.logoutUser();
                        finish();
                    }
                })
                .setNegativeButton("Tidak",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_utama, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuLogout) {
            if(pref.GetUserID().contains("petugas_")) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("petugas");
            }
            keluar();
        }
        if (id == R.id.menuAdd) {
            Intent intent = new Intent(getApplicationContext(), TambahLaporanActivity.class);
            startActivity(intent);
        }

        if (id == R.id.menuLogin) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        //startActivity(intent);
    }
}
