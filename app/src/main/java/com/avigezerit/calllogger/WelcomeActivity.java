package com.avigezerit.calllogger;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare
    static final int PICK_CONTACT = 1;

    //selected Contact
    String sId;
    String sName;
    String sNumber;

    checkPermissionsHelper cp;

    Button startBtn;
    TextView brieffTV;

    //permissions
    Button contactPermBtn;
    Button callsPermBtn;
    Button smsPermBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        cp = new checkPermissionsHelper(this);
        cp.activitySet(WelcomeActivity.this);

        startBtn = (Button) findViewById(R.id.pickContactBtn);
        contactPermBtn = (Button) findViewById(R.id.contactPermBtn);
        callsPermBtn = (Button) findViewById(R.id.callsPermBtn);
        smsPermBtn = (Button) findViewById(R.id.smsPermBtn);

        startBtn.setOnClickListener(this);
        contactPermBtn.setOnClickListener(this);
        callsPermBtn.setOnClickListener(this);
        smsPermBtn.setOnClickListener(this);

        brieffTV = (TextView) findViewById(R.id.ExpTV);

        checkIfSecondTimeUser();

        if (getIntent().getStringExtra("choose_contact_again")!=null){
            getContact();
        }

    }

    private void checkIfSecondTimeUser(){

        if (checkForAllPermmissions()){
            callsPermBtn.setVisibility(View.INVISIBLE);
            contactPermBtn.setVisibility(View.INVISIBLE);
            smsPermBtn.setVisibility(View.INVISIBLE);

            startBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            startBtn.setEnabled(true);

            brieffTV.setText(getResources().getString(R.string.app_brieff));

        }
    }

    private boolean checkForAllPermmissions() {

        if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && this.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && this.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            //all permissions granted
            return true;
        } else return false;
    }

    private void colorGrantedPermissionsBtns(String perm, Button btn){

        if (this.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED){

            btn.setBackgroundColor(getResources().getColor(R.color.pressed_color));

        }

    }


    //code
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                        //get data from chosen contact
                        Uri contactData = data.getData();
                        Cursor c = managedQuery(contactData, null, null, null, null);
                        if (c.moveToFirst()) {

                            sId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                            if (hasPhone.equalsIgnoreCase("1")) {

                                Cursor phones = getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + sId,
                                        null, null);
                                phones.moveToFirst();
                                sNumber = phones.getString(phones.getColumnIndex("data1"));
                                Log.d("", "number is:" + sNumber);
                            }
                            sName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                            Log.d("", "name is:" + sName);


                            //save to shared pref
                            if (sName != null && sNumber != null) {

                                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("name", sName);
                                editor.putString("number", sNumber);
                                editor.putString("id", sId);
                                editor.commit();

                                //go to logs
                                Intent intent = new Intent(WelcomeActivity.this, LogsInfoActivity.class);
                                startActivity(intent);

                            }

                        }
                    } else cp.alarmUser();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        cp.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //color btn
        colorGrantedPermissionsBtns(Manifest.permission.READ_CONTACTS, contactPermBtn);
        colorGrantedPermissionsBtns(Manifest.permission.READ_CALL_LOG, callsPermBtn);
        colorGrantedPermissionsBtns(Manifest.permission.READ_SMS, smsPermBtn);

        if (checkForAllPermmissions()){
            startBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            startBtn.setEnabled(true);
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.contactPermBtn:
                cp.askPermission(Manifest.permission.READ_CONTACTS);
                break;
            case R.id.callsPermBtn:
                cp.askPermission(Manifest.permission.READ_CALL_LOG);
                break;
            case R.id.smsPermBtn:
                cp.askPermission(Manifest.permission.READ_SMS);
                break;
            case R.id.pickContactBtn:
                if (!checkForAllPermmissions()) {
                    Toast.makeText(this, getResources().getString(R.string.grant_permissions), Toast.LENGTH_SHORT).show();
                } else {
                    getContact();
                }
                break;
        }
    }

    public void getContact(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

}
