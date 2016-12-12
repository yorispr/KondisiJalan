package example.com.appkondisijalan.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import example.com.appkondisijalan.helper.Config;
import example.com.appkondisijalan.helper.PrefManager;
import example.com.appkondisijalan.R;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getSimpleName();
    private EditText inputUsername, inputPassword;
    private Button btnMasuk, btnDaftar;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Masuk Akun");
        setContentView(R.layout.activity_login);

        pref = new PrefManager(this);
        btnMasuk = (Button) findViewById(R.id.btnMasuk);
        btnDaftar = (Button) findViewById(R.id.btnDaftar);
        inputUsername = (EditText) findViewById(R.id.inputUsername);
        inputPassword = (EditText) findViewById(R.id.inputPassword);

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                if (!username.isEmpty() && !password.isEmpty()){
                    login(username, password);
                }if (username.isEmpty()){
                    inputUsername.setError("Harap isi bidang ini.");
                }if (password.isEmpty()){
                    inputPassword.setError("Harap isi bidang ini.");
                }
            }
        });
    }

    private void login(final String username, final String password){
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "" , "Memuat...", false, false);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseObj = new JSONObject(response);

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {
                        loading.dismiss();
                        pref.createLoginSession(username);
                        pref.setUserID(username);

                        if(username.contains("petugas_")) {
                            FirebaseMessaging.getInstance().subscribeToTopic("petugas");
                        }
                        Intent intent = new Intent(getApplicationContext(), LaporanActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Error: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                    // hiding the progress bar
                    //progressBar.setVisibility(View.GONE);
                    loading.dismiss();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    loading.dismiss();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
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
                params.put("username", username);
                params.put("password", password);

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

    @Override
    protected void onResume() {
        super.onResume();
        //If we will get true
        if(pref.isLoggedIn()){
            Intent intent = new Intent(getApplicationContext(), LaporanActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(intent);
            finish();
        }
    }
}
