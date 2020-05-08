package com.example.chatfunction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    private String photoUrl = " ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        //create a folder in firebase storage with the name "User Profile Images"
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("User Profile Images");


        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
              UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*"); //select file type user will want to select
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }




    private void InitializeFields()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            //can set aspect ratio for x & y axis
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //to get the cropped image
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait while profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                Uri resultUri = result.getUri(); //contain the cropped image
                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg"); //store image using current user

                //another method
                 final String downloadUrl = resultUri.toString();
                 photoUrl = downloadUrl;

               // final UploadTask uploadTask = filePath.putFile(resultUri); //alternative

                //alternative solution
                //checkout difference between addOnSuccessListener and addOnCompleteListener
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            //get the link of images from firebase storage
                            String downloadedUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadedUrl).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    //images stored inside firebase
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SettingsActivity.this, "Image saved in Database successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        //   Glide.with(SettingsActivity.this).load(downloadedUrl).into(userProfileImage); //using glide dependency
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }

                        //image not stored inside firebase
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
        }
    }
    }



    /* MAIN SOLUTION
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            //get the link of images from firebase storage
                         String downloadedUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadedUrl).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    //images stored inside firebase
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SettingsActivity.this, "Image saved in Database successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                     //   Glide.with(SettingsActivity.this).load(downloadedUrl).into(userProfileImage); //using glide dependency
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }

                        //image not stored inside firebase
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                }); */




    /* OTHER SOLUTION
     final Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                    LoadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                    LoadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
     */




    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this,"Please set your username", Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this,"Please set your status", Toast.LENGTH_SHORT).show();
        }

        else
        {
            HashMap<String, Object> profileMap = new HashMap<>(); //change to object in the second parameter for alternative method
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName); //has to be the same name as database snapshot in Mainactivity
            profileMap.put("status", setStatus);
            profileMap.put("image",photoUrl); //add ons

            /*another method
            if(!TextUtils.isEmpty(photoUrl))
            {
                profileMap.put("image", photoUrl);
            }

             */


            RootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }

                }

            });

        }
    }


    //MAIN SOLUTION
    /*
    RootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }

                }

            });
     */

    /*
     //alternative solution
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile successfuly updated...", Toast.LENGTH_LONG).show();
                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
     */


   private void RetrieveUserInfo()
    {
        //retrieving old user profile using user ID
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //means if user has created an account and using ID exist in database under "Users"
                //checking for the name and profile image
                //if true, will show profile picture, username and status
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && dataSnapshot.hasChild("image"))
                {

                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    //retrieve the image stored in firebase storage and store inside this variable
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);

                    //alternative solution
                    photoUrl = retrieveProfileImage;

                    /*
                    need to set 'path', which is retrieveProfileImage as the link to database is
                    stored inside this variable.
                     */
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                 //   Glide.with(SettingsActivity.this).load(retrieveProfileImage).into(userProfileImage); //from Glide dependency
                }

                //profile image is optional
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                {
                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);

                }

                //if none of these exist
                else
                {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please set and update your profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




    //once done will send user to main page
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        //user cannot go back to login activity if they click on the back button when they are in main activity currently
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
