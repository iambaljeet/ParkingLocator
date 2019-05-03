package com.app.parkinglocator.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.app.parkinglocator.R;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.helper.SharedPreferenceHelper;
import com.app.parkinglocator.utility.AppUtils;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private FloatingActionButton mBackFab;
    private FloatingActionButton mSaveFab;
    private RelativeLayout mOptionsContainerTop;
    private AppCompatCheckBox mNotificationsCheckbox;

    private AppCompatActivity mActivity;
    private int status_bar_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        mActivity = this;

        mBackFab = findViewById(R.id.fab_back);
        mBackFab.setOnClickListener(this);
        mSaveFab = findViewById(R.id.fab_save);
        mSaveFab.setOnClickListener(this);
        mOptionsContainerTop = findViewById(R.id.top_options_container);
        mOptionsContainerTop.setOnClickListener(this);
        ;
        mNotificationsCheckbox = findViewById(R.id.checkbox_notifications);

        status_bar_height = AppUtils.getStatusBarHeight(mActivity);

        ViewGroup.MarginLayoutParams userlayoutParams =
                (ViewGroup.MarginLayoutParams) mOptionsContainerTop.getLayoutParams();
        userlayoutParams.setMargins(0, status_bar_height, 0, 0);
        mOptionsContainerTop.requestLayout();

        loadUserSettings();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_back:
                finish();
                break;
            case R.id.fab_save:
                saveUserSettings();
                break;
            case R.id.top_options_container:
                break;
            default:
                break;
        }
    }

    private void loadUserSettings() {
        if (SharedPreferenceHelper.contains(AppConstants.SHOW_NOTIFICATION)) {
            mNotificationsCheckbox.setChecked(SharedPreferenceHelper.getBoolean(AppConstants.SHOW_NOTIFICATION, true));
        }
    }

    private void saveUserSettings() {
        SharedPreferenceHelper.saveBoolean(AppConstants.SHOW_NOTIFICATION, mNotificationsCheckbox.isChecked());

        AppUtils.printErrorLog(TAG, "mNotificationsCheckbox.isChecked(): " + mNotificationsCheckbox.isChecked());
        AppUtils.showAlert(mActivity, "Settings", "Settings saved successfully.");
    }

}
