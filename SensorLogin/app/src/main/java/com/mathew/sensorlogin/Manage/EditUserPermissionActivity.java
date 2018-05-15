package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.Iterator;
import java.util.Map;

/**
 * I hate this activity.
 * Hackjob to get the checkboxes to work because for unknown reasons they would change from checked
 * to unchecked or while being unchecked, they would show a black checkmark.
 *
 * Acitivty to edit a users details and/or their permissions
 */
public class EditUserPermissionActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, userID;
    private ProgressDialog prgDialog; //dialog
    private Button details, permissions;
    private TextView firstLabel, lastLabel, emailLabel, errorFirstName, errorLastName, errorEmail;
    private EditText first, last, email;
    private ViewGroup transitionsContainer;

    private CheckBox ackNotifications, addNetwork, calibrateSensor, delUser, disableNotifications;
    private CheckBox editAccount, editGateway, editNetwork, editNotifications, editUsers, editSelf;
    private CheckBox editSensor, editSensorMult, editSensorGroup, editUserNames, export, modifyMap;
    private CheckBox unlock, pauseNotifications, resetOtherPass, sensorViewChart, viewMaps, viewMyAccount;
    private CheckBox viewNotifications, viewReports, viewSensorHistory;
    private ArrayList<String> networkNames = new ArrayList<String>();
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    private ArrayList<String> seeNetworks = new ArrayList<String>();
    private HashMap<String, CheckBox> networkCheckBox = new HashMap<String, CheckBox>();
    private HashMap<String, String> permissionPair = new HashMap<String, String>();
    private HashMap<String, CheckBox> varCheckPair = new HashMap<String, CheckBox>();
    private boolean emailChanged = false;
    private boolean nameChanged = false;
    private boolean validEmail = true;
    private boolean checkBoxChange = false;

    @Override
    /**
     * Creates the activity with buttons, edit fields and check boxes
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_permission);

        getSupportActionBar().setTitle("EDIT USER");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        }

        initVariables();
        populateUserDetails();
        displayNetworkData();
        populateUserPermissions();
        setVisibility();
        testChecked();
        testChecked();
        initListeners();
        first.addTextChangedListener(nameTextWatcher);
        last.addTextChangedListener(nameTextWatcher);
        email.addTextChangedListener(emailTextWatcher);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus && emailChanged)
                {
                    if(email != null && email.length() != 0 && !Utility.validate(email.getText().toString()))
                    {
                        //Toast.makeText(getApplicationContext(), "Email is not the correct format", Toast.LENGTH_LONG).show();
                        //email.setTextColor(Color.RED);
                        errorEmail.setVisibility(View.VISIBLE);
                        validEmail = false;
                    }else
                    {
                        //email.setTextColor(Color.BLACK);
                        errorEmail.setVisibility(View.INVISIBLE);
                        validEmail = true;
                    }
                }
            }
        });

        emailChanged = false;
        nameChanged = false;
        checkBoxChange = false;
    }

    /**
     * text watcher to see that the edit fields have been changed
     * if no changes, no need to call the method to change them
     */
    private TextWatcher nameTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nameChanged = true;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    /*
     * Text change listener
     * Only change value if the text has been changed, otherwise finish the activity
     */
    private TextWatcher emailTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            emailChanged = true;

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * users will have permissions already, grab the permissions and set the checkboxes to true
     * or false accordingly
     * calls GetCustomerPermissions
     */
    private void populateUserPermissions() {

        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();
        params.put("custID", userID);
        client.get(base_url + "GetCustomerPermissions/" + authToken, params, new AsyncHttpResponseHandler() {

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
                            String name = result.getString("Name");
                            boolean isChecked = result.getBoolean("Can");
                            if(name.contains("Network_View"))
                            {
                                name = name.substring(13);
                                CheckBox temp = networkCheckBox.get(name);

                                if(temp != null) {
                                    temp.setChecked(isChecked);

                                }

                            }else {
                                CheckBox temp2 = varCheckPair.get(name);
                                if (temp2 != null ) {
                                    temp2.setChecked(isChecked);
                                }
                            }
                        }
                    }
                    emailChanged = false;
                    nameChanged = false;
                    checkBoxChange = false;

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
     * Call AccountUserGet and get the selected user information to populate the fields so you can
     * see what the information is. Changing the fields and saving will change the account info.
     */
    private void populateUserDetails() {

        AsyncHttpClient client = new AsyncHttpClient();
        //params
        RequestParams params = new RequestParams();
        params.add("userID", userID);
        client.get(base_url + "AccountUserGet/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    JSONObject result = obj.getJSONObject("Result");;
                    String ID = result.getString("UserID");
                    String firstName = result.getString("FirstName");
                    String lastName = result.getString("LastName");
                    String name = first + " " + last;
                    String emailAdd = result.getString("EmailAddress");

                    first.setText(firstName);
                    last.setText(lastName);
                    email.setText(emailAdd);




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
                            ch.setButtonTintList(myList);
                            ViewGroup.MarginLayoutParams params = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(dp,0,dp,0);
                            ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                {
                                    String name = buttonView.getText().toString();
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
                            //ch.setVisibility(View.GONE);
                            ch.setVisibility(View.INVISIBLE);
                            ch.setChecked(true);
                            networkCheckBox.put(networkPair.get(name), ch);
                            transitionsContainer.addView(ch, params);
                        }
                    }} catch (JSONException e) {
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
     * switching method to hide the checkboxes and show the edittext fields
     * @param view
     */
    public void userDetailsDisplay(View view) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        firstLabel.setVisibility(View.VISIBLE);
        lastLabel.setVisibility(View.VISIBLE);
        emailLabel.setVisibility(View.VISIBLE);
        first.setVisibility(View.VISIBLE);
        last.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        errorFirstName.setVisibility(View.INVISIBLE);
        errorLastName.setVisibility(View.INVISIBLE);
        errorEmail.setVisibility(View.INVISIBLE);

        ackNotifications.setVisibility(View.GONE);
        addNetwork.setVisibility(View.GONE);
        calibrateSensor.setVisibility(View.GONE);
        delUser.setVisibility(View.GONE);
        disableNotifications.setVisibility(View.GONE);
        editAccount.setVisibility(View.GONE);
        editGateway.setVisibility(View.GONE);
        editNetwork.setVisibility(View.GONE);
        editNotifications.setVisibility(View.GONE);
        editUsers.setVisibility(View.GONE);
        editSelf.setVisibility(View.GONE);
        editSensor.setVisibility(View.GONE);
        editSensorMult.setVisibility(View.GONE);
        editSensorGroup.setVisibility(View.GONE);
        editUserNames.setVisibility(View.GONE);
        export.setVisibility(View.GONE);
        modifyMap.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        pauseNotifications.setVisibility(View.GONE);
        resetOtherPass.setVisibility(View.GONE);
        sensorViewChart.setVisibility(View.GONE);
        viewMaps.setVisibility(View.GONE);
        viewMyAccount.setVisibility(View.GONE);
        viewNotifications.setVisibility(View.GONE);
        viewReports.setVisibility(View.GONE);
        viewSensorHistory.setVisibility(View.GONE);

        Iterator it = networkCheckBox.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            CheckBox check = (CheckBox)pair.getValue();
            check.setVisibility(View.GONE);
        }
    }

    /**
     * switching method to hide the edittext fields and show the checkboxes
     * @param view
     */
    public void userPermissionsDisplay(View view) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        firstLabel.setVisibility(View.GONE);
        lastLabel.setVisibility(View.GONE);
        emailLabel.setVisibility(View.GONE);
        first.setVisibility(View.GONE);
        last.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        errorFirstName.setVisibility(View.GONE);
        errorLastName.setVisibility(View.GONE);
        errorEmail.setVisibility(View.GONE);

        ackNotifications.setVisibility(View.VISIBLE);
        addNetwork.setVisibility(View.VISIBLE);
        calibrateSensor.setVisibility(View.VISIBLE);
        delUser.setVisibility(View.VISIBLE);
        disableNotifications.setVisibility(View.VISIBLE);
        editAccount.setVisibility(View.VISIBLE);
        editGateway.setVisibility(View.VISIBLE);
        editNetwork.setVisibility(View.VISIBLE);
        editNotifications.setVisibility(View.VISIBLE);
        editUsers.setVisibility(View.VISIBLE);
        editSelf.setVisibility(View.VISIBLE);
        editSensor.setVisibility(View.VISIBLE);
        editSensorMult.setVisibility(View.VISIBLE);
        editSensorGroup.setVisibility(View.VISIBLE);
        editUserNames.setVisibility(View.VISIBLE);
        export.setVisibility(View.VISIBLE);
        modifyMap.setVisibility(View.VISIBLE);
        unlock.setVisibility(View.VISIBLE);
        pauseNotifications.setVisibility(View.VISIBLE);
        resetOtherPass.setVisibility(View.VISIBLE);
        sensorViewChart.setVisibility(View.VISIBLE);
        viewMaps.setVisibility(View.VISIBLE);
        viewMyAccount.setVisibility(View.VISIBLE);
        viewNotifications.setVisibility(View.VISIBLE);
        viewReports.setVisibility(View.VISIBLE);
        viewSensorHistory.setVisibility(View.VISIBLE);

        Iterator it = networkCheckBox.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            CheckBox check = (CheckBox) pair.getValue();
            check.setVisibility(View.VISIBLE);
        }
    }

    /*
     * method to initialize the listeners on the checkboxes
     */
    private void initListeners()
    {
        Iterator iit = networkCheckBox.entrySet().iterator();
        while (iit.hasNext()) {
            Map.Entry pairr = (Map.Entry) iit.next();
            CheckBox checkk = (CheckBox) pairr.getValue();
            checkk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    checkBoxChange = true;
                }

            });
        }

        Iterator it = varCheckPair.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            CheckBox check = (CheckBox) pair.getValue();
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    checkBoxChange = true;
                }

            });
        }


    }

    /*
     * initialize the variables by locating the view on the layout
     */
    private void initVariables() {
        details = findViewById(R.id.buttonOne);
        permissions = findViewById(R.id.buttonTwo);
        firstLabel = findViewById(R.id.firstText);
        lastLabel = findViewById(R.id.lastText);
        emailLabel = findViewById(R.id.emailText);
        first = findViewById(R.id.editFirstName);
        last = findViewById(R.id.editLastName);
        email = findViewById(R.id.editEmail);
        errorFirstName = findViewById(R.id.errorFirstName);
        errorLastName = findViewById(R.id.errorLastName);
        errorEmail = findViewById(R.id.errorEmail);
        transitionsContainer = findViewById(R.id.linearTrans);

        ackNotifications = findViewById(R.id.ackNotifications2);
        varCheckPair.put("Notification_Can_Acknowledge", ackNotifications);
        addNetwork = findViewById(R.id.addNetwork2);
        varCheckPair.put("Network_Create", addNetwork);
        calibrateSensor = findViewById(R.id.calibrateSensor2);
        varCheckPair.put("Sensor_Calibrate",calibrateSensor  );
        delUser = findViewById(R.id.delUser2);
        varCheckPair.put("Customer_Delete",delUser  );
        disableNotifications = findViewById(R.id.disableNotifications2);
        varCheckPair.put("Notification_Disable_Network",disableNotifications  );
        editAccount = findViewById(R.id.editAccount2);
        varCheckPair.put("Account_Edit", editAccount );
        editGateway = findViewById(R.id.editGateway2);
        varCheckPair.put("Network_Edit_Gateway_Settings",editGateway  );
        editNetwork = findViewById(R.id.editNetwork2);
        varCheckPair.put("Network_Edit",editNetwork  );
        editNotifications = findViewById(R.id.editNotifications2);
        varCheckPair.put("Notification_Edit", editNotifications );

        editUsers = findViewById(R.id.editUsers2);
        varCheckPair.put("Customer_Edit_Other",editUsers  );
        editSelf = findViewById(R.id.editSelf2);
        varCheckPair.put("Customer_Edit_Self",editSelf );
        editSensor = findViewById(R.id.editSensor2);
        varCheckPair.put("Sensor_Edit",editSensor );
        editSensorMult = findViewById(R.id.editSensorMult2);
        varCheckPair.put("Sensor_Configure_Multiple",editSensorMult );
        editSensorGroup = findViewById(R.id.editSensorGroup2);
        varCheckPair.put("Sensor_Group_Edit",editSensorGroup );
        editUserNames = findViewById(R.id.editUserNames2);
        varCheckPair.put("Customer_Change_Username",editUserNames );
        export = findViewById(R.id.export2);
        varCheckPair.put("Sensor_Export_Data",export );
        modifyMap = findViewById(R.id.modifyMap2);
        varCheckPair.put("Visual_Map_Edit", modifyMap);
        unlock = findViewById(R.id.unlock2);
        varCheckPair.put("Unlock_User",unlock );
        pauseNotifications = findViewById(R.id.pauseNotifications2);
        varCheckPair.put("Notification_Pause",pauseNotifications );
        resetOtherPass = findViewById(R.id.resetOtherPass2);
        varCheckPair.put("Customer_Reset_Password_Other", resetOtherPass);
        sensorViewChart = findViewById(R.id.sensorChart2);
        varCheckPair.put("Sensor_View_Chart", sensorViewChart);
        viewMaps = findViewById(R.id.viewMaps2);
        varCheckPair.put("Navigation_View_Maps", viewMaps);
        viewMyAccount = findViewById(R.id.viewMyAccount2);
        varCheckPair.put("Navigation_View_My_Account",viewMyAccount );
        viewNotifications = findViewById(R.id.viewNotifications2);
        varCheckPair.put("Sensor_View_Notifications",viewNotifications );
        viewReports = findViewById(R.id.viewReports2);
        varCheckPair.put("Navigation_View_Reports",viewReports );
        viewSensorHistory = findViewById(R.id.viewSensorHistory2);
        varCheckPair.put("Sensor_View_History",viewSensorHistory );


    }

    /*
     * fubar method to set the visibility programmatically because setting it through xml caused
     * problems and this seemed to fix it
     */
    private void setVisibility()
    {
        ackNotifications.setVisibility(View.GONE);
        addNetwork.setVisibility(View.GONE);
        calibrateSensor.setVisibility(View.GONE);
        delUser.setVisibility(View.GONE);
        disableNotifications.setVisibility(View.GONE);
        editAccount.setVisibility(View.GONE);
        editGateway.setVisibility(View.GONE);
        editNetwork.setVisibility(View.GONE);
        editNotifications.setVisibility(View.GONE);
        editUsers.setVisibility(View.GONE);
        editSelf.setVisibility(View.GONE);
        editSensor.setVisibility(View.GONE);
        editSensorMult.setVisibility(View.GONE);
        editSensorGroup.setVisibility(View.GONE);
        editUserNames.setVisibility(View.GONE);
        export.setVisibility(View.GONE);
        modifyMap.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);
        pauseNotifications.setVisibility(View.GONE);
        resetOtherPass.setVisibility(View.GONE);
        sensorViewChart.setVisibility(View.GONE);
        viewMaps.setVisibility(View.GONE);
        viewMyAccount.setVisibility(View.GONE);
        viewNotifications.setVisibility(View.GONE);
        viewReports.setVisibility(View.GONE);
        viewSensorHistory.setVisibility(View.GONE);

        Iterator iit = networkCheckBox.entrySet().iterator();
        while (iit.hasNext()) {
            Map.Entry pairr = (Map.Entry) iit.next();
            CheckBox checkk = (CheckBox) pairr.getValue();
            checkk.setVisibility(View.GONE);
        }
    }

    /*
     * fubar method to switch the checked value programmatically because setting it through xml
     * caused problems and this seemed to fix it by switching it and then switching it back
     * problems were that the boxes were checked but not shown as checked or shown as an empty box
     * with the checked
     */
    private void testChecked()
    {
        ackNotifications.setChecked(!ackNotifications.isChecked());
        addNetwork.setChecked(!addNetwork.isChecked());
        calibrateSensor.setChecked(!calibrateSensor.isChecked());
        delUser.setChecked(!delUser.isChecked());
        disableNotifications.setChecked(!disableNotifications.isChecked());
        editAccount.setChecked(!editAccount.isChecked());
        editGateway.setChecked(!editGateway.isChecked());
        editNetwork.setChecked(!editNetwork.isChecked());
        editNotifications.setChecked(!editNotifications.isChecked());
        editUsers.setChecked(!editUsers.isChecked());
        editSelf.setChecked(!editSelf.isChecked());
        editSensor.setChecked(!editSensor.isChecked());
        editSensorMult.setChecked(!editSensorMult.isChecked());
        editSensorGroup.setChecked(!editSensorGroup.isChecked());
        editUserNames.setChecked(!editUserNames.isChecked());
        export.setChecked(!export.isChecked());
        modifyMap.setChecked(!modifyMap.isChecked());
        unlock.setChecked(!unlock.isChecked());
        pauseNotifications.setChecked(!pauseNotifications.isChecked());
        resetOtherPass.setChecked(!resetOtherPass.isChecked());
        sensorViewChart.setChecked(!sensorViewChart.isChecked());
        viewMaps.setChecked(!viewMaps.isChecked());
        viewMyAccount.setChecked(!viewMyAccount.isChecked());
        viewNotifications.setChecked(!viewNotifications.isChecked());
        viewReports.setChecked(!viewReports.isChecked());
        viewSensorHistory.setChecked(!viewSensorHistory.isChecked());

        Iterator iit = networkCheckBox.entrySet().iterator();
        while (iit.hasNext()) {
            Map.Entry pairr = (Map.Entry) iit.next();
            CheckBox checkk = (CheckBox) pairr.getValue();
            checkk.setChecked(!checkk.isChecked());
        }
    }

    @Override
    /**
     * what do to when a menu item is selected
     * home button only, go back to the previous activity
     */
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
     * Standard method for the back button
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

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

    }

    /**
     * update the user details and the permissions if any changes have been made
     * calls AccountUserEdit and EditCustomerPermissions
     * @param view
     */
    public void updateUser(View view) {

        email.clearFocus();
        if(errorEmail.getVisibility() == View.VISIBLE)
        {
            email.requestFocus();
            return;
        }
        if(!nameChanged && !emailChanged && !checkBoxChange)
        {
            Toast.makeText(getApplicationContext(), "No changes have been made" , Toast.LENGTH_LONG).show();
            return;
        }else if(first == null || first.getText().length() == 0 || last == null || last.getText().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Names cannot be blank", Toast.LENGTH_LONG).show();
            return;
        }else if(emailChanged && !validEmail)
        {
            Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_LONG).show();
            return;
        }else
        {
            Toast.makeText(getApplicationContext(), "Updating user information", Toast.LENGTH_LONG).show();

            if(nameChanged || emailChanged)
            {
                String _firstName = first.getText().toString();
                String _lastName = last.getText().toString();
                String _email = email.getText().toString();

                //client
                AsyncHttpClient client = new AsyncHttpClient();
                prgDialog.show();
                //params
                RequestParams params = new RequestParams();
                params.put("userID", userID);
                params.put("FirstName", _firstName);
                params.put("LastName", _lastName);
                params.put("NotificationEmail", _email);

                client.get(base_url + "AccountUserEdit/" + authToken, params, new AsyncHttpResponseHandler() {

                    public void onSuccess(String response) {
                        prgDialog.hide();
                        try
                        {
                            JSONObject obj = new JSONObject(response);
                            Object item = obj.get("Result");

                            if(item instanceof String)
                            {
                                //Toast.makeText(getApplicationContext(), "Array of Errors", Toast.LENGTH_LONG).show();
                                String result = (String) item;
                                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                                return;

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
            if(checkBoxChange)
            {
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

                            }else
                            {
                                Toast.makeText(getApplicationContext(), "Permissions did not save. " + result, Toast.LENGTH_LONG).show();
                                return;
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
            prgDialog.dismiss();
            finish();
        }
    }

    /**
     * OnClick method when cancel button is hit
     * finishes the activity
     * @param view
     */
    public void cancelEdit(View view) {
        prgDialog.dismiss();
        finish();
    }
}
