package com.rogerxue.iot.keyless;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.User;
import com.rogerxue.iot.keyless.lib.AccessPoint;

import java.util.List;

/**
 * Verifies access permission for user
 */
class UserAccessController {
    private static final String TAG = "UserAccessController";

    private final MainActivity.AccessCallback mAccessCallback;
    private final AccessPoint mAccessPoint;
    private FirebaseFirestore mFirestore;
    private final UserCacher mUserCacher;
    private final Handler mHandler = new Handler(Looper.myLooper());

    UserAccessController(
            MainActivity.AccessCallback accessCallback,
            AccessPoint accessPoint) {
        mAccessCallback = accessCallback;
        mAccessPoint = accessPoint;

        mFirestore = FirebaseFirestore.getInstance();
        mUserCacher = new UserCacher(mFirestore);
    }

    public void validateCode(final String field, final String code) {
        final Process process = new Process();
        final OnCompleteListener<QuerySnapshot> listener = new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (process.processed) {
                    Log.d(TAG, "processed by cached user.");
                    return;
                }
                if (!task.isSuccessful()) {
                    Log.e(TAG, "There's error loading database", task.getException());
                    return;
                }
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.isEmpty()) {
                    Log.d(TAG, "no matching user " + code);
                    return;
                }
                // should only have one.
                List<DocumentSnapshot> doc = snapshot.getDocuments();
                if (doc.size() != 1) {
                    Log.d(TAG, "more than one matching user " + code);
                    return;
                }
                User user = doc.get(0).toObject(User.class);
                checkAccessPermission(user, code);
                process.processed = true;
            }
        };

        Task<QuerySnapshot> task = mFirestore.collection(User.COLLECTION_NAME)
                .whereEqualTo(field, code).get();
        task.addOnCompleteListener(listener);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!process.processed) {
                    Log.d(TAG, "not yet processed, using cached user.");
                    checkAccessPermission(mUserCacher.getUser(field, code), code);
                    process.processed = true;
                }
            }
        }, 5000);
    }

    private class Process {
        boolean processed = false;
    }

    private void checkAccessPermission(User user, String code) {
        if (user != null) {
            Log.d(TAG, "existing code: " + code);
            if (user.isGrantedNow(mAccessPoint)) {
                mAccessCallback.grantAccess();
            }
        }  else {
            Log.w(TAG, "invalid code: " + code);
        }
    }
}
