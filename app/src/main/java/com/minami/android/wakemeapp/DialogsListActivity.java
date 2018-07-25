package com.minami.android.wakemeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.minami.android.wakemeapp.Controller.RealtimeDatabaseController;
import com.minami.android.wakemeapp.Model.Dialog;
import com.minami.android.wakemeapp.Model.User;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;

import static com.minami.android.wakemeapp.Controller.RealtimeDatabaseController.USER_REF;

public class DialogsListActivity extends AppCompatActivity implements View.OnClickListener{
    private int dialogs;
    private DialogsList dialogsList;
    private static final String TAG = "DialogListActivity";
    private TextView friendNameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);
        User minami = new User("373", "minami");
        ArrayList<User> member = new ArrayList<>();
        member.add(minami);
        Dialog dialog = new Dialog("123456789", member);
        dialogs = R.id.dialogsList;
        dialogsList = findViewById(dialogs);
        DialogsListAdapter dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.get().load(url).into(imageView);
            }
        });
//        DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<>(dialogs, new ImageLoader() {
//            @Override
//            public void loadImage(ImageView imageView, String url) {
//                //If you using another library - write here your way to load image
//                Picasso.get().load(url).into(imageView);
//            }
//        });
        dialogsListAdapter.addItem(dialog);

        dialogsList.setAdapter(dialogsListAdapter);

        FloatingActionButton findFriendButton = findViewById(R.id.find_friend_button);
        findFriendButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // logout
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        launchLoginActivity();
                    }
                });
        return true;
    }


    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchMessagesListActivity() {
        Intent intent = new Intent(this, MessagesListActivity.class);
        startActivity(intent);
        finish();
    }

    public void searchUserByEmail(final String EMAIL){
        USER_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()){
                    if (EMAIL.equals(userSnapshot.child("email").getValue().toString())){
                        friendNameTextView.setText(userSnapshot.child("name").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: ", databaseError.toException());
            }
        });
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "showFindFriendDialog: --------------->  press???");
        // build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.find_friend_dialog_layout, null);
        builder.setView(dialogView);
        // init
        final EditText searchBox = dialogView.findViewById(R.id.search_box);
        ImageButton searchButton = dialogView.findViewById(R.id.search_by_email_button);
        Button addButton = dialogView.findViewById(R.id.add_button);
        friendNameTextView = dialogView.findViewById(R.id.friend_name_tv);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(searchBox.getText().toString())){
                    return;
                }
                searchUserByEmail(searchBox.getText().toString());
            }
        });
        if (TextUtils.isEmpty(friendNameTextView.getText().toString())){
            return;
        }
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: ------------- add friend");
            }
        });


    }
}
