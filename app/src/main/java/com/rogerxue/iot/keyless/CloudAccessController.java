package com.rogerxue.iot.keyless;

import android.util.Log;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.AccessRequest;

import java.util.Date;
import java.util.List;

/**
 * Processes cloud access request.
 */
public class CloudAccessController {
    private static final String TAG = "CloudAccessController";

    // 10 mins
    private static final long LATENCY_TOLERANCE_MS = 10 * 60 * 1000;

    private final FirebaseFirestore mFirestore;
    private final MainActivity.AccessCallback mAccessCallback;
    private final UserAccessController mUserAccessController;

    private CollectionReference mRequestReference;

    public CloudAccessController(
            MainActivity.AccessCallback accessCallback, AccessPoint accessPoint) {
        mAccessCallback = accessCallback;
        mFirestore = FirebaseFirestore.getInstance();
        mUserAccessController = new UserAccessController(mAccessCallback, accessPoint);
        mRequestReference = mFirestore.collection(AccessRequest.COLLECTION_NAME);
        mRequestReference
                .whereEqualTo(AccessRequest.PROCESSED, false)
                .whereEqualTo(AccessRequest.ACCESS_POINT, accessPoint.getName())
                .orderBy(AccessRequest.TIMESTAMP, Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "There's error loading database", e);
                            return;
                        }
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "no new request");
                            return;
                        }
                        List<DocumentChange> docs = documentSnapshots.getDocumentChanges();

                        for (DocumentChange doc : docs) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                AccessRequest accessRequest = doc.getDocument()
                                        .toObject(AccessRequest.class);
                                doc.getDocument().getReference()
                                        .update(AccessRequest.PROCESSED, true);

                                Log.d(TAG, "new entry in db: " + accessRequest.toString());

                                Date timestamp = accessRequest.getTimestamp();
                                if (timestamp == null) {
                                    Log.w(TAG, "timestamp is null");
                                    return;
                                }
                                if (System.currentTimeMillis() - timestamp.getTime()
                                        < LATENCY_TOLERANCE_MS) {
                                    mUserAccessController.validateCode(
                                            AccessRequest.ANDROID_ID, accessRequest.getAndroidId());
                                } else {
                                    Log.d(TAG, "old request: " + System.currentTimeMillis()
                                            + " request time: " + timestamp);
                                }
                            }
                        }
                    }
                });
    }
}
