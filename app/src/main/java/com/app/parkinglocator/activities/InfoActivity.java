package com.app.parkinglocator.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.utility.AppUtils;
import com.app.parkinglocator.views.TextViewBold;
import com.app.parkinglocator.views.TextViewRegular;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = InfoActivity.class.getSimpleName();
    String email;
    String whatsapp;
    String skype;
    String facebook;
    String instagram;
    private FloatingActionButton mBackFab;
    private FloatingActionButton mOptionsFab;
    private RelativeLayout mOptionsContainerTop;
    private TextViewBold mHeadingEmail;
    private TextViewRegular mAddressEmail;
    private RelativeLayout mContainerEmail;
    private TextViewBold mHeadingWhatsapp;
    private TextViewRegular mNumberContact;
    private CardView mContainerWhatsapp;
    private TextViewBold mHeadingSkype;
    private TextViewRegular mSkype;
    private CardView mContainerSkype;
    private ImageButton mIconFacebook;
    private ImageButton mIconInstagram;
    private AppCompatActivity mActivity;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        initView();
    }

    private void initView() {
        mActivity = this;

        databaseReference = FirebaseDatabase.getInstance(getString(R.string.database_url)).getReference();

        mBackFab = findViewById(R.id.fab_back);
        mBackFab.setOnClickListener(this);
        mOptionsFab = findViewById(R.id.fab_options);
        mOptionsFab.setOnClickListener(this);
        mOptionsContainerTop = findViewById(R.id.top_options_container);
        mHeadingEmail = findViewById(R.id.email_heading);
        mAddressEmail = findViewById(R.id.email_address);
        mContainerEmail = findViewById(R.id.email_container);
        mContainerEmail.setOnClickListener(this);
        mHeadingWhatsapp = findViewById(R.id.whatsapp_heading);
        mNumberContact = findViewById(R.id.contact_number);
        mContainerWhatsapp = findViewById(R.id.whatsapp_container);
        mContainerWhatsapp.setOnClickListener(this);
        mHeadingSkype = findViewById(R.id.skype_heading);
        mSkype = findViewById(R.id.skype);
        mContainerSkype = findViewById(R.id.skype_container);
        mContainerSkype.setOnClickListener(this);
        mIconFacebook = findViewById(R.id.facebook_icon);
        mIconFacebook.setOnClickListener(this);
        mIconInstagram = findViewById(R.id.instagram_icon);
        mIconInstagram.setOnClickListener(this);

        int status_bar_height = AppUtils.getStatusBarHeight(mActivity);

        ViewGroup.MarginLayoutParams userlayoutParams =
                (ViewGroup.MarginLayoutParams) mOptionsContainerTop.getLayoutParams();
        userlayoutParams.setMargins(0, status_bar_height, 0, 0);
        mOptionsContainerTop.requestLayout();

        getData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_back:
                finish();
                break;
            case R.id.fab_options:
                // TODO 18/08/11
                break;
            case R.id.email_container:
                // TODO 18/08/11
                break;
            case R.id.whatsapp_container:
                AppUtils.openWhatsapp(mActivity, whatsapp);
                break;
            case R.id.skype_container:
                // TODO 18/08/11
                break;
            case R.id.facebook_icon:
                AppUtils.openFacebook(mActivity, facebook);
                break;
            case R.id.instagram_icon:
                AppUtils.openInstagram(mActivity, instagram);
                break;
            default:
                break;
        }
    }

    private void getData() {
        final ProgressDialog progressDialog = AppUtils.createProgressDialog(mActivity);
        progressDialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AppUtils.printErrorLog(TAG, "dataSnapshot: " + dataSnapshot);

                if (dataSnapshot.getValue() != null) {
                    AppUtils.printErrorLog(TAG, "dataSnapshot: " + dataSnapshot.getValue());

                    email = dataSnapshot.child("email").getValue().toString();
                    whatsapp = dataSnapshot.child("whatsapp").getValue().toString();
                    skype = dataSnapshot.child("skype").getValue().toString();
                    facebook = dataSnapshot.child("facebook").getValue().toString();
                    instagram = dataSnapshot.child("instagram").getValue().toString();

                    mAddressEmail.setText(email);
                    mNumberContact.setText(whatsapp);
                    mSkype.setText(skype);

                } else {
                    AppUtils.showToast(mActivity, getString(R.string.something_went_wrong_message));
                    finish();
                }

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                AppUtils.printErrorLog(TAG, "databaseError: " + databaseError.getMessage());
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                AppUtils.showToast(mActivity, getString(R.string.something_went_wrong_message));
                finish();
            }
        });
    }
}
