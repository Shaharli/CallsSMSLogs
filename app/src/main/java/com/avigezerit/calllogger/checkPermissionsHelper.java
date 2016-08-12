package com.avigezerit.calllogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Shaharli on 12/08/2016.
 */
public class checkPermissionsHelper extends FragmentActivity {

    Context context;

    static Activity activity;

    //permissions
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    private final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG = 202;
    private final int MY_PERMISSIONS_REQUEST_READ_SMS_LOG = 303;
    private final int MY_PERMISSIONS_REQUEST_WRITE_TO_STORAGE = 404;
    private final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 505;

    public static boolean hasContactsPerm = false;
    public static boolean hasCallPerm = false;
    public static boolean hasSmsPerm = false;
    public static boolean hasStoragePerm = false;
    public static boolean hasStorageReadPerm = false;


    private static String permText;

    public checkPermissionsHelper(Context context) {
        this.context = context;
    }

    public void activitySet(Activity activity){
        this.activity = activity;
    }


    public void askPermission(String reqPerm){

        if (context.checkSelfPermission(reqPerm) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{reqPerm}, setReqCode(reqPerm));
        }
    }


    private int setReqCode(String reqPerm) {

        int reqCode = 0;

        switch (reqPerm) {

            case Manifest.permission.READ_CONTACTS:
                reqCode = MY_PERMISSIONS_REQUEST_READ_CONTACTS;
                permText = context.getResources().getString(R.string.contact_perm);
                break;
            case Manifest.permission.READ_CALL_LOG:
                reqCode = MY_PERMISSIONS_REQUEST_READ_CALL_LOG;
                permText = context.getResources().getString(R.string.calls_perm);
                break;
            case Manifest.permission.READ_SMS:
                reqCode = MY_PERMISSIONS_REQUEST_READ_SMS_LOG;
                permText = context.getResources().getString(R.string.sms_perm);
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                reqCode = MY_PERMISSIONS_REQUEST_WRITE_TO_STORAGE;
                permText = context.getResources().getString(R.string.write_storage_perm);
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                reqCode = MY_PERMISSIONS_REQUEST_READ_STORAGE;
                permText = context.getResources().getString(R.string.read_storage_perm);
                break;
        }

        return reqCode;

    }

    public void alarmUser() {

        String prefixToastPerm = context.getResources().getString(R.string.app_requires_permission);

        Toast.makeText(context, prefixToastPerm + permText, Toast.LENGTH_SHORT).show();
        Log.d("PERM", prefixToastPerm + permText);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasContactsPerm = true;

                } else {
                    hasContactsPerm = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_CALL_LOG: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCallPerm = true;

                } else {
                    hasCallPerm = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_SMS_LOG: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasSmsPerm = true;

                } else {
                    hasSmsPerm = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_TO_STORAGE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasStoragePerm = true;

                } else {
                    hasStoragePerm = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasStorageReadPerm = true;

                } else {
                    hasStorageReadPerm = false;
                }
                return;
            }
        }

    }
}


