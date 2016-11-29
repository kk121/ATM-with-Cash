package com.paisewalaatm.paisewalaatm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by krishna on 14/11/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {
    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    private Context context;
    private GPSTracker gps;

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
        final Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.d(TAG, "onReceive: bundle is null");
            return;
        }
        this.context = context;
        gps = new GPSTracker(context);
        String bankAtmName = "";
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    if (phoneNumber.length() == 9 && phoneNumber.charAt(2) == '-') {
                        Pattern atmRegExp
                                = Pattern.compile("[^a-zA-Z0-9]ATM|atm|NFS|nfs[^a-zA-Z0-9]");
                        Pattern rsRegExp = Pattern.compile("(Rs|rs|INR|₹)\\s?[0-9]*");
                        Pattern accRegExp = Pattern.compile("(Ac|AC|ac|a\\/c|A\\/c)\\s([x|X]+[0-9]{4})");
                        Pattern atmNameRegExp = Pattern.compile("(at|AT|At)\\s([a-zA-Z]{3}[\\s])");
                        Log.i(TAG, "onReceive: 1" + atmRegExp.matcher(message).find() + rsRegExp.matcher(message).find() + accRegExp.matcher(message).find());
                        if (atmRegExp.matcher(message).find()
                                && rsRegExp.matcher(message).find()
                                && accRegExp.matcher(message).find()) {
                            Matcher atmNameMatcher = atmNameRegExp.matcher(message);
                            if (atmNameMatcher.find()) {
                                Log.i(TAG, "onReceive: 2");
                                bankAtmName = atmNameMatcher.group(2);
                                saveBankAtmDetails(bankAtmName, currentMessage.getTimestampMillis());
                            }
                            break;
                        }
                    }
                    Log.i("SmsReceiver", "senderNum: " + phoneNumber + "; message: " + message + ",bankAtmName:" + bankAtmName);
                }
            }
        } catch (Exception e) {
            gps.stopUsingGPS();
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
        gps.stopUsingGPS();
    }

    private void saveBankAtmDetails(String bankAtmName, long timestampMillis) {
        if (gps.canGetLocation()) {
            /* Get lat and long */
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String latLong = latitude + "," + longitude;
            PreferenceManager.saveCurLocation(context, latLong);
            /* start sync service */
            PreferenceManager.saveAtmDetailsToSync(context, bankAtmName, timestampMillis + "", latLong);
            context.startService(new Intent(context, SyncData.class));
            Log.i(TAG, "getGpsLocation: " + latLong);
        }
    }
}
