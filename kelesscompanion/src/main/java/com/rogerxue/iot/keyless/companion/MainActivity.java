package com.rogerxue.iot.keyless.companion;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.User;

import java.util.List;


public class MainActivity extends Activity {
    private static final String TAG = "keyless_main";

    private BleScanner mBleScanner;
    private TextView mDeviceIdTextView;
    private ListView mRequestList;
    private Button mPopulateDataBtn;
    private ListView mAccessPointList;
    private FirebaseFirestore mFireStore;
    private String mAndroidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mBleScanner = new BleScanner();
        setContentView(R.layout.activity_main);
        mFireStore= FirebaseFirestore.getInstance();

        mDeviceIdTextView = findViewById(R.id.device_id);
        mPopulateDataBtn = findViewById(R.id.populate_data);

        mRequestList = findViewById(R.id.request_list);
        mRequestList.setAdapter(new AccessRequestAdapter(this, mFireStore));

        mAccessPointList = findViewById(R.id.access_point_list);

        mAndroidId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        mDeviceIdTextView.setText(mAndroidId);
        Log.d(TAG, "android id: " + mAndroidId);

        mFireStore.collection(User.COLLECTION_NAME).whereEqualTo(User.ANDROID_ID, mAndroidId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "no matching user " + mAndroidId);
                            return;
                        }
                        // should only have one.
                        List<DocumentSnapshot> doc = documentSnapshots.getDocuments();
                        if (doc.size() != 1) {
                            Log.d(TAG, "more than one matching user " + mAndroidId);
                            return;
                        }

                        User user = doc.get(0).toObject(User.class);
                        Log.d(TAG, "get user for this device: " + user);
                        if (user.getDeveloper()) {
                            mPopulateDataBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    populateData();
                                }
                            });
                            mPopulateDataBtn.setEnabled(true);
                        } else {
                            mPopulateDataBtn.setVisibility(View.INVISIBLE);
                            mPopulateDataBtn.setEnabled(false);
                        }
                        mAccessPointList.setAdapter(
                                new AccessPointAdapter(MainActivity.this, mFireStore, user));
                    }
                });
    }

    // for testing:
    private void populateData() {
        User user = new User(
                "roger",
                "38745a18cd4ea902",
                "129734");
        user.setDeveloper(true);
        grantUserPermission(user);

        user = new User(
                "shasha",
                "92fc7a8c310bdbf7",
                "155309");
        grantUserPermission(user);

        user = new User(
                "user1",
                "",
                "3722");
        grantUserPermission(user);

        user = new User(
                "user2",
                "",
                "3701");
        grantUserPermission(user);

        user = new User(
                "user3",
                "",
                "3719");
        grantUserPermission(user);

        user = new User(
                "user4",
                "",
                "3724");
        grantUserPermission(user);

        user = new User(
                "user5",
                "",
                "3717");
        grantUserPermission(user);

        user = new User(
                "gardener",
                "",
                "3708");
        user.grantAccess(AccessPoint.WEST_YARD_GATE, true, true, true, true, true, true, true);
        user.grantAccess(AccessPoint.EAST_YARD_GATE, true, true, true, true, true, true, true);

        mFireStore.collection(User.COLLECTION_NAME).document(user.getName()).set(user);

        AccessPoint accessPoint = new AccessPoint(
                AccessPoint.FRONT_DOOR, "7594ab098eceabf3", "", true, 2000);
        mFireStore.collection(AccessPoint.COLLECTION_NAME).document(accessPoint.getName())
                .set(accessPoint);
        accessPoint = new AccessPoint(
                AccessPoint.WEST_YARD_GATE, "ce8b2321d70aaaac", "", false, 1000);
        mFireStore.collection(AccessPoint.COLLECTION_NAME).document(accessPoint.getName())
                .set(accessPoint);
        accessPoint = new AccessPoint(
                AccessPoint.GARAGE_DOOR, "2e65d788476ed3f8", "", false, 300);
        mFireStore.collection(AccessPoint.COLLECTION_NAME).document(accessPoint.getName())
                .set(accessPoint);
        accessPoint = new AccessPoint(
                AccessPoint.EAST_YARD_GATE, "", "", false, 1000);
        mFireStore.collection(AccessPoint.COLLECTION_NAME).document(accessPoint.getName())
                .set(accessPoint);
    }

    private void grantUserPermission(User user) {
        user.grantAccess(AccessPoint.FRONT_DOOR, true, true, true, true, true, true, true);
        user.grantAccess(AccessPoint.WEST_YARD_GATE, true, true, true, true, true, true, true);
        user.grantAccess(AccessPoint.GARAGE_DOOR, true, true, true, true, true, true, true);
        user.grantAccess(AccessPoint.EAST_YARD_GATE, true, true, true, true, true, true, true);

        mFireStore.collection(User.COLLECTION_NAME).document(user.getName()).set(user);
    }
}
