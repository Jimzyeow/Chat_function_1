package com.example.chatfunction;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity
{
    private String receiverUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //get need to be the same as FindFriendsActivity
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();

        Toast.makeText(this, "User ID: " + receiverUserID, Toast.LENGTH_SHORT).show();
    }
}
