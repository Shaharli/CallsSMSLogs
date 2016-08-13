package com.avigezerit.callssmslogs;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class LogsFragment extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener {

    //list init
    ArrayList<String> logsList;
    ListView logsLV;
    mCursorAdapter adapter;

    //permissions
    checkPermissionsHelper cp;

    //context constructor init
    private static Context c;

    //loader id for content provider
    public static final int MY_CALLS_LOG_ID = 1;
    public static final int MY_SMS_LOG_ID = 2;

    //no entries result case
    TextView noEntriesTV;

    String sName;

    //btns
    Button smsBtn;
    Button callsBtn;

    public static LogsFragment newInstance(Context context) {
        c = context;
        LogsFragment logsFragment = new LogsFragment();
        return logsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logs, container, false);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mFileSavedReceiver, new IntentFilter("com.avigezerit.logger.FILE_SAVED"));

        cp = new checkPermissionsHelper(getActivity());
        cp.activitySet(getActivity());

        //read and set contact name
        TextView ContactNameTV = (TextView) v.findViewById(R.id.ContactNameTV);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        sName = preferences.getString("name", "");
        ContactNameTV.setText(sName);

        //list init
        Cursor c = null;
        logsLV = (ListView) v.findViewById(R.id.LogsLV);
        logsList = new ArrayList<String>();
        adapter = new mCursorAdapter(getActivity(), c);
        logsLV.setAdapter(adapter);

        getLogsEntriesFromLoader(MY_CALLS_LOG_ID);

        //set click btns
        smsBtn = (Button) v.findViewById(R.id.smsBtn);
        smsBtn.setOnClickListener(this);

        callsBtn = (Button) v.findViewById(R.id.callsBtn);
        callsBtn.setOnClickListener(this);

        noEntriesTV = (TextView) v.findViewById(R.id.noEntriesTV);

        return v;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;
        Uri uri;

        //queries
        switch (id) {
            case MY_CALLS_LOG_ID:

                if (getActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

                    uri = CallLog.Calls.CONTENT_URI;
                    String[] number = new String[]{args.getString("number")};
                    cursorLoader = new CursorLoader(c, uri, null, CallLog.Calls.NUMBER + "=?", number, null);

                } else {
                    cp.askPermission(Manifest.permission.READ_CALL_LOG);
                    cp.alarmUser();
                }


                break;

            case MY_SMS_LOG_ID:

                if (getActivity().checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

                    uri = Telephony.Sms.CONTENT_URI;
                    String[] smsNumber = new String[]{args.getString("number")};
                    cursorLoader = new CursorLoader(c, uri, null, Telephony.TextBasedSmsColumns.ADDRESS + "=?", smsNumber, null);
                } else {
                    cp.askPermission(Manifest.permission.READ_SMS);
                    cp.alarmUser();
                }

                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        Cursor c = (Cursor) data;

        //no entries result case
        if (c.getCount() != 0) {
            noEntriesTV.setVisibility(View.INVISIBLE);
        }

        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.callsBtn:
                getLogsEntriesFromLoader(MY_CALLS_LOG_ID);
                callsBtn.setTextColor(getResources().getColor(R.color.colorAccent));
                smsBtn.setTextColor(getResources().getColor(R.color.grey));
                break;

            case R.id.smsBtn:
                getLogsEntriesFromLoader(MY_SMS_LOG_ID);
                smsBtn.setTextColor(getResources().getColor(R.color.colorAccent));
                callsBtn.setTextColor(getResources().getColor(R.color.grey));
                break;
        }
    }

    private void getLogsEntriesFromLoader(int reqID) {

        adapter.setEntriesData(reqID);

        LoaderManager manager = getLoaderManager();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Bundle args = new Bundle();
        args.putString("number", preferences.getString("number", ""));
        manager.initLoader(reqID, args, LogsFragment.this);

    }

    private void saveDataToFile(Cursor[] cursors) {

        cp.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Handler handler = new Handler();
            saveToFileTask task = new saveToFileTask();
            task.setData(cursors, getActivity());
            handler.post(task);

        } else cp.alarmUser();

    }

    public void savingBtnClicked() {

        cp.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        cp.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        Cursor[] cursors = new Cursor[2];

        Uri CallsUri = CallLog.Calls.CONTENT_URI;
        Uri SmsUri = Telephony.Sms.CONTENT_URI;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String[] number = new String[]{preferences.getString("number", "")};


        if (getActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            cursors[0] = getActivity().getContentResolver().query(CallsUri, null, CallLog.Calls.NUMBER + "=?", number, null);
            cursors[1] = getActivity().getContentResolver().query(SmsUri, null, Telephony.TextBasedSmsColumns.ADDRESS + "=?", number, null);

            saveDataToFile(cursors);


        } else cp.alarmUser();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        cp.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            savingBtnClicked();

        }
    }

    private BroadcastReceiver mFileSavedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {

            String status = intent.getStringExtra("status");
            final String fileName = intent.getStringExtra("file_name");

            if (status.equals("saved")) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set title
                alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_title_saved_file));

                // set dialog message
                alertDialogBuilder.setMessage(getResources().getString(R.string.saved_file_want_to_share))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.dialog_yes_btn), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                    emailIntent.setType("text/*");
                                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Calls & SMS Logs File");
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, "My Calls & SMS Logs : "+sName);

                                    File file = saveToFileTask.getSavedFile();

                                    if (!file.exists() || !file.canRead()) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ fileName));
                                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.email_this)));
                                }

                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_no_btn), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

        }

    };

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mFileSavedReceiver);
        super.onDestroyView();
    }
}



