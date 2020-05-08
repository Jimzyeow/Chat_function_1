package com.example.chatfunction;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

   private Toolbar mToolbar;
   private ViewPager myViewPager;
   private TabLayout myTabLayout;
   private TabsAccessorAdapter myTabsAccessorAdapter;

   private FirebaseUser currentUser;
   private FirebaseAuth mAuth;
   private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

       mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
       setSupportActionBar(mToolbar);
       getSupportActionBar().setTitle("Let's Study");

       myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
       // FragmentManager fm = null;
        // int behavior = 0;
      //myTabsAccessorAdapter = new TabsAccessorAdapter(fm, behavior);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }



    @Override
    protected void onStart()
    {
        super.onStart(); //whenever the app just started

        if(currentUser == null) //if the user is not logged in
        {
            SendUserToLoginActivity(); //send user to login page, user must log in first
        }

        else
        {
            VerifyUserExistence();
        }

    }



    /*
    if name is available meaning user is not a new user, he/she is an old user
    if user is a new user, he will be directed to settings activity
     */
    private void VerifyUserExistence()
    {
        String currentUserID = mAuth.getCurrentUser().getUid();
        //check the parent node in database then subchild
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //check if the child/username is available, meaning it is a existing user
                if ((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome",Toast.LENGTH_SHORT).show();
                }
                //if user has not sign up, it means he has not set the profile settings
                //profile picture is optional so does not need any validation
                //only used for user name
                else
                {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        if(item.getItemId() == R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }

        if(item.getItemId() == R.id.main_create_group_option)
        {
            RequestNewGroup();
        }

        if(item.getItemId() == R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }

        return true;
    }



    private void RequestNewGroup()
    {
        //use alert dialog, ask user to enter group name and store in database
        //retrieve in group fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Classroom Parents");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please set name for the group", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();

    }



    private void CreateNewGroup(final String groupName)
    {
        //group name will be a key value
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupName + " is created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }



    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        //user cannot go back to login activity if they click on the back button until they have provide name or profile picture
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }



    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

}
