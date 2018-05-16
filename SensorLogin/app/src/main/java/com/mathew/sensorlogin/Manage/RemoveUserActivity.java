package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mathew.sensorlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Activity that allows the current user to select a user and remove that account
 */
public class RemoveUserActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken;
    private ProgressDialog prgDialog; //dialog
    private LinearLayout listLayout;
    private ArrayList<String> userIDs = new ArrayList<String>();

    @Override
    /**
     * Creates the activity and displays all the user accounts for the parent account
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_user);
        getSupportActionBar().setTitle("REMOVE USER");
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

        listLayout = findViewById(R.id.userRemoveList);
        populateUserList();
    }

    /**
     * Grabs all the users from the parent account and displays them in table format
     * User is able to click on a user and it will prompt for deletion
     */
    public void populateUserList()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "AccountUserList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempUser = objArray.getJSONObject(i);
                        String ID = tempUser.getString("UserID");
                        String first = tempUser.getString("FirstName");
                        String last = tempUser.getString("LastName");
                        final String name = first + " " + last;
                        String email = tempUser.getString("EmailAddress");

                        userIDs.add(ID);
                        final TextView item = new TextView(getApplicationContext());
                        item.setTextSize(18);
                        item.setId(i);
                        item.setTextColor(Color.BLACK);
                        item.setPadding(15,8,15,15);
                        item.setClickable(true);
                        if(i % 2 == 0){
                            item.setBackgroundColor(Color.LTGRAY);
                        }
                        item.setText(name + " " + email);
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int id = item.getId();
                                String tempID = userIDs.get(id);
                                removeUserPrompt(tempID, name);

                            }
                        });
                        listLayout.addView(item);
                    }


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

    /*
     * Have the user confirm the remove action. If the user selects ok, removeUser is called
     */
    private void removeUserPrompt(final String tempID, final String name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(RemoveUserActivity.this);
        alert.setTitle("Remove User");
        alert.setMessage("Are you sure you want to remove " + name + "?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                removeUser(tempID, name);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /*
     * Removes the selected user from the parent account
     * Calls AccountUserDelete
     */
    private void removeUser(String tempID, final String name)
    {
        if(tempID == null)
        {
            Toast.makeText(getApplicationContext(), name + " ID cannot be null", Toast.LENGTH_LONG).show();
            return;
        }
        //add the params to a RequestParams object
        //these will be used in the request
        RequestParams params = new RequestParams();
        params.put("userID", tempID);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "AccountUserDelete/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    String result = obj.getString("Result");

                    if(result.equalsIgnoreCase("Success"))
                    {
                        Toast.makeText(getApplicationContext(), name + " was removed", Toast.LENGTH_LONG).show();
                        listLayout.removeAllViews();
                        populateUserList();
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "Something went wrong, user was not removed", Toast.LENGTH_LONG).show();
                    }



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
     * OnClick method to call when the cancel button is pressed
     * Finishes the activity
     * @param view
     */
    public void cancelRemove(View view) {
        super.finish();
        prgDialog.dismiss();
    }

}
