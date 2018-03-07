package com.mathew.sensorlogin;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import java.util.Collections;
import java.util.Date;


public class GraphActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    private String authToken, sensorID;     // strings to hold values passed from previous activity
    private Calendar todaysDate;    // the current date
    private Calendar yesterdayDate;     // the current date -1, yesterday
    private String endParam;    // String to hold the end date param
    private String startParam;  // String to hold the start date param


    private final String GRAPH_FILE = "graphPlots"; // file name to hold graph entries
    private String fileContents;    // holds the contents of the file in unformatted form
    private String[] listOfPairs;   // holds conent of the file in formatted form

    LineChart chart;    // chart to display the plot data on

    // Arraylist to hold graph entries
    ArrayList<Entry> entries = new ArrayList<Entry>();
    // Arraylist to hold the x labels
    ArrayList<String>xVals = new ArrayList<String>();
    private ProgressDialog prgDialog;

    /**
     * Create the graph activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
        }
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        //graph = (GraphView) findViewById(R.id.graph);
        chart = findViewById(R.id.chart);

        createDates();
        populateDataPoints();
        //displayEntryGraph();

    }

    /**
     * After all the data is collected, display the graph
     */
    private void displayEntryGraph()
    {
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.RED);
        dataSet.setDrawCircles(false);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setValueTextSize(15f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(false);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        XAxis xAxis = chart.getXAxis();
        String[] labels = xVals.toArray(new String[xVals.size()]);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(labels));
        xAxis.setGranularity(2.0f);
        chart.invalidate(); //refresh

    }

    /**
     * Grab all the data messages from the chosen sensor
     * write them to an internal file so that it make be parsed outside of the request
     * and stored to the lists and then graphed
     */
    public void populateDataPoints()
    {
        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params
        RequestParams params = new RequestParams();
        params.put("SensorID", sensorID);
        params.put("fromDate", startParam);
        params.put("toDate", endParam);
        client.get(base_url + "SensorDataMessages/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    FileOutputStream outputStream = openFileOutput(GRAPH_FILE, Context.MODE_PRIVATE);
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONArray resultArray = obj.getJSONArray("Result");
                    if(resultArray.length() > 0) {
                        for (int i = 0; i < resultArray.length(); i++) {

                            JSONObject result = resultArray.getJSONObject(i);
                            if (result != null) {
                                String currentReading = result.getString("PlotValues");
                                if (currentReading.length() == 0) {
                                    currentReading = "-100"; // set to a negative number to show an error
                                }
                                String messageDate = result.getString("MessageDate");
                                // write the temperature value and the date/time to a file
                                outputStream.write(messageDate.getBytes());
                                outputStream.write(",".getBytes());
                                outputStream.write(currentReading.getBytes());
                                outputStream.write("\n".getBytes());
                            }
                        }
                        //close the file
                        outputStream.close();
                        // have to chain these calls
                        // called out of order otherwise, causing null pointer
                        readGatewayMapFile();
                        displayEntryGraph();
                    }else
                    {
                        //display a message if there is no data to graph
                        Toast.makeText(getApplicationContext(), "Sensor was not active for the past day, no data to display", Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
    /**
     * Method to create todays and yesterdays date in a format that can be used as params
     */
    public void createDates() {

        todaysDate = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        endParam = df.format(todaysDate.getTime());

        yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DATE, -1);
        startParam = df.format(yesterdayDate.getTime());
    }

    /**
     * Read the file and extract the contents to an arraylist
     */
    public void readGatewayMapFile() {
        try {
            FileInputStream read = openFileInput(GRAPH_FILE);
            InputStreamReader isr = new InputStreamReader(read);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            fileContents = sb.toString();
            listOfPairs = fileContents.split("\\n");
            splitPlotValues();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used internally to split the values on "," so we have pairs
     */
    public void splitPlotValues() {
        if (listOfPairs != null) {
            int reverseCount = listOfPairs.length -1;
            for (int i = 0; i < listOfPairs.length; i++) {

                String[] parts = listOfPairs[i].split(",");
                //grab the date and convert it
                if (parts != null && parts[0] != null && parts[1] != null) {

                    String messageDate = parts[0];
                    long time = Long.parseLong(messageDate.substring(6, messageDate.length() - 2));
                    Date date = new Date(time);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    String dayTime = df.format(date.getTime());
                    if(parts[1].length() != 0) {
                        Entry temp = new Entry(reverseCount, Float.parseFloat(parts[1].toString()));
                        entries.add(temp);
                        xVals.add(dayTime);
                        reverseCount--;
                    }
                    else
                    {
                        Entry temp = new Entry(reverseCount, 0.0f);
                        entries.add(temp);
                        xVals.add(dayTime);
                        reverseCount--;
                    }




                }
            }
            Collections.reverse(xVals);
            Collections.reverse(entries);
        }
    }

    /**
     * Android methods to close the dialog and activity
     * prevents leaked windows
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    /**
     * Android methods to close the dialog and activity
     * prevents leaked windows
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();
        finish();
    }
}
