package com.app.parkinglocator.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.parkinglocator.R;
import com.app.parkinglocator.callbacks.AlertGoToSettingsCallback;
import com.app.parkinglocator.callbacks.AlertSaveLocationCallback;
import com.app.parkinglocator.callbacks.DelayedCallback;
import com.app.parkinglocator.callbacks.LocationListOptionCallback;
import com.app.parkinglocator.callbacks.LocationSettingCallback;
import com.app.parkinglocator.helper.AppConstants;
import com.app.parkinglocator.views.ButtonBold;
import com.app.parkinglocator.views.EditTextRegular;
import com.app.parkinglocator.views.TextViewRegular;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AppUtils {

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getScreenWidth(AppCompatActivity mActivity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return width;
    }

    public static boolean checkGPSEnabled(Context ctx) {
        LocationManager service = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return enabled;
    }

    public static void showGPSSettings(final Activity mContext) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getResources().getString(R.string.track_location_title));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getResources().getString(R.string.track_location_text));

        // On pressing Settings button
        alertDialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog.cancel();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public static boolean checkIsConnectedToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static boolean checkLocationPermission(Context mContext) {
        String coarse_location_permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String fine_location_permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res_1 = mContext.checkCallingOrSelfPermission(coarse_location_permission);
        int res_2 = mContext.checkCallingOrSelfPermission(fine_location_permission);
        return (res_1 == PackageManager.PERMISSION_GRANTED && res_2 == PackageManager.PERMISSION_GRANTED);
    }

    public static void addMarkersToMap(GoogleMap mMap) {
    }

    public static void turnOnLocation(GoogleApiClient googleApiClient, final Activity mActivity) {
        if (googleApiClient != null) {
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        mActivity, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    public static void turnOnLocation(GoogleApiClient googleApiClient
            , final Activity mActivity, final LocationSettingCallback locationSettingCallback) {
        if (googleApiClient != null) {
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            locationSettingCallback.onSuccess();
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            locationSettingCallback.onFailed();
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        mActivity, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            locationSettingCallback.onNotAvailable();
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    public static void makeDelay(long timeMillis, final DelayedCallback delayedCallback) {
        Handler delayedHandler = new Handler();

        delayedHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayedCallback.onDelayCompleted();
            }
        }, timeMillis);
    }

    public static void saveLocationDialog(final Activity ctx, final AlertSaveLocationCallback alertSaveLocationCallback) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

        LayoutInflater inflater = ctx.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.round_dialog, null);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        final EditTextRegular input_location_name = dialogView.findViewById(R.id.input_location_name);
        TextViewRegular text_timestamp = dialogView.findViewById(R.id.text_timestamp);
        ButtonBold button_cancel = dialogView.findViewById(R.id.button_cancel);
        ButtonBold button_save = dialogView.findViewById(R.id.button_save);

        final long currentTimeMillis = System.currentTimeMillis();
        String currentTime = AppUtils.getFormattedDateFromMillies(currentTimeMillis);

        text_timestamp.setText(currentTime);

        //Ok Button
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location_title = input_location_name.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString(AppConstants.LOCATION_TITLE, location_title);
                bundle.putLong(AppConstants.LOCATION_TIME, currentTimeMillis);
                alertSaveLocationCallback.onSave(bundle);
                alertDialog.dismiss();
            }
        });

        //cancel Button
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSaveLocationCallback.onCancel();
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.round_dialog_bg));
        }

        alertDialog.show();
    }

    public static void customAlertDialog(final Activity ctx, final AlertGoToSettingsCallback alertGoToSettingsCallback) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

        LayoutInflater inflater = ctx.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.round_dialog_go_to_settings, null);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        ButtonBold button_cancel = dialogView.findViewById(R.id.button_cancel);
        ButtonBold button_ok = dialogView.findViewById(R.id.button_ok);
        //Ok Button
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertGoToSettingsCallback.onOkClick();
                alertDialog.dismiss();
            }
        });

        //cancel Button
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertGoToSettingsCallback.onCancelClick();
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.round_dialog_bg));
        }

        alertDialog.show();
    }

    public static String getDateFromMillies(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getFormattedDateFromMillies(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(milliSeconds);

        Date date = new Date(milliSeconds);

        return formatter.format(date);
    }

    public static void showAlert(Context context, String title, String message) {
        new AlertDialog.Builder(context)
//              .setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();
    }

    public static void printErrorLog(String TAG, String message) {
        Log.e(TAG, message);
    }

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static int convertDpToPx(Context mContext, int dp) {
        return Math.round(dp * (mContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    public static int convertPxToDp(Context mContext, int px) {
        return Math.round(px / (mContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static boolean isGoogleMapsInstalled(Context mContext) {
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void launchGoogleMapsNavitaion(Context mContext, double latitude, double longitude) {
        String navigationUrl = "google.navigation:q=" + latitude + "," + longitude;

        Uri gmmIntentUri = Uri.parse(navigationUrl);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(mapIntent);
    }

    public static void launchUrlShareIntent(Context mContext, String url) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        mContext.startActivity(Intent.createChooser(sharingIntent, mContext.getResources().getString(R.string.share_using)));
    }

    public static void showOptionsPopup(final Context mContext, final FloatingActionButton mFab) {
        //creating a popup menu
        PopupMenu popup = new PopupMenu(mContext, (mFab));
        //inflating menu from xml resource
        popup.inflate(R.menu.drawer_option_menu);

        popup.setGravity(Gravity.END);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_exit:
                        ((Activity) mContext).finish();
                        break;
                }
                return false;
            }
        });

        popup.show();
    }

    public static void showLocationListOptionsPopup(final Context mContext, final ImageView imageView,
                                                    final UUID locationId, final LocationListOptionCallback locationListOptionCallback) {
        //creating a popup menu
        PopupMenu popup = new PopupMenu(mContext, (imageView));
        //inflating menu from xml resource
        popup.inflate(R.menu.location_list_option_menu);

        popup.setGravity(Gravity.END);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        locationListOptionCallback.onDeleteOptionSelected(locationId);
                        break;
                }
                return false;
            }
        });

        popup.show();
    }

    public static void animateMarker(final GoogleMap mMap, final Marker marker, final LatLng toPosition,
                                     final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
// Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.loader_for_views);
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    public static void showToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void openInstagram(Context mContext, String url) {
        Uri uri = Uri.parse(url);
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            mContext.startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)));
        }
    }

    public static void openFacebook(Context mContext, String url) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(url)));
    }

    @SuppressLint("NewApi")
    public static void openWhatsapp(Context mContext, String phone) {
        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "");
            sendIntent.putExtra("jid", phone + "@s.whatsapp.net");
            sendIntent.setPackage("com.whatsapp");
            mContext.startActivity(sendIntent);
        } catch (Exception e) {
            printErrorLog("Error", e.toString());
        }
    }

    public static void rateUs(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        mContext.startActivity(intent);
    }

    public static void shareApp(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.share_app_subject,
                mContext.getResources().getString(R.string.app_name)));
        intent.putExtra(Intent.EXTRA_TEXT, mContext.getResources().getString(R.string.share_app_body,
                mContext.getResources().getString(R.string.app_name),
                "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
        mContext.startActivity(Intent.createChooser(intent, mContext.getResources().getString(R.string.share_title)));
    }

    public static void callEmergencyNumber(Context mContext) {
        try {
            Intent callIntent = new Intent("com.android.phone.EmergencyDialer.DIAL");
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            callIntent.setData(Uri.parse("tel:112")); // < Optional, will prompt number for user
            mContext.startActivity(callIntent);
        } catch (Exception e) {
            // in case something went wrong ...
        }
    }

    public static void sendSMS(Context mContext, String strMobileNo, String strMessage) {
        try {
            String phone_number = strMobileNo.replace(" ", "");
            String final_number = phone_number.replace("-", "");

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(final_number, null, strMessage, null, null);
            showToast(mContext, "Emergency alert sent.");
        } catch (Exception e) {
            e.printStackTrace();
            showToast(mContext, e.getMessage());
        }
    }
}