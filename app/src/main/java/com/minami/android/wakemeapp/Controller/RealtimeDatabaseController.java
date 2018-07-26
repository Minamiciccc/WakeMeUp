package com.minami.android.wakemeapp.Controller;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minami.android.wakemeapp.Model.User;

import java.util.List;

/**
 * Created by Minami on 2018/07/21.
 */

public class RealtimeDatabaseController {
    private static final String TAG = "RealtimeDBController";
    private static String id;
    private static final String USERS = "Users";
    private static final String MESSAGES = "Messages";
    private static final String CHAT_ROOMS = "ChatRooms";
    public static final DatabaseReference USER_REF = FirebaseDatabase.getInstance().getReference(USERS);
    public static final DatabaseReference MESSAGE_REF = FirebaseDatabase.getInstance().getReference(MESSAGES);
    public static final DatabaseReference CHAT_ROOM_REF = FirebaseDatabase.getInstance().getReference(CHAT_ROOMS);

    public static void addUserToRealtimeDB(final FirebaseUser USER, final String ID) {
        USER_REF.child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    RealtimeDatabaseController.writeNewUser(ID, USER.getDisplayName(), USER.getEmail());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(databaseError != null) {
                    Log.e(TAG, "onCancelled: ", databaseError.toException());
                }
            }
        });
    }

    public static void writeNewUser(String id, String name, String email) {
        User newUser = new User(id, name, email);
        USER_REF.child(id).setValue(newUser);
    }

    public static void addFriendById(final List<User> member, String id) {
        USER_REF.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                member.add(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(databaseError != null) {
                    Log.e(TAG, "onCancelled: ", databaseError.toException());
                }
            }
        });
    }

}
