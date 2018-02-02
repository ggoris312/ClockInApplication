package com.supercooler.www.clockin_url_application;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.supercooler.www.clockin_url_application.barcode.BarcodeCaptureActivity;

public class MainActivity extends AppCompatActivity {

/***********************************************************/
/*                                                         */
/*                                                         */
/*             Global Variables                            */
/*                                                         */
/*                                                         */
/***********************************************************/

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private static final int MY_STORAGE_REQUEST_CODE = 100;

    private TextView mResultTextView;

    private String barcodeResult = null;
    private String serverUrl;

    Button sendBarCodeButton;

/***********************************************************/
/*                                                         */
/*                                                         */
/*             Other Functions                             */
/*                                                         */
/*                                                         */
/***********************************************************/

//Check to see if the android device has internet.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getServerUrl() {
        String url = "";
        String ipString = SaveSharedPreference.getStringPreference(
                getApplicationContext(), SaveSharedPreference.IP_STRING);
        String portString = SaveSharedPreference.getStringPreference(
                getApplicationContext(), SaveSharedPreference.PORT_STRING);
        if (!ipString.isEmpty() && !portString.isEmpty()) {
            url = "http://" + ipString + ":" + portString;
        }else{
            url = "";
            Toast.makeText(this, "There is no url stored", Toast.LENGTH_SHORT).show();
        }

        return url;

    }

/***********************************************************/
/*                                                         */
/*                                                         */
/*             Android Start create Activity.              */
/*                                                         */
/*                                                         */
/***********************************************************/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
            Permission Check.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_STORAGE_REQUEST_CODE);
        }

        /*
            make sure to get serverusl
         */
        serverUrl = getServerUrl();

        /*
            Bind components to xml view.
         */

        mResultTextView = (TextView) findViewById(R.id.result_textview);

        //Initialize The barcode reader on button press.
        Button scanBarcodeButton = (Button) findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });

        //configuration button to start the url to store to preferences.
        Button serverButton = (Button) findViewById(R.id.server_configuration_button);
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                ServerConfigurationDialog newFragment = ServerConfigurationDialog.newInstance();// call the static method
                newFragment.show(manager, "dialog");
            }
        });

        //send the code to the server.
        sendBarCodeButton = (Button) findViewById(R.id.send_barcode_button);
        sendBarCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    if (!barcodeResult.isEmpty()) {
                        JsonObject json = new JsonObject();
                        json.addProperty("employee", barcodeResult);
                        serverUrl = getServerUrl();
                        if(!serverUrl.isEmpty()){
                            String url = serverUrl + "/api/employee/latestDocument";
                            Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();
                            Ion.with(MainActivity.this)
                                    .load(url)
                                    .setJsonObjectBody(json)
                                    .asJsonArray()
                                    .setCallback(new FutureCallback<JsonArray>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonArray result) {
                                            if(e != null){
                                                Toast.makeText(MainActivity.this, "Error loading tweets", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            if (result.toString().isEmpty()) {
                                                    Toast.makeText(MainActivity.this, "no response", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "We got Response", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, ClockActivity.class);
                                                intent.putExtra("message", result.toString());
                                                intent.putExtra("employee", barcodeResult);
                                                sendBarCodeButton.setVisibility(View.INVISIBLE);
                                                barcodeResult = null;
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(MainActivity.this, "There is no ip saved", Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        Toast.makeText(MainActivity.this, "There is no barcode number to send.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "No network Connection available.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

/***********************************************************/
/*                                                         */
/*                                                         */
/*             Android on Resume                           */
/*                                                         */
/*                                                         */
/***********************************************************/
    @Override
    protected void onResume() {
        super.onResume();

    }

/***********************************************************/
/*                                                         */
/*                                                         */
/*             Android on Activity Result                  */
/*                                                         */
/*                                                         */
/***********************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    barcodeResult = barcode.displayValue;
                    mResultTextView.setText(barcode.displayValue);
                    sendBarCodeButton.setVisibility(View.VISIBLE);
                } else mResultTextView.setText(R.string.no_barcode_captured);
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "write permission granted", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this, "write permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }
}
