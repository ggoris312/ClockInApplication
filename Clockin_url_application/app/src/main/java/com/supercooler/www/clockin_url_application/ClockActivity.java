package com.supercooler.www.clockin_url_application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by gio on 10/13/2017.
 */

public class ClockActivity extends AppCompatActivity {

    TextView mClockedInText;
    TextView mLastTimerClock;
    Button mResponseButton;

    JSONArray jsonArray;

    private String id = "";

    private String name = "";
    private String position = "";
    private String supervisor = "";
    private String department = "";
    private long identification = 0;
    private Date start = new Date();
    private Date end = new Date();
    private int pto = 0;
    private int vacation = 0;
    private boolean clocked = false;

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
            finish();
        }

        return url;

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        mClockedInText = (TextView) findViewById(R.id.clockin_state);
        mLastTimerClock = (TextView) findViewById(R.id.last_timer);
        mResponseButton = (Button) findViewById(R.id.clock_button);
        mResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonObject json = (clocked) ? updateDocument() : prepareNeDocument();
                String serverUrl = getServerUrl();
                String extension = (clocked) ? "/api/employee/updatedocument" : "/api/employee/storedocument";
                String url = serverUrl + extension;
                Toast.makeText(ClockActivity.this, url, Toast.LENGTH_SHORT).show();
                Ion.with(ClockActivity.this)
                        .load(url)
                        .setJsonObjectBody(json)
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> result) {
                                if(result.getHeaders().code() == 200);
                                finish();
                            }
                        });
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String message = bundle.getString("message");
            String employee = bundle.getString("employee");
            jsonArray = parseJson(message);
            if(jsonArray.length() <= 0) {
                setUpInitialClock(employee);
            }else {
                setUpClock(jsonArray);
            }
        }else{
            finish();
        }
    }


    JSONArray parseJson(String message){
        try {
            return new JSONArray(message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    void setUpInitialClock(String employee){
        JsonObject json = new JsonObject();
        json.addProperty("employee", employee);
        String serverUrl = getServerUrl();
        String extension = "/api/user/information";
        String url = serverUrl + extension;
        Toast.makeText(ClockActivity.this, url, Toast.LENGTH_SHORT).show();
        Ion.with(ClockActivity.this)
                .load(url)
                .setJsonObjectBody(json)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null){
                            Toast.makeText(ClockActivity.this, "Error loading tweets", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (result.toString().isEmpty()) {
                            Toast.makeText(ClockActivity.this, "no response", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClockActivity.this, "We got Response", Toast.LENGTH_SHORT).show();
                            jsonArray = parseJson(result.toString());
                            setUpFirstTimeClock(jsonArray);
                        }
                    }
                });

    }

    private void setUpFirstTimeClock(JSONArray mJsonArray) {
        try {
            JSONObject mJsonObject = mJsonArray.getJSONObject(0);
            Log.v("clockActivity", mJsonObject.toString());
            Log.v("clockActivity", "" + mJsonObject.getInt("identification"));
            id = mJsonObject.getString("_id");
            name = mJsonObject.getString("name");
            position = mJsonObject.getString("position");
            supervisor = mJsonObject.getString("supervisor");
            department = mJsonObject.getString("department");
            identification = mJsonObject.getLong("identification");
            DateTime dt = new DateTime(new Date());
            start = dt.toDate();
            dt = new DateTime(new Date() );
            end = dt.toDate();
            pto = 0;
            vacation = 0;
            clocked = false;
            prepareTextViews();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    void setUpClock(JSONArray mJsonArray){
        try {
            JSONObject mJsonObject = mJsonArray.getJSONObject(0);
            id = mJsonObject.getString("_id");
            name = mJsonObject.getString("name");
            position = mJsonObject.getString("position");
            supervisor = mJsonObject.getString("supervisor");
            department = mJsonObject.getString("department");
            identification = mJsonObject.getLong("identification");
            DateTime dt = new DateTime( mJsonObject.getString("start"));
            start = dt.toDate();
            dt = new DateTime( mJsonObject.getString("end"));
            end = dt.toDate();
            pto = mJsonObject.getInt("pto");
            vacation = mJsonObject.getInt("vacation");
            clocked = mJsonObject.getBoolean("clocked");
            prepareTextViews();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void prepareTextViews(){
        Log.v("Clock Activity", "" + identification);
        if(clocked){
            mClockedInText.setText("clocked in at: ");
            mLastTimerClock.setText(start.toString());
            mResponseButton.setText("Clock Out");
        }else{
            mClockedInText.setText("clocked out at: ");
            mLastTimerClock.setText(end.toString());
            mResponseButton.setText("Clock In");
        }
        mResponseButton.setVisibility(View.VISIBLE);
    }

    private JsonObject prepareNeDocument(){
        JsonObject mJsonObject = new JsonObject();
        mJsonObject.addProperty("name", name);
        mJsonObject.addProperty("position", position);
        mJsonObject.addProperty("supervisor", supervisor);
        mJsonObject.addProperty("department", department);
        mJsonObject.addProperty("identification", identification);
        mJsonObject.addProperty("start", new DateTime().toString());
        mJsonObject.addProperty("end", end.toString());
        mJsonObject.addProperty("pto", 0);
        mJsonObject.addProperty("vacation", 0);
        mJsonObject.addProperty("clocked", !clocked);
        return mJsonObject;
    }

    private JsonObject updateDocument(){
        JsonObject mJsonObject = new JsonObject();
        mJsonObject.addProperty("id", id);
        mJsonObject.addProperty("name", name);
        mJsonObject.addProperty("position", position);
        mJsonObject.addProperty("supervisor", supervisor);
        mJsonObject.addProperty("department", department);
        mJsonObject.addProperty("identification", identification);
        mJsonObject.addProperty("start", start.toString());
        mJsonObject.addProperty("end", new DateTime().toString());
        mJsonObject.addProperty("pto", pto);
        mJsonObject.addProperty("vacation", vacation);
        mJsonObject.addProperty("clocked", !clocked);
        return mJsonObject;
    }

}
