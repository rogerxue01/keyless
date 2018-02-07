package com.rogerxue.iot.keyless.companion;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.AccessRequest;
import com.rogerxue.iot.keyless.lib.User;

class AccessPointAdapter extends ArrayAdapter<String> {
    private static final String TAG = "AccessPointAdapter";

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final FirebaseFirestore mFirestore;
    private final User mUser;

    public AccessPointAdapter(Context context, final FirebaseFirestore firestore, User user) {
        super(context, R.layout.access_point_item);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFirestore = firestore;
        mUser = user;
        clear();
        addAll(user.accessPointDayOfWeek.keySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.access_point_item, parent, false);
        } else {
            view = convertView;
        }
        final String accessPointString = getItem(position);
        // set trigger
        Button trigger = view.findViewById(R.id.trigger);
        trigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessRequest accessRequest =
                        new AccessRequest(accessPointString);
                mFirestore.collection(AccessRequest.COLLECTION_NAME)
                        .add(accessRequest.setAndroidId(mUser.getAndroidId()));
            }
        });
        trigger.setText(accessPointString);

        //set indicator
        final Button indicator = view.findViewById(R.id.indicator);
        if (indicator.getTag() != null) {
            ((ListenerRegistration)indicator.getTag()).remove();
        }
        ListenerRegistration registration = mFirestore
                .collection(AccessPoint.COLLECTION_NAME)
                .document(accessPointString)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(
                            DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot == null) {
                            Log.e(TAG, "no matching accessPoint " + accessPointString, e);
                            return;
                        }
                        AccessPoint accessPoint = documentSnapshot.toObject(AccessPoint.class);
                        indicator.setBackgroundColor(
                                accessPoint.getTriggered() ? Color.GREEN : Color.RED);
                    }
                });
        indicator.setTag(registration);
        return view;
    }
}
