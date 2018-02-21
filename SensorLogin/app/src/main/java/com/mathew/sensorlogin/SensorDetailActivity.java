package com.mathew.sensorlogin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

    private TextView sensorIDText, nameText, tempText, errorText;
    private String authToken, sensorID;
    private int currentMinutes;
    private final String base_url = "https://www.imonnit.com/json/";
    boolean alert = false;
    private ProgressDialog prgDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sensor_detail);
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        sensorIDText = findViewById(R.id.sensorID);
        nameText = findViewById(R.id.sensorName);
        tempText = findViewById(R.id.tempField);
        errorText = findViewById(R.id.error);
        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
            alert = extras.getBoolean("alert");
            getSupportActionBar().setTitle("SENSOR " + sensorID);
        }

        displayThisSensor(sensorID);
    }

    public void onResume()
    {
        super.onResume();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        setContentView(R.layout.activity_sensor_detail);
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        sensorIDText = findViewById(R.id.sensorID);
        nameText = findViewById(R.id.sensorName);
        tempText = findViewById(R.id.tempField);
        errorText = findViewById(R.id.error);
        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
            alert = extras.getBoolean("alert");
            getSupportActionBar().setTitle("SENSOR " + sensorID);
        }

        displayThisSensor(sensorID);
    }


    /**
     * method to display a single sensor
     *
     * @param _sensorID
     */
    public void displayThisSensor(final String _sensorID) {
        prgDialog.show();
        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params
        RequestParams params = new RequestParams();
        params.put("sensorID", _sensorID);
        client.get(base_url + "SensorGetExtended/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    prgDialog.hide();
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONObject result = obj.getJSONObject("Result");
                    String currentReading = result.getString("CurrentReading");
                    String name = result.getString("SensorName");
                    String lastCommunicationDate = result.getString("LastCommunicationDate");
                    long time = Long.parseLong(lastCommunicationDate.substring(6, lastCommunicationDate.length() - 2));
                    Date date = new Date(time);
                    String myDate = new SimpleDateFormat("yyyy/MM/dd  HH:mm").format(date);
                    String shours = new SimpleDateFormat("HH").format(date);
                    String sMinutes = new SimpleDateFormat("mm").format(date);



                    if(alert) {
                        sensorIDText.setText( sensorID);
                        nameText.setText( name);
                        tempText.setText(currentReading);
                        tempText.setTextColor(Color.GRAY);
                        sensorIDText.setTextColor(Color.GRAY);
                        nameText.setTextColor(Color.GRAY);
                        int color = Color.parseColor("#C4A69F");
                        tempText.setBackgroundColor(color);
                        nameText.setBackgroundColor(color);
                        sensorIDText.setBackgroundColor(color);
                        errorText.setText("No communication\nLast communication: " + myDate);
                        errorText.setTextColor(Color.RED);
                        errorText.setVisibility(TextView.VISIBLE);
                    }else
                    {
                        nameText.setText( name);
                        tempText.setText(currentReading);
                        sensorIDText.setText( sensorID);
                        sensorIDText.setTextColor(Color.BLACK);
                        sensorIDText.setBackgroundColor(Color.WHITE);
                        nameText.setTextColor(Color.BLACK);
                        nameText.setBackgroundColor(Color.WHITE);
                        tempText.setTextColor(Color.BLACK);
                        tempText.setBackgroundColor(Color.WHITE);
                        errorText.setVisibility(TextView.INVISIBLE);
                    }


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Throwable error, String content) {
                prgDialog.hide();
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
        prgDialog.hide();
    }


    public void startHistoryActivity(View view) {

        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("sensorID", sensorID);
        prgDialog.dismiss();
        startActivity(intent);

    }

    public void startGraphActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("sensorID", sensorID);
        prgDialog.cancel();
        startActivity(intent);
    }

    public void startEditActivity(View view) {

        Intent intent = new Intent(getApplicationContext(), EditSensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("sensorID", sensorID);
        prgDialog.dismiss();
        //finish();
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                prgDialog.cancel();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();

    }
}
