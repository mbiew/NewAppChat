package com.example.newappchat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.HashMap;

public class  GroupChatActivity extends AppCompatActivity {

    private String groupId, myGroupRole="";
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbarr;
    private ImageButton attachBtn, sendBtn;
    private ImageView groupIconIv;
    TextView groupTitleTv;
    EditText messageEt;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    private ActionBar actionBar;

    //permission request constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    //image pick co  nstants
    private static final int IMAGE_PICK_GALERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1000;
    //permissions to be requrested
    private String[] cameraPermission;
    private String[] storagePermission;
    //uri of picked image
    private Uri image_uri = null;
//    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //init views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);
        attachBtn = findViewById(R.id.attachBtn);

        toolbarr = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarr);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

       //init required permissions
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        LoadGroupInfo();
        LoadGroupMessages();
        LoadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //inputData
                String message = messageEt.getText().toString().trim();
                //validate
                if (TextUtils.isEmpty(message)){
                    //empty, dont send
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //send message
                    sendMessage(message);
                }
            }

        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image from camera/gallery
                showImageImportDialog();
            }
        });
        
    }

    private void showImageImportDialog() {
        //options to display
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle clicks
                if (i == 0) {
                    //camera Clicked
                    if (!checkCameraPermission()) {
                        //not granted, request
                        requestCameraPermission();
                    }
                    else {
                        //already granted
                        pickCamera();
                    }
                }
                else {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        //not granted, request
                        requestStoragePermission();
                    }
                    else{
                        //already granted
                        pickGalery();
                    }
                }

            }
        }).show();

    }

    private void pickGalery(){
        //intent to pick iamge from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALERY_CODE);
    }

    private void pickCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageDescription");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void sendImageMessages(){
        //progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Sending Image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        //file name and pendImrolog
    String filenamePart = "ChatImages/" + ""+System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePart);

    //upload image
    storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            //image uploaded, getUrl
            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!p_uriTask.isSuccessful());
            Uri p_downloadUri = p_uriTask.getResult();

            if (p_uriTask.isSuccessful()){
                //image url received, save in DB

                //timestamp
                String timestamp = ""+System.currentTimeMillis();

                //setup message data
                HashMap<String, Object> hashMap =new HashMap<>();
                hashMap.put("sender", ""+ firebaseAuth.getUid());
                hashMap.put("message", ""+ p_downloadUri);
                hashMap.put("timestamp", ""+ timestamp);
                hashMap.put("type", ""+ "text");     //text / image / file

                //add in DB
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Messages").child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //message sent
                                //clearMessageEt
                                messageEt.setText("");
                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                //message sending failed
                                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            //failed uuploading iamge
            Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT );
            progressDialog.dismiss();
        }
    });

    }

    private void  LoadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    myGroupRole = ""+ds.child("role").getValue();

                    //refresh menu items
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void LoadGroupMessages() {
        //init list
        groupChatList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                    groupChatList.add(model);
                }
                //adapter
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                //set to RecyclerView
                chatRv.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(final String message) {

        //timestamp
        String timestamp = ""+System.currentTimeMillis();

        //setup message data
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("sender", ""+ firebaseAuth.getUid());
        hashMap.put("message", ""+ message);
        hashMap.put("timestamp", ""+ timestamp);
        hashMap.put("type", ""+ "text");     //text / image / file

        //add in DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        //clearMessageEt
                        messageEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //message sending failed
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void LoadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();

                    groupTitleTv.setText(groupTitle);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_black).into(groupIconIv);
                    } catch (Exception e) {
                        groupIconIv.setImageResource(R.drawable.ic_group_black);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin") ){
            //im admin/creator, show add person options
            menu.findItem(R.id.action_add_participant_group).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_add_participant_group).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_participant_group) {
            Intent intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        else if (id == R.id.action_groupinfo) {
            Intent intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //picked from gallery
                image_uri = data.getData();
                sendImageMessages();
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    }
                    else Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writedStorageAccpted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writedStorageAccpted) {
                        pickGalery();
                    }
                    else {
                        Toast.makeText(this, "Storage permission reuqird...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }


//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}