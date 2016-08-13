package com.avigezerit.callssmslogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Shaharli on 12/08/2016.
 */
public class saveToFileTask implements Runnable {

    //Cursor cursor;
    Cursor[] cursors;
    FileWriter fw;
    Context context;
    static File FileToSave;

    public void setData(Cursor[] cursors, Context context) {
        this.cursors = cursors;
        this.context = context;
    }


    @Override
    public void run() {

        Cursor callsCursor = cursors[0];
        Cursor smsCursor = cursors[1];

        //calls log file
        StringBuilder sb = new StringBuilder();
        sb.append("\n//////////////////////////////////////////////////\n");
        sb.append("Call Log :");

        int number = callsCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = callsCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = callsCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = callsCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (callsCursor.moveToNext()) {

            String phNum = callsCursor.getString(number);
            String callTypeCode = callsCursor.getString(type);
            String strcallDate = callsCursor.getString(date);
            Date callDate = new Date(Long.valueOf(strcallDate));

            int dur = callsCursor.getInt(duration);
            long durMili = dur * 1000;
            String durString = calculateDuration(durMili);

            String callType = null;
            int callcode = Integer.parseInt(callTypeCode);

            switch (callcode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callType = "Missed";
                    break;
            }

            sb.append("\nPhone Number:--- " + phNum
                    + " \nCall Type:--- " + callType
                    + " \nCall Date:--- " + callDate
                    + " \nCall Duration :--- " + durString);
            sb.append("\n----------------------------------");

        }

        //sms log file
        sb.append("\n//////////////////////////////////////////////////\n");
        sb.append("Sms Log :");

        int address = smsCursor.getColumnIndex(Telephony.Sms.ADDRESS);
        int body = smsCursor.getColumnIndex(Telephony.Sms.BODY);
        int smsDate = smsCursor.getColumnIndex(Telephony.Sms.DATE);
        int smsType = smsCursor.getColumnIndex(Telephony.Sms.TYPE);

        while (smsCursor.moveToNext()) {

            String phNum = smsCursor.getString(address);
            String smsTypeCode = smsCursor.getString(smsType);
            String strSmsDate = smsCursor.getString(smsDate);
            Date smsDateFormat = new Date(Long.valueOf(strSmsDate));
            String smsBody = smsCursor.getString(body);
            String smsCodeType = null;
            int smsCode = Integer.parseInt(smsTypeCode);

            switch (smsCode) {
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                    smsCodeType = "Outgoing";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX:
                    smsCodeType = "Incoming";
                    break;
            }

            sb.append("\nPhone Number:--- " + phNum
                    + " \nSMS Type:--- " + smsCodeType
                    + " \nSMS Date:--- " + smsDateFormat
                    + " \nSMS Body :--- " + smsBody);
            sb.append("\n----------------------------------");


        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = preferences.getString("name", "");

        writeFile(sb, "Calls & SMS : "+name);

        callsCursor.close();
        smsCursor.close();

    }

    private void writeFile(StringBuilder sb, String fileName) {

        try {
            FileToSave = new File(Environment.getExternalStorageDirectory()+"/"+fileName+".txt");
            fw = new FileWriter(FileToSave);
            Log.d("TXT", sb.toString());
            fw.write(sb.toString());
            setFileToShare(FileToSave);
            fw.close();

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        Intent intent = new Intent("com.avigezerit.logger.FILE_SAVED");
        intent.putExtra("status", "saved");

        String savedFileName = Environment.getExternalStorageDirectory()+"/"+fileName+".txt";
        intent.putExtra("file_name", savedFileName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


    }

    private void setFileToShare(File f){
        f = FileToSave;
    }

    public static File getSavedFile(){
        return FileToSave;
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

