package com.example.chatfunction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText InputPhoneNumber, InputUserVerificationCode;
    private Button SendVerificationCodeButton, VerifyButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();



        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputUserVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        loadingBar = new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number first", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, authenticating in process");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });


        //process where verification code has sent to the user
        VerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputUserVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code", Toast.LENGTH_SHORT).show();
                }

                else
                {

                    loadingBar.setTitle("Verification code");
                    loadingBar.setMessage("Please wait, verifying code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    //verification id to produce the code, user use the code to verify
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });



        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            //whenever verification is computed successfully from the code, allow user to login
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            //whenever the user provide invalid/wrong verification code
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Please enter correct phone number", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputUserVerificationCode.setVisibility(View.INVISIBLE);
            }



            //when code is sent to user phone
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputUserVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        //user has provided correct code
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }

                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }




    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


}




    /*
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);


        continueAndNextBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //continue button will become submit
                if(continueAndNextBtn.getText().equals("Submit") || checker.equals("Code Sent"))
                {
                    String verificationCode = codeText.getText().toString();

                    if(verificationCode.equals(""))
                    {
                        Toast.makeText(PhoneLoginActivity.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }

                //else statement will execute first for new user
                else
                {
                    //phoneNumber = ccp.getDefaultCountryCodeWithPlus()+phoneText.getText().toString();
                    //phoneNumber = ccp.getFullNumberWithPlus();

                    phoneNumber  = ccp.getFullNumberWithPlus();


                    //if not equals to null, send a call using firebase to user phone
                    if(!phoneNumber.equals(""))
                    {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        //https://firebase.google.com/docs/auth/android/phone-auth
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, PhoneLoginActivity.this, mCallbacks);
                    }
                    else
                    {
                        Toast.makeText(PhoneLoginActivity.this, "Please write valid phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            //if wrong verification
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            //if sim card in the same phone
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "You have logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    */






