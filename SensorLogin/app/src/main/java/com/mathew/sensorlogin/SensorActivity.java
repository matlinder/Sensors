package com.mathew.sensorlogin;


import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SensorActivity extends AppCompatActivity {
    String authToken, userID, networkID, gatewayID, today, yesterday;
    TextView networkData, gatewayData, sensorData; //views to display the various data
    final String base_url = "https://www.imonnit.com/json/";
    private HashMap<String, String> sensorMap = new HashMap<String, String>();
    private static HashMap<String, String> mainSensorMap = new HashMap<String, String>();
    private ArrayList<String>  listOfSensors = new ArrayList<String>();
    // Progress Dialog Object
    ProgressDialog prgDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userID = extras.getString("userID");
        }
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);



        //grab the components from the view
        networkData = findViewById(R.id.networkData);
        gatewayData = findViewById(R.id.gatewayData);
        sensorData = findViewById(R.id.sensorData);
        createDates();
        //call methods to display the data
        //certain values will be hardcode for testing purposes, remove later
        //displayNetworkData();
        networkData.setText("using the network 33466");
        networkID = "33466";
        //display networks with this network id
        //displayGatewayData();
        gatewayData.setText("using the gateway 200329");
        gatewayID = "200329";
        sensorMap.put("asadf", "testesttestst");
        sensorMap.put("asdfasd","sdfas");
        //displaySensorData();
        Toast.makeText(getApplicationContext(), "after display data map size " + sensorMap.size(), Toast.LENGTH_LONG).show();

    }

    /**
     * display the network data of the associated account
     * user to select which network to display gateways from
     */
    public void displayNetworkData()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "NetworkList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try{
                    JSONObject obj = new JSONObject(response);
                    networkData.setText(obj.toString());
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured could not process data!", Toast.LENGTH_LONG).show();
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

    /**
     * Displays a list of gateways for the user to choose
     */
    public void displayGatewayData()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        //grab the list of gateways
        //client.get(base_url + "GatewayList/" + authToken, new AsyncHttpResponseHandler() {

        //grab a specific gateway to display
        RequestParams params = new RequestParams();
        params.put("networkID",networkID);
        client.get(base_url + "GatewayList/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try{
                    JSONObject obj = new JSONObject(response);
                    gatewayData.setText(obj.toString());
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured could not process data!", Toast.LENGTH_LONG).show();
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

    /**
     * display the sensors with the associated gateway
     * will need to pass in the gatewayID or have it set in displaygateway data when it is clicked
     */
    public void displaySensorData()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "SensorList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try{
                    JSONObject obj = new JSONObject(response);
                    JSONArray sensors = obj.getJSONArray("Result");

                    for(int i = 0; i < sensors.length(); i++)
                    {
                        JSONObject tempSensor = sensors.getJSONObject(i);
                        String sensorID = tempSensor.getString("SensorID");

                        //uses the sensorID to grab the specific gateway that it communicates with
                        //gateWayMapping(sensorID);
                        // throw all sensors in an array list
                        listOfSensors.add(sensorID);

                        //Toast.makeText(getApplicationContext(), "adding sensor to list " + listOfSensors, Toast.LENGTH_SHORT).show();


                    }

                    Toast.makeText(getApplicationContext(), "all sensors added to list", Toast.LENGTH_LONG).show();

//                     map the gateways to the sensors
                    Toast.makeText(getApplicationContext(), "going to run my list of sensors of size " + listOfSensors.size(), Toast.LENGTH_LONG).show();

                    for(int i = 0; i < listOfSensors.size(); i++)
                    {
                        if(listOfSensors.get(i) != null) {
                            //Toast.makeText(getApplicationContext(), "sending this sensor to gatewaymapping " + listOfSensors.get(i), Toast.LENGTH_LONG).show();

                            gateWayMapping(listOfSensors.get(i));

                        }
                    }
                    Toast.makeText(getApplicationContext(), "going to run my map of sensors of size " + sensorMap.size(), Toast.LENGTH_LONG).show();
                    displayGateway(gatewayID);
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occurred could not sensor data!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcureed! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });

        Toast.makeText(getApplicationContext(), "what is in the array", Toast.LENGTH_LONG).show();


        // display the specific gateway with its associated sensors
       // displayGateway(gatewayID);
    }

    /**
     * displays the sensors with the associated gateway
     * @param _gatewayID
     */
    private void displayGateway(String _gatewayID) {


        Iterator it = mainSensorMap.entrySet().iterator();
        //loop through all the sensors
        while(it.hasNext()) {
            prgDialog.show();
            //extract each the key value pair
            Map.Entry pair = (Map.Entry) it.next();
            // check if the pair matches our gateway
            if((pair != null) && pair.getValue().toString().equals(_gatewayID)) {
                // grab the sensor from the pair
                String sensorID = pair.getKey().toString();
                // create params
                RequestParams params = new RequestParams();
                params.put("sensorID", sensorID);

                // json stuff
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(base_url + "SensorGet/" + authToken, params, new AsyncHttpResponseHandler() {

                    public void onSuccess(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            sensorData.setText(obj.toString());

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Error Occurred could not sensor data!", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "Unexpected Error occcureed! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), "nothing more sensors to display", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void gateWayMapping(final String sensorID)
    {
        //Toast.makeText(getApplicationContext(), "inside gatewaymapping with sensor " + sensorID, Toast.LENGTH_LONG).show();

        RequestParams params = new RequestParams();
        params.put("sensorID", sensorID);
        params.put("fromDate", yesterday);
        params.put("toDate", today);

        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(base_url + "SensorDataMessages/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                sensorMap.put("before the try", "before the try");
                try
                {
                    prgDialog.hide();
                    JSONObject obj = new JSONObject(response);
                    JSONArray sensors = obj.getJSONArray("Result");
                    if(sensors.length() > 0 ) {


                        JSONObject tempSensor = sensors.getJSONObject(0);
                        String gatewayID = tempSensor.getString("GatewayID");
                        Toast.makeText(getApplicationContext(), sensorID + " " + gatewayID + " adding to map ", Toast.LENGTH_LONG).show();

                        sensorMap.put(sensorID, gatewayID);

                        //Toast.makeText(getApplicationContext(), "added sensor to map", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), "sensor map size " + sensorMap.size(), Toast.LENGTH_LONG).show();

                    }

                }

                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error occurred could not gateway data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                sensorMap.put("after the try", "after the try");
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

    public void createDates()
    {
        Calendar todaysDate = Calendar.getInstance();
        //System.out.println(todaysDate.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        today = df.format(todaysDate.getTime());

        Calendar yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DATE, -1);
        yesterday = df.format(yesterdayDate.getTime());
    }

    public void updateMap(HashMap<String, String> newSensorMap)
    {
        mainSensorMap = newSensorMap;
    }
}
