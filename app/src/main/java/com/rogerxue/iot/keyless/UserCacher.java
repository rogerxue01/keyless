package com.rogerxue.iot.keyless;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rogerxue on 10/14/17.
 */

public class UserCacher {
    private static final String TAG = "UserCacher";

    private final FirebaseFirestore mFirestore;
    private final List<User> mUsers = new ArrayList<>();

    public UserCacher(FirebaseFirestore firestore) {
        mFirestore = firestore;

        mFirestore.collection(User.COLLECTION_NAME).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "There's error loading database", task.getException());
                    return;
                }
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    mUsers.add(doc.toObject(User.class));
                }
            }
        });
    }

    public User getUser(String field, String code) {
        for (User user : mUsers) {
            if (field.equals(User.ANDROID_ID) && user.getAndroidId().equals(code)) {
                return user;
            }
            if (field.equals(User.HID_CODE) && user.getHidCode().equals(code)) {
                return user;
            }
        }
        return null;
    }
}
