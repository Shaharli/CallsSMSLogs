package com.avigezerit.callssmslogs;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shaharli on 09/08/2016.
 */
public class mCursorAdapter extends CursorAdapter {

    private static int entriesData;

    public mCursorAdapter(Context context, Cursor c) {
        super(context, c);

    }

    public void setEntriesData(int reqID) {
        entriesData = reqID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View v = LayoutInflater.from(context).inflate(R.layout.entry_list_item, null);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView entryMainTV = (TextView) view.findViewById(R.id.entryMainTV);
        TextView entrySecondTV = (TextView) view.findViewById(R.id.entrySecondTV);
        ImageView entryIconTV = (ImageView) view.findViewById(R.id.entryIconIV);

        switch (entriesData) {
            case LogsFragment.MY_CALLS_LOG_ID:

                //int columns
                int cType = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int date = cursor.getColumnIndex(CallLog.Calls.DATE);
                int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

                //extracted string
                String callType = cursor.getString(cType);

                int dur = cursor.getInt(duration);
                long durMili = dur * 1000;
                String durString = calculateDuration(durMili);

                //date formatting
                String callDateInfo = cursor.getString(date);
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                String dateString = formatter.format(new Date(Long.parseLong(callDateInfo)));

                //check for incoming - outgoing - missed

                if (callType==null){
                    entryIconTV.setVisibility(View.INVISIBLE);
                } else
                switch (callType) {
                    case "1":
                        entryIconTV.setImageResource(R.drawable.ic_incoming_call);
                        break;
                    case "2":
                        entryIconTV.setImageResource(R.drawable.ic_outgoing_call);
                        break;
                    case "3":
                        entryIconTV.setImageResource(R.drawable.ic_missed_call);
                        break;
                }

                //bind to views
                entryMainTV.setText(durString);
                entrySecondTV.setText(dateString);

                break;

            case LogsFragment.MY_SMS_LOG_ID:

                //int columns
                int smsDate = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.DATE);
                int body = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.BODY);
                int sType = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.TYPE);

                //extracted string
                String smsBody = cursor.getString(body);
                int smsType = cursor.getInt(sType);

                //date formatting
                String smsDateInfo = cursor.getString(smsDate);
                SimpleDateFormat smsDateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                String smsDateString = smsDateFormatter.format(new Date(Long.parseLong(smsDateInfo)));

                //check if incoming - outgoing
                switch (smsType) {

                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX:
                        Log.d("", ""+Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
                        entryIconTV.setImageResource(R.drawable.ic_incoming_sms);
                        break;

                    case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                        entryIconTV.setImageResource(R.drawable.ic_outgoing_sms);
                        break;
                }

                //bind to views
                entryMainTV.setText(smsBody);
                entrySecondTV.setText(smsDateString);

                break;
        }

    }

    private String calculateDuration(long timeInMillis){

         int hours = (int) ((timeInMillis / (1000 * 60 * 60)));
         int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
         int seconds = (int) ((timeInMillis / 1000) % 60);

        String duration = "";
        if(hours>0)
            duration += hours+":";
        //duration += minutes+":";
        if(minutes < 10)
            duration += "0"+minutes+":";
        else
            duration += ""+minutes+":";

        if(seconds < 10)
            duration += "0"+seconds;
        else
            duration += ""+seconds;

        return duration;
    }
}
