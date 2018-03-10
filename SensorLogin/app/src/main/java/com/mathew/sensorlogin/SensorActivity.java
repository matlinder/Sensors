package com.mathew.sensorlogin;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.angmarch.views.NiceSpinner;
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
    // file name constants
    private static final String sensorFileName = "sensor_file";
    private static final String gatewayMapFileName = "gateway_file";
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    // used to store the time of the click, so you cannot spam clicks
    private long mLastClickTime = 0;
    // Strings to store the information from the previous intent
    private String authToken, networkID;
    // store today and yesterday for displaying activity
    private String today, yesterday;
    // main table to display the sensors
    private TableLayout mainTable;
    // maps to store sensor and network pairs
    private HashMap<String, String> sensorMap = new HashMap<String, String>();
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ProgressDialog prgDialog; //dialog
    private int currentMinutes; // the current time in minutes
    private TextView networkPrompt; // prompt to tell the user to select a network
    private boolean spinnerFlag = false; // flag to know when spinner is selected
    ArrayAdapter<String> dataAdapter; // adapter for the spinner
    NiceSpinner spinner; // the spinner

    // files to store the data from the json request
    private String sensorFileContents, gatewayFileContents;
    private String[] listOfSensors; // extrated file contents
    private String[] listOfPairs; // extracted file contents


    /**
     * Create the sensor activity to display all the sensors from the associated network
     * @param savedInstanceState
     */
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("SENSOR DATA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_sensor);

        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
        }



        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //grab the components from the view
        networkPrompt = findViewById(R.id.networkPrompt);
        mainTable = findViewById(R.id.main_table);
        spinner = findViewById(R.id.spinner);

        networkNames.add("Select a Network");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlag)
                {
                    networkNames.remove("Select a Network");
                    networkNames.add(0, "Clear All");
                    spinnerFlag = true;
                }
                if(id != 0)
                {
                    //position--; // snafu to reduce the position because the prompt messed it up
                    String networkName = parent.getItemAtPosition((int)id).toString();
                    if(!networkName.equals("Select a Network")) {
                        // display the associated sensors from the network
                        networkID = networkPair.get(networkName);
                        networkPrompt.setVisibility(View.INVISIBLE);
                        mainTable.removeAllViews(); //clear the table
                        createTable(); // create the header

                        displayNetworkSensors(networkID); // add the sensors to the rows
                    }
                }else
                {
                    // "Clear all" was selected so just clear the table
                    mainTable.removeAllViews();
                    createTable();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // gather all the data needed to run this activity from the following methods
        createDates();
        pullAllSensorIDs();
        readSensorFile();
        gatewaySensorPairing();
        readGatewayMapFile();
        displayNetworkData();


    }



    /**
     * display the network data of the associated account
     * user to select which network to display gateways from
     */
    public void displayNetworkData() {
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "NetworkList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    prgDialog.hide();
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempNetwork = objArray.getJSONObject(i);
                        String name = tempNetwork.getString("NetworkName");
                        String ID = tempNetwork.getString("NetworkID");
                        networkPair.put(name, ID);
                        networkNames.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,networkNames){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinner.setAdapter(dataAdapter);

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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /*
     * loop sesnsors and request data messages
     * pull the gateway ID and write it to a file with the sensor
     */
    private void gatewaySensorPairing() {
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });

    }



    /*
     * Method to create todays and yesterdays date in a format that can be used as params
     */
    private void createDates() {
        Calendar todaysDate = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        today = df.format(todaysDate.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String hours = sdf.format(todaysDate.getTime());
        sdf = new SimpleDateFormat("mm");
        String minutes = sdf.format(todaysDate.getTime());
        currentMinutes = (Integer.parseInt(hours) * 60) + Integer.parseInt(minutes);


        Calendar yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DATE, -1);
        yesterday = df.format(yesterdayDate.getTime());
    }

    /*
     * Read the sensor file stream and put its contents into a String builder
     * The contents will have a newline character after each item
     * call split method to split on newline character
     */
    private void readSensorFile() {
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

    /*
     * Split the sensor file at the newline character, and store into an array
     */
    private void splitSensors() {
        listOfSensors = sensorFileContents.split("\\n");
    }

    /*
     * method to read the gateway file and put the file contents in a String array
     * String array will be in the format of "sensorID-gatewayID"
     * need to split it up and store into hashmap
     */
    private void readGatewayMapFile() {
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

    /*
     * Gateways are currently in a String format of "sensorID-gatewayID"
     * split the two pieces of information, storing them in a key/value pair
     * using a hashmap
     */
    private void splitGateways() {
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
     * Display all the sensors from a given network ID
     * Network ID is grabbed from the spinner
     * @param _networkID
     */
    public void displayNetworkSensors(String _networkID)
    {
        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();
        params.put("networkID", _networkID);
        client.get(base_url + "SensorListExtended/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try {
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONArray resultArray = obj.getJSONArray("Result");

                    for(int i=0; i < resultArray.length(); i++) {

                        JSONObject result = resultArray.getJSONObject(i);
                        if (result != null) {
                            String sensorID = result.getString("SensorID");
                            String currentReading = result.getString("CurrentReading");
                            String name = result.getString("SensorName");
                            String batteryLevel = result.getString("BatteryLevel");
                            String signalStrength = result.getString("SignalStrength");
                            String lastCommunicationDate = result.getString("LastCommunicationDate");
                            String inactivityAlert = result.getString("InactivityAlert");

                            Calendar tempStart = Calendar.getInstance();
                            Date currentDate = tempStart.getTime();

                            long time = Long.parseLong(lastCommunicationDate.substring(6, lastCommunicationDate.length() - 2));
                            Date lastCommDate = new Date(time);
                            Calendar lastDate = Calendar.getInstance();
                            lastDate.setTime(lastCommDate);
                            String myDate = new SimpleDateFormat("yyyy/MM/dd").format(lastCommDate);
                            String shours = new SimpleDateFormat("HH").format(lastCommDate);
                            String sMinutes = new SimpleDateFormat("mm").format(lastCommDate);
                            int sensorTotalMinutes = (Integer.parseInt(shours) * 60) + Integer.parseInt(sMinutes);
                            boolean alert = false;

                            boolean year = tempStart.get(Calendar.YEAR) == lastDate.get(Calendar.YEAR);
                            boolean day = tempStart.get(Calendar.DAY_OF_MONTH) == lastDate.get(Calendar.DAY_OF_MONTH);
                            boolean month = tempStart.get(Calendar.MONTH) == lastDate.get(Calendar.MONTH);

                            if((day && year && month) && ((currentMinutes - sensorTotalMinutes) >= Integer.parseInt(inactivityAlert))) {
                                alert = true;
                            }
                            else if((!day && year && month) || (!day && !month && year) || (!day && !month && !year))
                            {
                                alert = true;
                            }
                            createSensorRow(sensorID, name, myDate, signalStrength, batteryLevel, currentReading, alert);
                        }
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
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
                    createSensorRow(_sensorID, name, myDate, signalStrength, batteryLevel, currentReading, alert);

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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Create the initial heading row of the table
     */
    @SuppressLint("ResourceType")
    public void createTable() {
        TableRow tr_head = new TableRow(this);
        tr_head.setId(10);
        tr_head.setBackgroundColor(Color.DKGRAY);

        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));


        TextView label_ID = new TextView(this);
        label_ID.setId(20);
        label_ID.setText("Sensor ID");
        label_ID.setTextColor(Color.WHITE);
        label_ID.setPadding(50, 50, 5, 50);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setId(21);// define id that must be unique
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        label_Name.setText("Sensor/Data"); // set the text for the header
        label_Name.setTextColor(Color.WHITE); // set the color
        label_Name.setPadding(50, 50, 5, 50); // set the padding (if required)

        TextView label_signal = new TextView(this);
        label_signal.setId(21);// define id that must be unique
        label_signal.setText("Signal"); // set the text for the header
        label_signal.setTextColor(Color.WHITE); // set the color

        if(width < 1100)
        {
            label_Name.setWidth(375);
            label_signal.setPadding(100, 50, 5, 50); // set the padding (if required)
        }else
        {
            label_Name.setWidth(500);
            label_signal.setPadding(150, 50, 5, 50); // set the padding (if required)
        }

        tr_head.addView(label_Name); // add the column to the table row here
        tr_head.addView(label_signal); // add the column to the table row here

        TextView label_battery = new TextView(this);
        label_battery.setId(21);// define id that must be unique
        label_battery.setText("Battery"); // set the text for the header
        label_battery.setTextColor(Color.WHITE); // set the color
        label_battery.setPadding(50, 50, 50, 50); // set the padding (if required)
        tr_head.addView(label_battery); // add the column to the table row here

        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /*
     * Private method to display the sensor in a row in the table
     */
    @SuppressLint("ResourceType")
    private void createSensorRow(final String _sensorID, String _name, String _date, String _signal,
                                String _battery, String _data, final boolean _alert) {
        final TableRow tr_head = new TableRow(this);
        tr_head.setId(10);
        //tr_head.setBackgroundColor(getResources().getColor(R.color.tableBackGround));
        tr_head.setBackgroundColor(Color.WHITE);
        tr_head.setPadding(0, 25, 0, 25);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));

        tr_head.setClickable(true);
        tr_head.setSelected(false);
        // on click listener to display the sensor in more details when clicked from the table
        tr_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableRow tablerow = (TableRow) view;
                TextView sample = (TextView) tablerow.getChildAt(0);
                String result = sample.getText().toString();
                if (!tr_head.isSelected()) {

                    Intent intent = new Intent(getApplicationContext(), SensorDetailActivity.class);
                    intent.putExtra("token", authToken);
                    intent.putExtra("sensorID", _sensorID);
                    intent.putExtra("alert", _alert);

                    startActivity(intent);
                }
            }
        });


        TextView label_ID = new TextView(this);
        label_ID.setId(20);
        label_ID.setText(_sensorID);
        label_ID.setTextColor(Color.BLACK);
        label_ID.setPadding(50, 5, 5, 5);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // quick fix to fix some formatting for screen size.
        // will need to implement multiple screen sizes in the full release
        if(width < 1100)
        {
            label_Name.setWidth(375);
        }else
        {
            label_Name.setWidth(500);
        }

        label_Name.setId(21);// define id that must be unique
        label_Name.setText(_name + "\n" + _data); // set the text for the header

        // alert flag if the sensor is not communicating
        if(_alert)
        {
            label_Name.setTextColor(Color.GRAY); // set the color
        }else
        {
            label_Name.setTextColor(Color.BLACK); // set the color

        }

        label_Name.setPadding(50, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_Name); // add the column to the table row here

        ImageView image_signal = new ImageView(this);
        image_signal.setId(21);

        int strength = Integer.parseInt(_signal);
        Drawable drawable;

        // signal icons
        if(_alert)
        {
            drawable = getResources().getDrawable(R.drawable.question_small);
        }
        else if (strength <= 100 && strength >= 80) {
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

        // battery icons
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

        // add the row to the table
        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }



    /**
     * Method that will call the display sensors to pull the data, refresh the data, on the table
     * so that the user can see the changes
     * @param view the activity
     */
    public void refreshSensors(View view) {

        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();


        mainTable.removeAllViews();
        createTable();
        createDates();
        if(networkID != null) {
            displayNetworkSensors(networkID);
        }
    }

    /**
     * Clicking the back button on the title bar returns to the previous activity on the stack
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                prgDialog.dismiss();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for back button on title bar
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * What to do when the acitivty is destroyed
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    /**
     * what to do when the activity is paused
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();

    }

    /**
     * what to do when the activity is resumed
     */
    public void onResume()
    {
        super.onResume();
        refreshSensors(this.mainTable);
    }
}


























