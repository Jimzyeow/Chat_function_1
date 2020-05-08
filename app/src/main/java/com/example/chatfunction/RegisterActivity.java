package com.example.chatfunction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText userEmail, userPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef; //referencing to firebase database which is the unique code

    private ProgressDialog loadingBar; //progress indicator with optional text message or view



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();


        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CreateNewAccount();
            }
        });
    }



    //user create new account
    private void CreateNewAccount()
    {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        //returns true if string is null or 0-length
        if (TextUtils.isEmpty(email)) //if email is empty
        {
            //Toast provide simple feedback about an operation in small popup
            //3 parameters, application'contect', text, duration
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)) //if password is empty
        {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }


        else //both email and password are creating
        {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait, while we are creating new account for you");
            //set whether dialog is canceled when touched outside the window's bound. If true, dialog is cancelable
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            //create user account using firebase auth
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {


                    if(task.isSuccessful()) //if account is created and authenticated successfully
                    {
                        //if account created successfully, store user unique id in firebase database
                        String currentUserID = mAuth.getCurrentUser().getUid(); //get current user ID
                        RootRef.child("Users").child(currentUserID).setValue("");

                       SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account created successful", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        //mAuth.signOut();
                    }

                    else
                    {
                        //returns the exception thrown by the base computation
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        //mAuth.signOut();
                    }
                }
            });
        }
    }


    //link to the id in register XML file
    private void InitializeFields()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.exist_account);

        loadingBar = new ProgressDialog(this);
    }


    //if clicked, send user from register activity to login activity
    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }



    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        //user cannot go back if they click on the back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
