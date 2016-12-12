package example.com.appkondisijalan.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import example.com.appkondisijalan.R;
import example.com.appkondisijalan.helper.Config;
import example.com.appkondisijalan.helper.GPSTracker;
import example.com.appkondisijalan.helper.PrefManager;

public class TambahLaporanActivity extends AppCompatActivity {
    private static String TAG = TambahLaporanActivity.class.getSimpleName();
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
    private CircleImageView picture;
    private EditText inputLokasi;
    private TextView txtLatitude, txtLongitude;
    private Button btnLapor;
    private PrefManager pref;
    private Bitmap bitmap;
    GPSTracker gps;
    double latitude, longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_laporan);

        pref = new PrefManager(this);
        inputLokasi = (EditText) findViewById(R.id.inputLokasi);
        picture = (CircleImageView) findViewById(R.id.choosePicture);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        btnLapor = (Button) findViewById(R.id.btnLapor);



        ActivityCompat.requestPermissions(TambahLaporanActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);


        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        btnLapor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> user = pref.getUserDetails();
                if(bitmap == null){
                    Toast.makeText(getApplicationContext(), "Foto jalan harus diisi!", Toast.LENGTH_SHORT).show();
                }else {
                    String image = getStringImage(bitmap);
                    String username = user.get(PrefManager.KEY_ID);
                    String lokasi = inputLokasi.getText().toString().trim();
                    String latitude = txtLatitude.getText().toString().trim();
                    String longitude = txtLongitude.getText().toString().trim();
                    laporKondisi(username, lokasi, latitude, longitude, image);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLokasi();
                    myLocation();
                } else {
                    Toast.makeText(TambahLaporanActivity.this, "Akses ke perangkat ditolak", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void captureImage() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }  catch(NullPointerException e){e.printStackTrace();}

}

    private Uri getOutputMediaFileUri(int type) {
            return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Gagal membuat "
                        + IMAGE_DIRECTORY_NAME + " directori");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "Anda membatalkan pengambilan foto.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Maaf! Pengambilan foto gagal.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewCapturedImage() {
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
            picture.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void  myLocation(){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();

            String location = address + " - " + city + " - " + state;
            inputLokasi.setText(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void getLokasi(){
        gps = new GPSTracker(TambahLaporanActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            txtLatitude.setText(String.valueOf(latitude));
            txtLongitude.setText(String.valueOf(longitude));

        }else{
            gps.showSettingsAlert();
        }
    }

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void laporKondisi(final String username, final String lokasi, final String latitude, final String longitude, final String foto){

        final ProgressDialog loading = ProgressDialog.show(TambahLaporanActivity.this, "" , "Unggah laporan...", false, false);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_LAPOR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {
                        Intent intent = new Intent(getApplicationContext(), LaporanActivity.class);
                        startActivity(intent);
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Error: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                    loading.dismiss();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    loading.dismiss();
                }
                catch (NullPointerException e) {
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
            @Override
            protected Map<String, String> getParams() {
                String id_laporan = String.valueOf(System.currentTimeMillis());

                Map<String, String> params = new HashMap<String, String>();
                if(pref.isLoggedIn()) {
                    params.put("username", pref.GetUserID());
                }
                else {
                    params.put("username", GetDeviceID());
                }
                params.put("lokasi", lokasi);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("foto", foto);
                params.put("id_laporan", id_laporan);

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

    public String GetDeviceID() {

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LaporanActivity.class);
        startActivity(intent);
    }
}
