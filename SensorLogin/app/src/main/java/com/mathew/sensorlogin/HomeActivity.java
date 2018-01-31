package com.mathew.sensorlogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    String authToken;
    String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); //displays the home screen




        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userName = extras.getString("userName");
        }



    }

    /**
     * starts the account details activity
     * passes the token via extra
     */
    public void displayAccountDetails(View view)
    {
        Intent intent = new Intent(HomeActivity.this, AccountDetailsActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }



}
