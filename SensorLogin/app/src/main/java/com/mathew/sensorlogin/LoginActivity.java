package com.mathew.sensorlogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String PREF_NAME = "prefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Progress Dialog Object
    ProgressDialog prgDialog;
    // checkbox for saving login details
    CheckBox check;
    // Username Edit View Object
    EditText userNameET;
    // Password Edit View Object
    EditText pwdET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().setTitle("CMMET LOGIN");
        setContentView(R.layout.activity_login);



        // Find Email Edit View control by ID
        userNameET =  findViewById(R.id.loginEmail);
        // Find Password Edit View control by ID
        pwdET =  findViewById(R.id.loginPassword);
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        // find the checkbox
        check = findViewById(R.id.checkBox);

        // Shared preferences to see if the user saved their login information
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if(preferences.getBoolean(KEY_REMEMBER, false))
        {
            check.setChecked(true);
            RequestParams params = new RequestParams();
            // Put Http parameter username with value of Email Edit View control
            params.put("username", preferences.getString(KEY_USERNAME, ""));
            // Put Http parameter password with value of Password Edit Value control
            params.put("password", preferences.getString(KEY_PASS, ""));
            userNameET.setText(preferences.getString(KEY_USERNAME,""));
            pwdET.setText(preferences.getString(KEY_PASS,""));
            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }

    }

    /**
     * Method gets triggered when Login button is clicked
     *
     * @param view
     */
    public void loginUser(View view) {
        // Get Email Edit View Value
        String userName = userNameET.getText().toString();
        // Get Password Edit View Value
        String password = pwdET.getText().toString();
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(userName) && Utility.isNotNull(password)) {
            // When Email entered is Valid

                // Put Http parameter username with value of Email Edit View control
                params.put("username", userName);
                // Put Http parameter password with value of Password Edit Value control
                params.put("password", password);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            else {
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://www.imonnit.com/json/GetAuthToken", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    String authToken = obj.getString("Result");
                    validateLogon(authToken);



                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'

            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
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
    }
    /**
     * Method that performs RESTful webservice invocations
     *
     * @param authToken the authorization token to be used for all json calls
     */
    public void validateLogon(final String authToken) {
        // Show Progress Dialog
        prgDialog.show();
        //Toast.makeText(getApplicationContext(), "validating token", Toast.LENGTH_LONG).show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://www.imonnit.com/json/Logon/" + authToken, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    String result = obj.getString("Result");
                    if(result.equals("Success"))
                    {
                        //save the user data
                        if(check.isChecked() && !preferences.getBoolean(KEY_REMEMBER, false))
                        {
                            preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            // Get Email Edit View Value
                            String userName = userNameET.getText().toString();
                            // Get Password Edit View Value
                            String password = pwdET.getText().toString();

                            editor = preferences.edit();
                            editor.putBoolean(KEY_REMEMBER, true);
                            editor.putString(KEY_USERNAME, userName);
                            editor.putString(KEY_PASS, password);
                            editor.putString(KEY_TOKEN, authToken);
                            editor.apply();

                        }

                        navigatetoHomeActivity(authToken);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Login failed, please try again", Toast.LENGTH_LONG).show();
                    }



                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }

            }

            // When the response returned by REST has Http response code other than '200'

            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
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
    }
    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoHomeActivity(String token){
        Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
        //homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.putExtra("token", token);
        homeIntent.putExtra("userName", userNameET.getText().toString());
        startActivity(homeIntent);
    }
}




