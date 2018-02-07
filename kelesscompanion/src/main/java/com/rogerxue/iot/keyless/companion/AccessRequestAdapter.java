package com.rogerxue.iot.keyless.companion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.AccessRequest;

import java.util.ArrayList;
import java.util.List;

class AccessRequestAdapter extends ArrayAdapter<AccessRequest> {
    private static final String TAG = "AccessRequestAdapter";
    public AccessRequestAdapter(Context context, FirebaseFirestore firestore) {
        super(context, android.R.layout.simple_list_item_1);
        firestore.collection(AccessRequest.COLLECTION_NAME)
                .orderBy(AccessRequest.TIMESTAMP, Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            QuerySnapshot documentSnapshots,
                            FirebaseFirestoreException e) {
                        if (documentSnapshots == null) {
                            Log.d(TAG, "can't load request: " + e);
                            return;
                        }
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "no request to show");
                            return;
                        }
                        Log.d(TAG, "getting request: " + documentSnapshots.size());
                        clear();
                        addAll(toAccessRequest(documentSnapshots.getDocuments()));
                        notifyDataSetChanged();
                    }
                });
    }

    private List<AccessRequest> toAccessRequest(List<DocumentSnapshot> docs) {
        List<AccessRequest> requests = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            requests.add(doc.toObject(AccessRequest.class));
        }
        return requests;
    }
}
