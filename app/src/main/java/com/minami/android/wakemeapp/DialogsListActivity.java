package com.minami.android.wakemeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.minami.android.wakemeapp.Model.ChatRoom;
import com.minami.android.wakemeapp.Model.Dialog;
import com.minami.android.wakemeapp.Model.User;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.minami.android.wakemeapp.Config.Config.CURRENT_USER;
import static com.minami.android.wakemeapp.Controller.DBController.CHAT_ROOM_REF;
import static com.minami.android.wakemeapp.Controller.DBController.EMAIL;
import static com.minami.android.wakemeapp.Controller.DBController.FRIENDS_ID_LIST;
import static com.minami.android.wakemeapp.Controller.DBController.ID;
import static com.minami.android.wakemeapp.Controller.DBController.NAME;
import static com.minami.android.wakemeapp.Controller.DBController.USER_REF;

public class DialogsListActivity extends AppCompatActivity {
    private DialogsList dialogsListView;
    private static final String TAG = "DialogListActivity";
    private TextView friendNameTextView;
    private List<User> member;
    private HashSet<User> set;
    private DialogsListAdapter dialogsListAdapter;
    private EditText searchBox;
    private AlertDialog alertDialog;
    private ImageButton searchButton;
    private Button addButton;
    private ArrayList<User> currentUserHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);
        currentUserHolder = new ArrayList<>();
        member = new ArrayList<>();
        set = new HashSet<>();
        dialogsListView = findViewById(R.id.dialogsList);
        dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.get().load(url).into(imageView);
            }
        });
        dialogsListView.setAdapter(dialogsListAdapter);
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

    public void showFindFriendDialog(View view) {
        member.clear();
        set.clear();
        Log.i(TAG, "showFindFriendDialog: --------------->  press???");
        // build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.find_friend_dialog_layout, null);
        builder.setView(dialogView);
        // init
        searchBox = dialogView.findViewById(R.id.search_box);
        searchButton = dialogView.findViewById(R.id.search_by_email_button);
        addButton = dialogView.findViewById(R.id.add_button);
        addButton.setEnabled(false);
        friendNameTextView = dialogView.findViewById(R.id.friend_name_tv);
        alertDialog = builder.create();
        alertDialog.show();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(searchBox.getText().toString())){
                    toast("Please enter an email address");
                    return;
                }
                searchFriendByEmail(searchBox.getText().toString());
                Log.i(TAG, "onClick: ---------------------searchFriendByEmail");
            }
        });
    }


    public void searchFriendByEmail(final String email){
        member.clear();
        set.clear();
//        USER_REF.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()){
//                    if (userSnapshot.child(EMAIL).getValue().equals(email)){
//                        friendNameTextView.setText(userSnapshot.child(NAME).getValue().toString());
//                        // if already friends or not
//                        String mFriendId = userSnapshot.child(ID).getValue().toString();
//                        if (mFriendId.equals(CURRENT_USER.getUid())){
//                            toast("Search your friends");
//                            addButton.setEnabled(false);
//                        } else {
//                            User mFriend = userSnapshot.getValue(User.class);
//                            User currentUser = dataSnapshot.child(CURRENT_USER.getUid()).getValue(User.class);
//                            if (userSnapshot.child(FRIENDS_ID_LIST).getValue() != null) {
//                                if (!mFriend.getFriendsIdList().contains(CURRENT_USER.getUid())){
//                                    toast("new");
//                                    addButton.setEnabled(true);
//                                    addMember(mFriend);
//                                    addMember(currentUser);
//                                } else {
//                                    toast("Search new user");
//                                    addButton.setEnabled(false);
//                                }
//                            } else {
//                                toast("new");
//                                addButton.setEnabled(true);
//                                addMember(mFriend);
//                                addMember(currentUser);
//                            }
//                            break;
//                        }
//
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                if (databaseError != null){
//                    Log.e(TAG, "onCancelled: ", databaseError.toException() );
//                }
//            }
//        });
        USER_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()){
                    if (userSnapshot.child(EMAIL).getValue().equals(email)){
                        friendNameTextView.setText(userSnapshot.child(NAME).getValue().toString());
                        // if already friends or not
                        String mFriendId = userSnapshot.child(ID).getValue().toString();
                        if (mFriendId.equals(CURRENT_USER.getUid())){
                            toast("Search your friends");
                            addButton.setEnabled(false);
                        } else {
                            User mFriend = userSnapshot.getValue(User.class);
                            User currentUser = dataSnapshot.child(CURRENT_USER.getUid()).getValue(User.class);
                            if (userSnapshot.child(FRIENDS_ID_LIST).getValue() != null) {
                                if (!mFriend.getFriendsIdList().contains(CURRENT_USER.getUid())){
                                    toast("new");
                                    addButton.setEnabled(true);
                                    Log.i(TAG, "onDataChange: " + mFriendId);
                                    Log.i(TAG, "onDataChange: " + mFriend);
                                    Log.i(TAG, "onDataChange: " + currentUser);
                                    mFriend.addFriendToList(CURRENT_USER.getUid());
                                    currentUser.addFriendToList(mFriendId);
                                    member.add(mFriend);
                                    member.add(currentUser);
                                } else {
                                    toast("Search new user");
                                    addButton.setEnabled(false);
                                }
                            } else {
                                toast("new");
                                addButton.setEnabled(true);
                                Log.i(TAG, "onDataChange: " + mFriendId);
                                Log.i(TAG, "onDataChange: " + mFriend);
                                Log.i(TAG, "onDataChange: " + currentUser);
                                mFriend.addFriendToList(CURRENT_USER.getUid());
                                currentUser.addFriendToList(mFriendId);
                                member.add(mFriend);
                                member.add(currentUser);

                            }
                            break;
                        }

                    } else if (member.size() < 2){
                        // no match user
                        toast("No one is found");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (databaseError != null){
                    Log.e(TAG, "onCancelled: ", databaseError.toException() );
                }
            }
        });
        Log.i(TAG, "searchFriendByEmail: end of --------------------------->");
    }

    private void addMember(User user){
        if (!set.contains(user)){
            member.add(user);
            set.add(user);
        }
    }

    private void addCurrentUser(User user){
        currentUserHolder.clear();
        currentUserHolder.add(user);
    }

    private void toast(String msg) {
        Toast.makeText(
                DialogsListActivity.this,
                msg,
                Toast.LENGTH_SHORT).show();
    }

    public void createDialog(View view) {
//         no match user
//        if (TextUtils.isEmpty(friendNameTextView.getText().toString())) {
//            toast("No one is found");
//        }
//        Log.i(TAG, "createDialog: How are you?");
//        USER_REF.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.i(TAG, "onDataChange: Excuse me???");
//                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
//                    if (userSnapshot.child(EMAIL).getValue().equals(searchBox.getText().toString())) {
//                        String mFriendId = userSnapshot.child(ID).getValue().toString();
//                        Log.i(TAG, "onDataChange: " + mFriendId);
//                        User mFriend = userSnapshot.getValue(User.class);
//                        Log.i(TAG, "onDataChange: " + mFriend);
//                        User currentUser = dataSnapshot.child(CURRENT_USER.getUid()).getValue(User.class);
//                        Log.i(TAG, "onDataChange: " + currentUser);
//                        mFriend.addFriendToList(CURRENT_USER.getUid());
//                        currentUser.addFriendToList(mFriendId);
//                        member.add(mFriend);
//                        member.add(currentUser);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                if (databaseError != null){
//                    Log.e(TAG, "onCancelled: ", databaseError.toException() );
//                }
//            }
//        });
        updateFriendList();
        String chat_id = CHAT_ROOM_REF.push().getKey();
        ChatRoom mChatRoom = new ChatRoom(chat_id, member);
        CHAT_ROOM_REF.child(chat_id).setValue(mChatRoom);
        Dialog dialog = new Dialog(chat_id, member);
        dialogsListAdapter.addItem(dialog);
        dialogsListAdapter.notifyDataSetChanged();
        alertDialog.dismiss();
    }

    private void updateFriendList() {
        for (User user: member){
            Log.i(TAG, "updateFriendList: " + user.getFriendsIdList());
            USER_REF.child(user.getId()).child(FRIENDS_ID_LIST).setValue(user.getFriendsIdList(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    toast("Successfully added");
                }
            });
        }
        addButton.setEnabled(false);
    }
}
