package com.mathew.sensorlogin;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class SensorActivity extends AppCompatActivity {
    private String authToken, userID, networkID, gatewayID, today, yesterday;
    private Spinner networkSpinner, gatewaySpinner, sensorData; //views to display the various data
    private TableLayout mainTable;
    private final String base_url = "https://www.imonnit.com/json/";
    private HashMap<String, String> sensorMap = new HashMap<String, String>();
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ProgressDialog prgDialog;
    private String sensorFileName = "sensor_file";
    private String gatewayMapFileName = "gateway_file";
    private int selectedIndex = -1;

    private String sensorFileContents, gatewayFileContents;
    private String[] listOfSensors;
    private String[] listOfPairs;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
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
//        networkSpinner = findViewById(R.id.NetworkData);
//        networkSpinner.setPrompt("Select your Network");
        //networkSpinner.setOnItemClickListener();
//        gatewaySpinner = findViewById(R.id.GatewayData);




        mainTable = findViewById(R.id.main_table);
        createTable();
        createDates();
        //call methods to display the data
        //certain values will be hardcode for testing purposes, remove later
        //displayNetworkData();
        //networkData.setText("using the network 33466");
        networkID = "33466";
        //display networks with this network id
        //displayGatewayData();
        //gatewaySpinner.setText("using the gateway 200329");
        gatewayID = "200329";

        pullAllSensorIDs();
        readSensorFile();
        gatewaySensorPairing();
        readGatewayMapFile();
        displayNetworkData();
        displayThisSensor("169541");
        Toast.makeText(getApplicationContext(), "testest ", Toast.LENGTH_LONG).show();
        //readGatewayMapFile();


    }

    /**
     * display the network data of the associated account
     * user to select which network to display gateways from
     */
    public void displayNetworkData() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "NetworkList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempNetwork = objArray.getJSONObject(i);
                        String name = tempNetwork.getString("NetworkName");
                        networkNames.add(name);

                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,networkNames);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    networkSpinner.setAdapter(dataAdapter);

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

    /**
     * Displays a list of gateways for the user to choose
     */
    public void displayGatewayData() {
        AsyncHttpClient client = new AsyncHttpClient();
        //grab the list of gateways
        //client.get(base_url + "GatewayList/" + authToken, new AsyncHttpResponseHandler() {

        //grab a specific gateway to display
        RequestParams params = new RequestParams();
        params.put("networkID", networkID);
        client.get(base_url + "GatewayList/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    //to be modified later
                    //will display all the individual gateways in selectable format
                    JSONObject obj = new JSONObject(response);

                } catch (JSONException e) {
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
     * grabs all the sensors and writes them to a file on the phones internal storage
     */
    public void pullAllSensorIDs() {
        //http client object and a request to the specific method
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "SensorList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    //grab the response and store in an object
                    JSONObject obj = new JSONObject(response);
                    //put the array of "Result" into a local array
                    JSONArray sensors = obj.getJSONArray("Result");

                    //open a file to write to on the phone's internal storage
                    //MODE_PRIVATE means that it will not be able to be read by anything but this
                    //application. THIS DOES NOT APPEND, creates new file when called
                    FileOutputStream outputStream = openFileOutput(sensorFileName, Context.MODE_PRIVATE);

                    //loops through the sensor array
                    for (int i = 0; i < sensors.length(); i++) {
                        //grabs the object
                        JSONObject tempSensor = sensors.getJSONObject(i);
                        //pulls the data from the tag "SensorID"
                        String sensorID = tempSensor.getString("SensorID");
                        //writes the sensorID to the file, adds a \n at the end for splitting format
                        outputStream.write(sensorID.getBytes());
                        outputStream.write("\n".getBytes());

                    }
                    //close the file
                    outputStream.close();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occurred could not sensor data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
    }


    /**
     * displays the sensors with the associated gateway
     *
     * @param _gatewayID
     */
    private void displayGateway(String _gatewayID) {

        //sensorData.setText("Size of the sensor map " + sensorMap.size());
        //Iterator it = sensorMap.entrySet().iterator();
        //loop through all the sensors


//        while(it.hasNext()) {
//            prgDialog.show();
//            //extract each the key value pair
//            Map.Entry pair = (Map.Entry) it.next();
//            // check if the pair matches our gateway
//            if((pair != null) && pair.getValue().toString().equals(_gatewayID)) {
//                // grab the sensor from the pair
//                String sensorID = pair.getKey().toString();
//                // create params
//                RequestParams params = new RequestParams();
//                params.put("sensorID", sensorID);
//
//                // json stuff
//                AsyncHttpClient client = new AsyncHttpClient();
//                client.get(base_url + "SensorGet/" + authToken, params, new AsyncHttpResponseHandler() {
//
//                    public void onSuccess(String response) {
//                        try {
//                            JSONObject obj = new JSONObject(response);
//                            sensorData.setText(obj.toString());
//
//                        } catch (JSONException e) {
//                            // TODO Auto-generated catch block
//                            Toast.makeText(getApplicationContext(), "Error Occurred could not sensor data!", Toast.LENGTH_LONG).show();
//                            e.printStackTrace();
//                        }
//                    }
//
//                    public void onFailure(int statusCode, Throwable error, String content) {
//
//                        // When Http response code is '404'
//                        if (statusCode == 404) {
//                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
//                        }
//                        // When Http response code is '500'
//                        else if (statusCode == 500) {
//                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
//                        }
//                        // When Http response code other than 404, 500
//                        else {
//                            Toast.makeText(getApplicationContext(), "Unexpected Error occcureed! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//                prgDialog.hide();
//                Toast.makeText(getApplicationContext(), "nothing more sensors to display", Toast.LENGTH_LONG).show();
//            }
//        }
    }

    /**
     * loop sesnsors and request data messages
     * pull the gateway ID and write it to a file with the sensor
     */
    public void gatewaySensorPairing() {
        //loop through the list of sensors
        if(listOfSensors != null) {
            for (int i = 0; i < listOfSensors.length; i++) {
                //call the method to get the associated gateway ID for each sensor ID
                gatewayPair(listOfSensors[i]);
            }
        }
    }

    /**
     * Method to get the gateway ID that the specified sensor is communicating with and
     * write the pairing to a file, to be processed later.
     *
     * @param _sensorID the specific sensor for the http request
     */
    public void gatewayPair(final String _sensorID) {
        //add the params to a RequestParams object
        //these will be used in the request
        RequestParams params = new RequestParams();
        params.put("sensorID", _sensorID);
        params.put("fromDate", yesterday);
        params.put("toDate", today);

        //create an http client object
        AsyncHttpClient client = new AsyncHttpClient();
        //make a request
        client.get(base_url + "SensorDataMessages/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {

                try {
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the array from tag "Result"
                    JSONArray sensors = obj.getJSONArray("Result");
                    //open a file with MODE_APPEND so that the previous content is not overwritten
                    FileOutputStream outputStream = openFileOutput(gatewayMapFileName, Context.MODE_APPEND);

                    //check if there is any data messages
                    if (sensors.length() > 0) {

                        JSONObject tempSensor = sensors.getJSONObject(0); //grab the first message
                        String tempGatewayID = tempSensor.getString("GatewayID"); //grab the gateway ID
                        //write the contents to a file, separated by - and end with \n
                        outputStream.write(_sensorID.getBytes());
                        outputStream.write("-".getBytes());
                        outputStream.write(tempGatewayID.getBytes());
                        outputStream.write("\n".getBytes());
                    } else {
                        //if there is no data messages, the sensor is not communicating with a gateway
                        outputStream.write(_sensorID.getBytes());
                        outputStream.write("-".getBytes());
                        outputStream.write("not active".getBytes());
                        outputStream.write("\n".getBytes());
                    }
                    outputStream.close();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error occurred could not gateway data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            public void onFailure(int statusCode, Throwable error, String content){

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
     * Method to create todays and yesterdays date in a format that can be used as params
     */
    public void createDates() {
        Calendar todaysDate = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        today = df.format(todaysDate.getTime());

        Calendar yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DATE, -1);
        yesterday = df.format(yesterdayDate.getTime());
    }

    /**
     * Read the sensor file stream and put its contents into a String builder
     * The contents will have a newline character after each item
     * call split method to split on newline character
     */
    public void readSensorFile() {
        try {
            //open a filestream to read the file
            FileInputStream sensorRead = openFileInput(sensorFileName);
            InputStreamReader isr = new InputStreamReader(sensorRead);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            //read the file line by line
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            //put the contents in string
            sensorFileContents = sb.toString();
            splitSensors();//split the contents

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Split the sensor file at the newline character, and store into an array
     */
    public void splitSensors() {
        listOfSensors = sensorFileContents.split("\\n");
    }

    /**
     * method to read the gateway file and put the file contents in a String array
     * String array will be in the format of "sensorID-gatewayID"
     * need to split it up and store into hashmap
     */
    public void readGatewayMapFile() {
        try {
            FileInputStream gatewayRead = openFileInput(gatewayMapFileName);
            InputStreamReader isr = new InputStreamReader(gatewayRead);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            gatewayFileContents = sb.toString();
            listOfPairs = gatewayFileContents.split("\\n");
            splitGateways();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gateways are currently in a String format of "sensorID-gatewayID"
     * split the two pieces of information, storing them in a key/value pair
     * using a hashmap
     */
    public void splitGateways() {
        if (listOfPairs != null) {
            for (int i = 0; i < listOfPairs.length; i++) {

                String[] parts = listOfPairs[i].split("-");
                if (parts != null) {
                    sensorMap.put(parts[0], parts[1]);
                }
            }
        }

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
        client.get(base_url + "SensorGet/" + authToken, params, new AsyncHttpResponseHandler() {

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
//                    sensorData.setText("Sensor ID: " + _sensorID
//                                        + "\nName: " + name
//                                        + "\nLast Communication Date: " + myDate
//                                        + "\nSignal Strength: " + signalStrength
//                                        + "\nBattery Level: " + batteryLevel
//                                        + "\nCurrent Reading: " + currentReading);
                    createSensorRow(_sensorID, name, myDate, signalStrength, batteryLevel, currentReading);


                    // testing sensors
//                    createSensorRow("75%", name, myDate, "75", "75", currentReading);
//                    createSensorRow("55%", name, myDate, "55", "55", currentReading);
//                    createSensorRow("30%", name, myDate, "30", "30", currentReading);
//                    createSensorRow("5%", name, myDate, "5", "12", currentReading);
//                    createSensorRow("no signal", name, myDate, "0", "0", currentReading);
//                    createSensorRow(_sensorID, name, myDate, "100", batteryLevel, currentReading);
//                    createSensorRow("75%", name, myDate, "75", "75", currentReading);
//                    createSensorRow("55%", name, myDate, "55", "55", currentReading);
//                    createSensorRow("30%", name, myDate, "30", "30", currentReading);
//                    createSensorRow("5%", name, myDate, "5", "12", currentReading);
//                    createSensorRow("no signal", name, myDate, "0", "0", currentReading);
//                    createSensorRow(_sensorID, name, myDate, "100", batteryLevel, currentReading);
//                    createSensorRow("75%", name, myDate, "75", "75", currentReading);
//                    createSensorRow("55%", name, myDate, "55", "55", currentReading);
//                    createSensorRow("30%", name, myDate, "30", "30", currentReading);
//                    createSensorRow("5%", name, myDate, "5", "12", currentReading);
//                    createSensorRow("no signal", name, myDate, "0", "0", currentReading);

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

    @SuppressLint("ResourceType")
    public void createTable() {
        TableRow tr_head = new TableRow(this);
        tr_head.setId(10);
        tr_head.setBackgroundColor(Color.GRAY);

        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));


        TextView label_ID = new TextView(this);
        label_ID.setId(20);
        label_ID.setText("Sensor ID");
        label_ID.setTextColor(Color.WHITE);
        label_ID.setPadding(50, 5, 5, 50);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setId(21);// define id that must be unique
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        label_Name.setText("Sensor/Data"); // set the text for the header
        label_Name.setTextColor(Color.WHITE); // set the color
        label_Name.setPadding(50, 5, 5, 50); // set the padding (if required)

        TextView label_signal = new TextView(this);
        label_signal.setId(21);// define id that must be unique
        label_signal.setText("Signal"); // set the text for the header
        label_signal.setTextColor(Color.WHITE); // set the color

        if(width < 1100)
        {
            label_Name.setWidth(375);
            label_signal.setPadding(100, 5, 5, 50); // set the padding (if required)
        }else
        {
            label_Name.setWidth(500);
            label_signal.setPadding(150, 5, 5, 50); // set the padding (if required)
        }

        tr_head.addView(label_Name); // add the column to the table row here
        tr_head.addView(label_signal); // add the column to the table row here

        TextView label_battery = new TextView(this);
        label_battery.setId(21);// define id that must be unique
        label_battery.setText("Battery"); // set the text for the header
        label_battery.setTextColor(Color.WHITE); // set the color
        label_battery.setPadding(50, 5, 50, 50); // set the padding (if required)
        tr_head.addView(label_battery); // add the column to the table row here

        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * @param _sensorID
     * @param _name
     * @param _date
     * @param _signal
     * @param _battery
     * @param _data
     */
    @SuppressLint("ResourceType")
    public void createSensorRow(String _sensorID, String _name, String _date, String _signal,
                                String _battery, String _data) {
        final TableRow tr_head = new TableRow(this);
        tr_head.setId(10);
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setPadding(0, 0, 0, 25);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));

        tr_head.setClickable(true);
        tr_head.setSelected(false);
        tr_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableRow tablerow = (TableRow) view;
                TextView sample = (TextView) tablerow.getChildAt(0);
                String result = sample.getText().toString();
                if (!tr_head.isSelected()) {
                    tr_head.setBackgroundColor(Color.CYAN);
                    tr_head.setSelected(true);
                } else {
                    tr_head.setBackgroundColor(Color.GRAY);
                    tr_head.setSelected(false);
                }

                Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
                toast.show();

            }
        });


        TextView label_ID = new TextView(this);
        label_ID.setId(20);
        label_ID.setText(_sensorID);
        label_ID.setTextColor(Color.WHITE);
        label_ID.setPadding(50, 5, 5, 5);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        if(width < 1100)
        {
            label_Name.setWidth(375);
        }else
        {
            label_Name.setWidth(500);
        }
        Toast.makeText(getApplicationContext(), "x size is: " + size.x, Toast.LENGTH_LONG).show();


        label_Name.setId(21);// define id that must be unique
        label_Name.setText(_name + "\n" + _data); // set the text for the header
        label_Name.setTextColor(Color.WHITE); // set the color
        label_Name.setPadding(50, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_Name); // add the column to the table row here

//        TextView label_data = new TextView(this);
//        label_data.setId(21);// define id that must be unique
//        label_data.setText(_data); // set the text for the header
//        label_data.setTextColor(Color.WHITE); // set the color
//        label_data.setPadding(50, 5, 5, 5); // set the padding (if required)
//        tr_head.addView(label_data); // add the column to the table row here

//        TextView last_com_date = new TextView(this);
//        last_com_date.setId(21);// define id that must be unique
//        last_com_date.setText(_date); // set the text for the header
//        last_com_date.setTextColor(Color.WHITE); // set the color
//        last_com_date.setPadding(50, 5, 5, 5); // set the padding (if required)
//        tr_head.addView(last_com_date); // add the column to the table row here

        ImageView image_signal = new ImageView(this);
        image_signal.setId(21);

        int strength = Integer.parseInt(_signal);
        Drawable drawable;
        if (strength <= 100 && strength >= 80) {
            drawable = getResources().getDrawable(R.drawable.signal_5_small);
        } else if (strength < 80 && strength >= 60) {
            drawable = getResources().getDrawable(R.drawable.signal_4_small);
        } else if (strength < 60 && strength >= 40) {
            drawable = getResources().getDrawable(R.drawable.signal_3_small);
        } else if (strength < 40 && strength >= 20) {
            drawable = getResources().getDrawable(R.drawable.signal_2_small);
        } else if (strength < 20 && strength > 0) {
            drawable = getResources().getDrawable(R.drawable.signal_1_small);
        } else {
            drawable = getResources().getDrawable(R.drawable.signal_none_small);
        }
        image_signal.setImageDrawable(drawable);
        image_signal.setPadding(100, 0, 0, 0);
        tr_head.addView(image_signal);

        ImageView image_battery = new ImageView(this);
        image_battery.setId(21);

        int battery = Integer.parseInt(_battery);
        Drawable drawable_battery;
        if (battery <= 100 && battery >= 75) {
            drawable_battery = getResources().getDrawable(R.drawable.batt_4);
        } else if (battery < 75 && battery >= 50) {
            drawable_battery = getResources().getDrawable(R.drawable.batt_3);
        } else if (battery < 50 && battery >= 25) {
            drawable_battery = getResources().getDrawable(R.drawable.batt_2);
        } else if (battery < 25 && battery >= 1) {
            drawable_battery = getResources().getDrawable(R.drawable.batt_1);
        } else {
            drawable_battery = getResources().getDrawable(R.drawable.batt_none);
        }
        image_battery.setImageDrawable(drawable_battery);
        image_battery.setPadding(0, 0, 0, 0);
        tr_head.addView(image_battery);
//        TextView label_signal = new TextView(this);
//        label_signal.setId(21);// define id that must be unique
//        label_signal.setText(_signal); // set the text for the header
//        label_signal.setTextColor(Color.WHITE); // set the color
//        label_signal.setPadding(150, 5, 5, 5); // set the padding (if required)
//        tr_head.addView(label_signal); // add the column to the table row here

//        TextView label_battery = new TextView(this);
//        label_battery.setId(21);// define id that must be unique
//        label_battery.setText(_battery); // set the text for the header
//        label_battery.setTextColor(Color.WHITE); // set the color
//        label_battery.setPadding(50, 5, 5, 5); // set the padding (if required)
//        tr_head.addView(label_battery); // add the column to the table row here

        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

}


























