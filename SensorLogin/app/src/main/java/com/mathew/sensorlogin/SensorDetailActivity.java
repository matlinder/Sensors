package com.mathew.sensorlogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SensorDetailActivity extends AppCompatActivity {

    private TextView sensorIDText, nameText, tempText;
    private String authToken, sensorID;
    private int currentMinutes;
    private final String base_url = "https://www.imonnit.com/json/";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sensor_detail);

        sensorIDText = findViewById(R.id.sensorID);
        nameText = findViewById(R.id.sensorName);
        tempText = findViewById(R.id.tempField);
        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
            getSupportActionBar().setTitle("SENSOR " + sensorID);
        }

        displayThisSensor(sensorID);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * method to display a single sensor
     *
     * @param _sensorID
     */
    public void displayThisSensor(final String _sensorID) {
        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params
        RequestParams params = new RequestParams();
        params.put("sensorID", _sensorID);
        client.get(base_url + "SensorGetExtended/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONObject result = obj.getJSONObject("Result");
                    String currentReading = result.getString("CurrentReading");
                    String name = result.getString("SensorName");
                    String batteryLevel = result.getString("BatteryLevel");
                    String signalStrength = result.getString("SignalStrength");
                    String lastCommunicationDate = result.getString("LastCommunicationDate");
                    long time = Long.parseLong(lastCommunicationDate.substring(6, lastCommunicationDate.length() - 2));
                    Date date = new Date(time);
                    String myDate = new SimpleDateFormat("yyyy/MM/dd").format(date);
                    String inactivityAlert = result.getString("InactivityAlert");
                    String shours = new SimpleDateFormat("HH").format(date);
                    String sMinutes = new SimpleDateFormat("mm").format(date);
                    int sensorTotalMinutes = (Integer.parseInt(shours) * 60) + Integer.parseInt(sMinutes);
                    boolean alert = false;
                    if((currentMinutes - sensorTotalMinutes) >= Integer.parseInt(inactivityAlert))
                    {
                        alert = true;
                    }
                    sensorIDText.setText( sensorID);
                    nameText.setText( name);
                    tempText.setText(currentReading);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Throwable error, String content) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void startHistoryActivity(View view) {

        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("sensorID", sensorID);

        startActivity(intent);

    }

    public void startGraphActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("sensorID", sensorID);

        startActivity(intent);
    }
}
