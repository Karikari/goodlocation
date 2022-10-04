package com.karikari.goodlocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageBox {

    public static void showMessageOK(Context context,CharSequence message, DialogInterface.OnClickListener okListener) {
        showMessageOK(context, message, okListener, null);
    }

    private static void showMessageOK(Context context, CharSequence message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Enable GPS", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }
}
