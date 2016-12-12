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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import example.com.appkondisijalan.R;
import example.com.appkondisijalan.helper.Config;
import example.com.appkondisijalan.helper.PrefManager;

public class SignUpActivity extends AppCompatActivity {
    private EditText inputUsername, inputPassword, inputNama, inputAlamat, inputNoTelp;
    private Button btnSubmit, btnLogin;
    private PrefManager pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputUsername   = (EditText)findViewById(R.id.inputUsername);
        inputPassword   = (EditText)findViewById(R.id.inputPassword);
        inputNama       = (EditText)findViewById(R.id.inputNama);
        inputAlamat     = (EditText)findViewById(R.id.inputAlamat);
        inputNoTelp     = (EditText)findViewById(R.id.inputNoTelp);
        btnSubmit       = (Button) findViewById(R.id.btnSubmit);
        btnLogin        = (Button) findViewById(R.id.btnLogin);
        pref            = new PrefManager(getApplicationContext());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validasiForm()) {
                    String username = inputUsername.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();
                    String nama     = inputNama.getText().toString().trim();
                    String alamat   = inputAlamat.getText().toString().trim();
                    String no_telp  = inputNoTelp.getText().toString().trim();
                    daftaAkun(username, password, nama, alamat, no_telp);
                }

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private static boolean isValidPassword(String password) {
        String regEx = "^(?=.*?\\d)(?=.*?[a-zA-Z])[a-zA-Z\\d]+$";
        return password.matches(regEx);
    }

    private boolean validasiForm(){
        boolean cek = true;
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String nama     = inputNama.getText().toString().trim();
        String alamat   = inputAlamat.getText().toString().trim();
        String no_telp  = inputNoTelp.getText().toString().trim();

        if (username.length() == 0 || password.length() == 0 || nama.length() == 0 || alamat.length() == 0 || no_telp.length() == 0){
            Toast.makeText(getApplicationContext(), "Harap lengkapi form pendaftaran!", Toast.LENGTH_SHORT).show();
            cek = false;
        }
        if(username.contains("petugas_")){
            inputUsername.setError("Username sudah digunakan");
            cek = false;
        }
        if (!isValidPassword(password) && password.length() >= 8){
            cek = false;
            inputPassword.setError("Password harus kombinasi angka dan huruf, minimal 8 digit!");
        }

        return cek;
    }

    private void daftaAkun(final String username, final  String password, final String nama, final  String alamat, final String no_telp) {
        final ProgressDialog loading = ProgressDialog.show(SignUpActivity.this, "", "Mendaftarkan Akun...", false, false);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_SIGNUP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseObj = new JSONObject(response);

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        pref.createLoginSession(username);
                        Intent intent = new Intent(getApplicationContext(), LaporanActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

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
             *
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                params.put("nama", nama);
                params.put("alamat", alamat);
                params.put("no_telp", no_telp);

                Log.i("Posting params: ", params.toString());

                return params;
            }

        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(
                15000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}
