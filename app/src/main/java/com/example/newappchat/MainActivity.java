package com.example.newappchat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //firebase
    FirebaseAuth firebaseAuth;

    //ActionBar
    ActionBar actionBar;

    //Bottom Nav
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //Action Bar
        actionBar =  getSupportActionBar();
        actionBar.setTitle("Profile");

        //bottom navigation
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment (default, on Start)
        actionBar.setTitle("Home"); //change actionBar title
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //handle clicks
            switch (item.getItemId()){
                case R.id.nav_home:
                    //home fragment
                    actionBar.setTitle("Home");
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1, "");
                    ft1.commit();
                    return true;
                case R.id.nav_profile:
                    //profile fragment
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment2 = new ProfileFragment();
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2,"");
                    ft2.commit();
                    return true;
                case R.id.nav_users:
                    //users fragment
                    actionBar.setTitle("Users");
                    UsersFragment fragment3 = new UsersFragment();
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content, fragment3,"");
                    ft3.commit();
                    return true;
                case R.id.nav_more:
                    //users fragment
                  showMoreOptions();
                    return true;
            }

            return false;
        }
    };


    private void showMoreOptions() {

        //popup menu to show more options
        PopupMenu popupMenu =  new PopupMenu(this, bottomNavigationView, Gravity.END);
        //item to show in menu
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Notifications");
        popupMenu.getMenu().add(Menu.NONE, 1, 0,  "Group Chats");

        //menu clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {
                    //notifications clicked

                    //Natifications fragment transaction
//                    actionBar.setTitle("Notification");
//                    NotificationFragment fragment5 = new NotificationFragment();
//                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
//                    ft5.replace(R.id.content, fragment5,"");
//                    ft5.commit();

                } else if (id == 1) {
                    //group chats clicked

                    //Group Chat fragment transaction
                    actionBar.setTitle("Group Chats");
                    GroupChatsFragment fragment6 = new GroupChatsFragment();
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content, fragment6,"");
                    ft6.commit();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus(){
        //get Current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            //user is signed in stay here
            //set Email of logged in user

        }
        else {
            //user not signed in, go to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    //ActionBar
    @Override
    protected void onStart(){
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

//    //inflate options Menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        //inflating menu
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    /*handle item clicks */
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        //get Item id
//        int id = item.getItemId();
//        if (id == R.id.action_logout) {
//            firebaseAuth.signOut();
//            checkUserStatus();
//        }
//        return super.onOptionsItemSelected(item);
//    }
//


}