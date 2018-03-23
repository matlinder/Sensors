package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mathew.sensorlogin.R;
import com.mathew.sensorlogin.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddUserPermissionActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, userID;
    private ProgressDialog prgDialog; //dialog
    private CheckBox ackNotifications, addNetwork, addUser, calibrateSensor, delUser, disableNotifications;
    private CheckBox editAccount, editGateway, editNetwork, editNotifications, editUsers, editSelf;
    private CheckBox editSensor, editSensorMult, editSensorGroup, editUserNames, export, modifyMap;
    private CheckBox unlock, pauseNotifications, resetOtherPass, sensorViewChart, viewMaps, viewMyAccount;
    private CheckBox viewNotifications, viewReports, viewSensorHistory;
    private Button addPermissions;
    private LinearLayout permLayout;
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    private ArrayList<String> seeNetworks = new ArrayList<String>();
    private Drawable id;
    private boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_permission);
        getSupportActionBar().setTitle("USER PERMISSIONS");

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            userID = extras.getString("userID");
            admin = extras.getBoolean("admin");
        }

        ackNotifications = findViewById(R.id.ackNotifications);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            id = ackNotifications.getButtonDrawable();
        }
        addNetwork = findViewById(R.id.addNetwork);
        addUser = findViewById(R.id.addUser);
        if(!admin){addUser.setVisibility(View.GONE);}
        calibrateSensor = findViewById(R.id.calibrateSensor);
        delUser = findViewById(R.id.delUser);
        disableNotifications = findViewById(R.id.disableNotifications);
        editAccount = findViewById(R.id.editAccount);
        editGateway = findViewById(R.id.editGateway);
        editNetwork = findViewById(R.id.editNetwork);
        editNotifications = findViewById(R.id.editNotifications);
        editUsers = findViewById(R.id.editUsers);
        editSelf = findViewById(R.id.editSelf);
        editSensor = findViewById(R.id.editSensor);
        editSensorMult = findViewById(R.id.editSensorMult);
        editSensorGroup = findViewById(R.id.editSensorGroup);
        editUserNames = findViewById(R.id.editUserNames);
        export = findViewById(R.id.export);
        modifyMap = findViewById(R.id.modifyMap);
        unlock = findViewById(R.id.unlock);
        pauseNotifications = findViewById(R.id.pauseNotifications);
        resetOtherPass = findViewById(R.id.resetOtherPass);
        sensorViewChart = findViewById(R.id.sensorChart);
        viewMaps = findViewById(R.id.viewMaps);
        viewMyAccount = findViewById(R.id.viewMyAccount);
        viewNotifications = findViewById(R.id.viewNotifications);
        viewReports = findViewById(R.id.viewReports);
        viewSensorHistory = findViewById(R.id.viewSensorHistory);

        addPermissions = findViewById(R.id.addPermissions);
        if(admin){addPermissions.setText("All Permissions Selected for Admin");}
        permLayout = findViewById(R.id.permLayout);

        displayNetworkData();
        //createNetworkCheckBoxes();

    }

    public void addPermissions(View view) {


        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();
        params.put("custID", userID);
        for(String name : seeNetworks)
        {
            String networkID = networkPair.get(name);

            params.add("Network_View_Net_"+networkID, "on");
        }

        params.put("Customer_Create", (addUser.isChecked() ?  "on" : "off" ));

        params.put("Customer_Delete", (delUser.isChecked() ?  "on" : "off"));
        params.put("Notification_Disable_Network",(disableNotifications.isChecked() ?  "on" : "off"));
        params.put("Account_Edit", (editAccount.isChecked() ?  "on" : "off"));
        params.put("Network_Edit_Gateway_Settings", (editGateway.isChecked() ?  "on" : "off"));
        params.put("Network_Edit", (editNetwork.isChecked() ?  "on" : "off"));
        params.put("Notification_Edit", (editNotifications.isChecked() ?  "on" : "off"));
        params.put("Customer_Edit_Other", (editUsers.isChecked() ?  "on" : "off"));
        params.put("Customer_Edit_Self", (editSelf.isChecked() ?  "on" : "off"));
        params.put("Sensor_Edit", (editSensor.isChecked() ?  "on" : "off"));
        params.put("Sensor_Configure_Multiple", (editSensorMult.isChecked() ?  "on" : "off"));
        params.put("Visual_Map_Edit", (modifyMap.isChecked() ?  "on" : "off"));
        params.put("Customer_Reset_Password_Other", (resetOtherPass.isChecked() ?  "on" : "off"));
        params.put("Sensor_Calibrate", (calibrateSensor.isChecked() ?  "on" : "off"));
        params.put("Sensor_Export_Data", (export.isChecked() ?  "on" : "off"));
        params.put("Sensor_View_Chart", (sensorViewChart.isChecked() ?  "on" : "off"));
        params.put("Navigation_View_Maps", (viewMaps.isChecked() ?  "on" : "off"));
        params.put("Navigation_View_My_Account", (viewMyAccount.isChecked() ?  "on" : "off"));
        params.put("Sensor_View_Notifications", (viewNotifications.isChecked() ?  "on" : "off"));
        params.put("Navigation_View_Reports", (viewReports.isChecked() ?  "on" : "off"));
        params.put("Sensor_View_History", (viewSensorHistory.isChecked() ?  "on" : "off"));
        params.put("Network_Create", (addNetwork.isChecked() ?  "on" : "off"));
//        params.put("Sensor_Advanced_Configuration
//        params.put("Sensor_Heartbeat_Restriction
        params.put("Customer_Change_Username", (editUserNames.isChecked() ?  "on" : "off"));
        params.put("Unlock_User", (unlock.isChecked() ?  "on" : "off"));
        params.put("Sensor_Group_Edit",(editSensorGroup.isChecked() ?  "on" : "off"));
//        params.put("Show_Dashboard
        params.put("Notification_Pause", (pauseNotifications.isChecked() ?  "on" : "off"));
//        params.put("Navigation_View_API
        params.put("Notification_Can_Acknowledge", (ackNotifications.isChecked() ?  "on" : "off"));


        client.get(base_url + "EditCustomerPermissions/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = null;
                    obj = new JSONObject(response);

                    String result = obj.getString("Result");

                    if(result.equals("Save successful.")) {
                        prgDialog.dismiss();
                        finish();
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "result is " + result, Toast.LENGTH_LONG).show();
                    }



                } catch (JSONException e)
                {
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
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end ", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });

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
                    int dp =(int) Utility.pxFromDp(getApplicationContext(), 8);
                    int[][] states = new int[][]{
                            new int[]{android.R.attr.state_checked}, //checked
                            new int[]{-android.R.attr.state_checked} //unchecked with negation
                    };
                    int[] colors = new int[]{
                            getApplication().getResources().getColor(R.color.colorAccent),
                            Color.BLACK
                    };
                    ColorStateList myList = new ColorStateList(states, colors);
                    for(String name : networkNames)
                    {
                        if(name != null)
                        {
                            CheckBox ch = new CheckBox(getApplicationContext());


                            ch.setText("Can See Network " + name);
                            ch.setPadding(dp, dp, dp, dp);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                ch.setButtonTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorAccent));
                                ch.setButtonTintList(myList);
                            }

                            ViewGroup.MarginLayoutParams params = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(dp,0,dp,0);
                            ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                {   String name = buttonView.getText().toString();
                                    name = name.substring(16);

                                    if(isChecked)
                                    {
                                        seeNetworks.add(name);
                                    }else
                                    {
                                        seeNetworks.remove(name);
                                    }


                                }
                            });
                            permLayout.addView(ch, params);

                        }
                    }
                    permLayout.removeView(addPermissions);
                    permLayout.addView(addPermissions);
                    addPermissions.setVisibility(View.VISIBLE);


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

//    private void createNetworkCheckBoxes() {
//
//        for(String name : networkNames)
//        {
//            if(name != null)
//            {
//                CheckBox ch = new CheckBox(getApplicationContext());
//                ch.setText("Can See Network " + name);
//                permLayout.addView(ch);
//
//            }
//        }
//
//
//    }


    /**
     * finish the activity and close the dialog
     * needed this because of leaked activity
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    /**
     * dismiss the dialog so that we do not have a leaked activity
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();
        finish();
    }
}
