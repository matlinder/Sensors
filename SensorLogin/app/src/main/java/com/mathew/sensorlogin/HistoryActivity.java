package com.mathew.sensorlogin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HistoryActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private static final int DATE_DIALOG_ID_START = 0; // constant to choose the start date picker
    private static final int DATE_DIALOG_ID_END = 1; // constant to choose the end date picker

    private String authToken, sensorID; // variables to store the data passed from the previous activity
    private TableLayout mainTable; // table to display all of the sensors history
    private ProgressDialog prgDialog; // progress dialog for long requests
    private EditText startDate, endDate; // edittext for the user to see/pick the dates
    // variables to help with formatting the dates
    private int startYear, startMonth, startDay, endYear, endMonth, endDay ;
    private String endParam; // end date param for request
    private String startParam; // start date param for request
    private Calendar todaysDate, yesterdayDate; // instances of today and yesterday's dates
    private boolean backgroundColor = true;

    /**
     * Create the history activity and populate the table with the last day's history of the sensor
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_history);
        createDates();
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
            getSupportActionBar().setTitle("SENSOR " + sensorID);
        }
        mainTable = findViewById(R.id.main_table);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showDialog(DATE_DIALOG_ID_START); }
        });
        endDate.setOnClickListener(new View.OnClickListener(){
           public void onClick(View v) { showDialog(DATE_DIALOG_ID_END);}
        });

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle );
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        createTable();

        // display the current date
        updateStartDate();
        updateEndDate();
        displayDataHistory();
    }
    //return date picker dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID_START:
                return new DatePickerDialog(this, startDateSetListener, startYear, startMonth, startDay);
            case DATE_DIALOG_ID_END:
                return new DatePickerDialog(this, endDateSetListener, endYear, endMonth, endDay);

        }
        return null;
    }

    //update month day year
    private void updateStartDate() {
        startDate.setText(//this is the edit text where you want to show the selected date
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(startYear).append("/")
                        .append(startMonth + 1).append("/")
                        .append(startDay).append(""));

        startParam = startYear + "/" + (startMonth + 1) + "/" + startDay;

    }

    // the call back received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener startDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar tempStart = new GregorianCalendar(year, monthOfYear,dayOfMonth);
                    if(tempStart.compareTo(todaysDate) < 0)
                    {
                        startYear = year;
                        startMonth = monthOfYear;
                        startDay = dayOfMonth;
                        yesterdayDate = tempStart;
                        updateStartDate();
                        displayDataHistory();
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "Invalid Date Range", Toast.LENGTH_LONG).show();

                    }
                }
            };
    //update month day year
    private void updateEndDate() {
        endDate.setText(//this is the edit text where you want to show the selected date
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(endYear).append("/")
                        .append(endMonth + 1).append("/")
                        .append(endDay).append(""));

        endParam = endYear + "/" + (endMonth + 1) + "/" + endDay;
    }

    // the call back received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener endDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar tempEnd = new GregorianCalendar(year, monthOfYear,dayOfMonth);
                    if(tempEnd.compareTo(yesterdayDate) > 0) {
                        endYear = year;
                        endMonth = monthOfYear;
                        endDay = dayOfMonth;
                        todaysDate = tempEnd;
                        updateEndDate();
                        displayDataHistory();
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "Invalid Date Range", Toast.LENGTH_LONG).show();

                    }
                }
            };
    /**
     * Request the data messages of the sensor that came from the previous activity
     * Create a row for each one and assign it to the table
     */
    public void displayDataHistory()
    {
        // Show Progress Dialog
        prgDialog.show();
        mainTable.removeAllViews();
        createTable();
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
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONArray resultArray = obj.getJSONArray("Result");
                    // loop the results
                    for(int i=0; i < resultArray.length(); i++) {

                        JSONObject result = resultArray.getJSONObject(i);
                        if (result != null) {
                            String currentReading = result.getString("DisplayData");
                            String batteryLevel = result.getString("Battery");
                            String signalStrength = result.getString("SignalStrength");
                            String messageDate = result.getString("MessageDate");


                            long time = Long.parseLong(messageDate.substring(6, messageDate.length() - 2));
                            Date date = new Date(time);


                            createSensorRow(date, signalStrength, batteryLevel, currentReading);
                        }
                    }

                    // Hide Progress Dialog
                    prgDialog.hide();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    prgDialog.hide();
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        endYear = todaysDate.get(Calendar.YEAR);
        endMonth = todaysDate.get(Calendar.MONTH);
        endDay = todaysDate.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        endParam = df.format(todaysDate.getTime());

        yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DATE, -2);
        startYear = yesterdayDate.get(Calendar.YEAR);
        startMonth = yesterdayDate.get(Calendar.MONTH);
        startDay = yesterdayDate.get(Calendar.DAY_OF_MONTH);
        startParam = df.format(yesterdayDate.getTime());
    }

    /**
     * Create the heading row of the table
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
        label_ID.setText("Date");
        label_ID.setWidth(350);
        label_ID.setTextColor(Color.WHITE);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if(width < 1100)
        {
            label_ID.setWidth(350);
            label_ID.setPadding(50, 0, 5, 0); // set the padding (if required)
        }else
        {
            label_ID.setWidth(500);
            label_ID.setPadding(150, 50, 5, 50); // set the padding (if required)
        }
        //label_ID.setPadding(50, 0, 5, 0);
        //label_ID.setPadding(50, 50, 5, 50);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setId(21);// define id that must be unique

        label_Name.setText("Signal"); // set the text for the header
        label_Name.setTextColor(Color.WHITE); // set the color
        label_Name.setPadding(50, 50, 5, 50); // set the padding (if required)

        TextView label_signal = new TextView(this);
        label_signal.setId(21);// define id that must be unique
        label_signal.setText("Battery"); // set the text for the header
        label_signal.setTextColor(Color.WHITE); // set the color
        label_signal.setPadding(50, 50, 5, 50);

//        if(width < 1100)
//        {
//            label_Name.setWidth(375);
//            label_signal.setPadding(100, 50, 5, 50); // set the padding (if required)
//        }else
//        {
//            label_Name.setWidth(500);
//            label_signal.setPadding(150, 50, 5, 50); // set the padding (if required)
//        }

        tr_head.addView(label_Name); // add the column to the table row here
        tr_head.addView(label_signal); // add the column to the table row here

        TextView label_battery = new TextView(this);
        label_battery.setId(21);// define id that must be unique
        label_battery.setText("Sensor Reading"); // set the text for the header
        label_battery.setTextColor(Color.WHITE); // set the color
        label_battery.setPadding(50, 50, 50, 50); // set the padding (if required)
        tr_head.addView(label_battery); // add the column to the table row here

        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Create the row with the specified data for the sensor data message
     * @param date The date and time the message was sent
     * @param signalStrength the signal strength at the time of the message
     * @param batteryLevel the battery strength at the time of the message
     * @param currentReading the current temp reading at the time of the message
     */
    @SuppressLint("ResourceType")
    public void createSensorRow(Date date, String signalStrength, String batteryLevel, String currentReading) {
        final TableRow tr_head = new TableRow(this);
        tr_head.setId(10);
        //tr_head.setBackgroundColor(getResources().getColor(R.color.tableBackGround));
        tr_head.setBackgroundColor(Color.WHITE);
        tr_head.setPadding(0, 25, 0, 0);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));

        TextView label_ID = new TextView(this);
        label_ID.setId(20);
        String myDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
        label_ID.setText(myDate );
        label_ID.setWidth(350);
        label_ID.setTextColor(Color.BLACK);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if(width < 1100)
        {
            label_ID.setWidth(350);
            label_ID.setPadding(50, 0, 5, 15); // set the padding (if required)
        }else
        {
            label_ID.setWidth(500);
            label_ID.setPadding(150, 25, 5, 25); // set the padding (if required)
        }
        //label_ID.setPadding(50, 0, 5, 0);
        tr_head.addView(label_ID);// add the column to the table row here

        TextView label_Name = new TextView(this);
        label_Name.setId(21);// define id that must be unique
        label_Name.setText(signalStrength); // set the text for the header
        label_Name.setTextColor(Color.BLACK); // set the color
        label_Name.setPadding(75, 0, 5, 0); // set the padding (if required)

        TextView label_signal = new TextView(this);
        label_signal.setId(21);// define id that must be unique
        label_signal.setText(batteryLevel); // set the text for the header
        label_signal.setTextColor(Color.BLACK); // set the color
        label_signal.setPadding(75, 0, 5, 0);



        tr_head.addView(label_Name); // add the column to the table row here
        tr_head.addView(label_signal); // add the column to the table row here

        TextView label_battery = new TextView(this);
        label_battery.setId(21);// define id that must be unique
        label_battery.setText(currentReading); // set the text for the header
        label_battery.setTextColor(Color.BLACK); // set the color
        label_battery.setWidth(350);
        label_battery.setPadding(100, 0, 0, 0); // set the padding (if required)
        tr_head.addView(label_battery); // add the column to the table row here
        if(backgroundColor) {
            tr_head.setBackgroundColor(Color.LTGRAY);
        }

        backgroundColor = !backgroundColor;

        mainTable.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Menu item method for back button
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

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Method to close the dialog and activity when you leave the activity
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }
    /**
     * Method to close the dialog and activity when you leave the activity
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();
        finish();
    }
}
