package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

/**
 * Activity to add a user to the parent account
 * User will be able to see networks assigned to them
 * Permission activity will start after this and user permissions will be selected
 */
public class AddUserActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, userID;
    private ProgressDialog prgDialog; //dialog
    // IDs to link with the layout
    private EditText userName, firstName, lastName, email, password, confirmPassword;
    private TextView errorUserName, errorFirstName, errorLastName, errorEmail, errorPassword, errorConfirmPassword;
    private CheckBox admin;
    private boolean validEmail = false;
    private boolean passwordMatch = false;
    private boolean emailChanged = false;

    @Override
    /**
     * Creates the activity and sets up the entry fields
     * Finds IDs
     * Sets up input listeners and watchers
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        getSupportActionBar().setTitle("ADD USER");
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
        }

        userName = findViewById(R.id.editUserName);
        firstName = findViewById(R.id.editFirstName);
        lastName = findViewById(R.id.editLastName);
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);
        confirmPassword = findViewById(R.id.editConfirmPassword);
        admin = findViewById(R.id.admin);
        email.addTextChangedListener(emailTextWatcher);

        errorUserName = findViewById(R.id.errorUserName);
        errorFirstName = findViewById(R.id.errorFirstName);
        errorLastName = findViewById(R.id.errorLastName);
        errorEmail = findViewById(R.id.errorEmail);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword = findViewById(R.id.errorConfirmPassword);

        admin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {

             @Override
             public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                 userName.clearFocus();
                 firstName.clearFocus();
                 lastName.clearFocus();
                 email.clearFocus();
                 password.clearFocus();
                 confirmPassword.clearFocus();

             }
         }
        );

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
        password.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    String p1 = password.getText().toString();
                    if(p1 != null && p1.length() < 8)
                    {
                        //Toast.makeText(getApplicationContext(), "Password must be at least 8 characters", Toast.LENGTH_LONG).show();
                        errorPassword.setVisibility(View.VISIBLE);
                    }else
                    {
                        errorPassword.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        confirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    String p1 = password.getText().toString();
                    String p2 = confirmPassword.getText().toString();

                    if(!p1.equals(p2))
                    {
                        //Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                        errorConfirmPassword.setVisibility(View.VISIBLE);
                        passwordMatch = false;
                    }else
                    {
                        errorConfirmPassword.setVisibility(View.INVISIBLE);
                        passwordMatch = true;
                    }
                }
            }
        });


    }



    /**
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
     * Add the new user to the account
     * Grab all the specific data, validate it, and add the user
     * If any information is incorrect, display an error message and return from this method
     * Calls "CreateAccountUser"
     * @param view
     */
    public void addUser(View view) {

        String _password =  password.getText().toString();
        String _confirmPassword = confirmPassword.getText().toString();
        String _userName = userName.getText().toString();
        String _firstName = firstName.getText().toString();
        String _lastName = lastName.getText().toString();
        String _email = email.getText().toString();
        String _admin = String.valueOf(admin.isChecked());
        boolean errorFlag = false;
        StringBuilder errorMsg = new StringBuilder();
        boolean errorFound = false;

        if(_userName == null || _userName.length() == 0)
        {
            //Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG).show();
//            userName.setHint("Required");
//            userName.setHintTextColor(Color.RED);
            errorUserName.setText("Required");
            errorUserName.setVisibility(View.VISIBLE);
            errorFlag = true;
            errorFound = true;
            userName.requestFocus();

        }
        if(_firstName == null || _firstName.length() == 0)
        {
            //Toast.makeText(getApplicationContext(), "Please enter a first name", Toast.LENGTH_LONG).show();
//            firstName.setHint("Required");
//            firstName.setHintTextColor(Color.RED);
            errorFirstName.setVisibility(View.VISIBLE);
            errorFlag = true;
            if(!errorFound)
            {
                errorFound = true;
                firstName.requestFocus();
            }
        }
        if(_lastName == null || _lastName.length() == 0)
        {
            //Toast.makeText(getApplicationContext(), "Please enter a last name", Toast.LENGTH_LONG).show();
//            lastName.setHint("Required");
//            lastName.setHintTextColor(Color.RED);
            errorLastName.setVisibility(View.VISIBLE);
            errorFlag = true;
            if(!errorFound)
            {
                errorFound = true;
                lastName.requestFocus();
            }
        }
        if(!Utility.validate(_email))
        {
//            Toast.makeText(getApplicationContext(), "Email is not the correct format", Toast.LENGTH_LONG).show();
//            errorMsg.append("Email is not the correct format\n");
//            if(_email != null && _email.length() != 0) {
//                email.setTextColor(Color.RED);
//            }
//            validEmail = false;
            errorEmail.setVisibility(View.VISIBLE);
            errorFlag = true;
            if(!errorFound)
            {
                errorFound = true;
                email.requestFocus();
            }
        }
        if(_password == null || _confirmPassword == null || _password.length() == 0 || _confirmPassword.length() == 0 )
        {
            errorPassword.setVisibility(View.VISIBLE);
            errorFlag = true;
            if(!errorFound)
            {
                errorFound = true;
                password.requestFocus();
            }
        }
        if(_password == null || _confirmPassword == null || !_password.equals(_confirmPassword))
        {
//            errorMsg.append("Passwords do not match\n");
//            passwordMatch = false;

            errorConfirmPassword.setVisibility(View.VISIBLE);
            errorFlag = true;
            if(!errorFound)
            {
                errorFound = true;
                confirmPassword.requestFocus();
            }
        }


        if(errorFlag)
        {
            return;
        }else
        {
            errorUserName.setVisibility(View.INVISIBLE);
            errorFirstName.setVisibility(View.INVISIBLE);
            errorLastName.setVisibility(View.INVISIBLE);
            errorEmail.setVisibility(View.INVISIBLE);
            errorConfirmPassword.setVisibility(View.INVISIBLE);
            errorPassword.setVisibility(View.INVISIBLE);

        }
        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();
        params.put("userName", _userName);
        params.put("firstName", _firstName);
        params.put("lastName", _lastName);
        params.put("password", _password);
        params.put("confirmPassword", _confirmPassword);
        params.put("notificationEmail", _email);
        params.put("isAdmin", _admin );

        client.get(base_url + "CreateAccountUser/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = new JSONObject(response);
                    Object item = obj.get("Result");

                    if(item instanceof JSONArray)
                    {
                        //Toast.makeText(getApplicationContext(), "Array of Errors", Toast.LENGTH_LONG).show();
                        JSONArray resultArray = (JSONArray) item;

                        for(int i = 0; i < resultArray.length(); i++)
                        {
                            JSONObject temp = resultArray.getJSONObject(i);
                            //String error = temp.getString("Error");
                            //Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            userName.requestFocus();
                            errorUserName.setText("Username not available");
                            errorUserName.setVisibility(View.VISIBLE);

                        }

                    }else
                    {
                        JSONObject result = (JSONObject) item;
                        String userID = result.getString("UserID");

                        //Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        finish();
                        startAddUserPermissionActivity(userID, admin.isChecked());
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
     * Start UserPermission activity
     * @param userID the ID of the new user, used for accessing permissions
     * @param admin true if they are an admin account
     */
    private void startAddUserPermissionActivity(String userID, boolean admin) {
        Intent intent = new Intent(getApplicationContext(), AddUserPermissionActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        intent.putExtra("isAdmin", admin);
        startActivity(intent);
    }

    /**
     * OnClick method to finish the activity when the cancel button is pressed
     * @param view
     */
    public void cancelAdd(View view) {
        prgDialog.dismiss();
        finish();
    }

    @Override
    /**
     * Finish the activity when the back arrow button is pressed
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
        finish();
    }


}
