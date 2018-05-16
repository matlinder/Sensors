package com.mathew.sensorlogin;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private ProgressDialog prgDialog; //dialog
    private String authToken; //authorization token from json
    private String userName; //the users username for json calls
    private String userID; //the user id linked to the username, for json calls
    private TextView name; //temp placeholder to display the account json data
    private TextView email, username, password, passwordView, confirmPasswordView, confirmPassword, errorPassword, errorConfirmPassword;
    // transition container
    private ViewGroup transitionsContainer;
    private Button saveButton;


    /**
     * Create the account acitivity to display the user account details
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("ACCOUNT DETAILS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_account);

        name = findViewById(R.id.nameTag);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.editPassword);
        passwordView = findViewById(R.id.newPassView);
        confirmPasswordView = findViewById(R.id.confirmPassView);
        confirmPassword = findViewById(R.id.editConfirmPassword);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword= findViewById(R.id.errorConfirmPassword);
        transitionsContainer = findViewById(R.id.accountLayout);
        saveButton = findViewById(R.id.changePassword);

        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userName = extras.getString("userName");
            userID = extras.getString("userID");
            //Toast.makeText(getApplicationContext(), "user id = " + userID, Toast.LENGTH_LONG).show();
        }
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        displayData();
    }

    /**
     * menu options back button
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * menu options buttons
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * method to display the user data after a successful login
     */
    public void displayData()
    {
        AsyncHttpClient client = new AsyncHttpClient(); //create the client
        RequestParams params = new RequestParams(); //create a place to store the json params
        params.put("userID", userID);

        //json request
        client.get(base_url + "AccountUserGet/" + authToken, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            public void onSuccess(String response) {

                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // Get the data of the user
                    JSONObject result = obj.getJSONObject("Result");
                    // assign the data to the fields so that it is displayed
                    username.setText(result.getString("UserName"));
                    name.setText(String.format("%s %s", result.getString("FirstName"), result.getString("LastName")));
                    email.setText(String.format("%s", result.getString("EmailAddress")));


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error occurred could not display data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            public void onFailure(int statusCode, Throwable error,
                                  String content) {

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
     * Onclick method to change the users password
     * Validates the 2 text fields to != null and >=8 characters
     * If valid, will call UpdatePassword
     * @param view
     */
    public void changePassword(View view) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        if(password.getVisibility() == view.GONE) {
            password.setVisibility(View.VISIBLE);
            passwordView.setVisibility(View.VISIBLE);
            confirmPasswordView.setVisibility(View.VISIBLE);
            confirmPassword.setVisibility(View.VISIBLE);
            errorPassword.setVisibility(View.INVISIBLE);
            errorConfirmPassword.setVisibility(View.INVISIBLE);
            saveButton.setText("Save Changes");

            return;
        }

        String _password = password.getText().toString();
        String _confirmPassword = confirmPassword.getText().toString();

        if(_password == null || _confirmPassword == null || _password.length() == 0 || _confirmPassword.length() == 0 )
        {
            errorPassword.setVisibility(View.VISIBLE);
            password.requestFocus();
            return;
        }
        if(_password == null || _confirmPassword == null || !_password.equals(_confirmPassword))
        {
            errorConfirmPassword.setVisibility(View.VISIBLE);
            confirmPassword.requestFocus();
            return;

        }

        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();

        params.put("NewPassword", _password);

        client.get(base_url + "UpdatePassword/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = new JSONObject(response);
                    String result = obj.getString("Result");
                    if(result.equals("Success")) {
                        TransitionManager.beginDelayedTransition(transitionsContainer);
                        password.setVisibility(View.GONE);
                        passwordView.setVisibility(View.GONE);
                        confirmPasswordView.setVisibility(View.GONE);
                        confirmPassword.setVisibility(View.GONE);
                        errorPassword.setVisibility(View.GONE);
                        errorConfirmPassword.setVisibility(View.GONE);
                        saveButton.setText("Change Password");
                        Toast.makeText(getApplicationContext(), "Your password has been changed!", Toast.LENGTH_LONG).show();

                    }else
                    {
                        Toast.makeText(getApplicationContext(), "Your password was not changed, please try again", Toast.LENGTH_LONG).show();
                        errorPassword.setVisibility(View.INVISIBLE);
                        errorConfirmPassword.setVisibility(View.INVISIBLE);

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
}
