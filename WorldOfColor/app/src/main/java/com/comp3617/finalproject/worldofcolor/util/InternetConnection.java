package com.comp3617.finalproject.worldofcolor.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by edz on 2017-07-19.
 */

public class InternetConnection {

    public static void isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null){
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

}
