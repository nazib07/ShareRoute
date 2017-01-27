package shareroute.nazib.com.shareroute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by nazib on 1/8/17.
 */

public class CommonUtils {
    public final static String SELECTED_ROUTE_FILE_NAME = "SELECTED_ROUTE_FILE_NAME";
    public final static String INTENT_ACTION_CUSTOM_1 = "INTENT_ACTION_CUSTOM_1";
    public final static String INTENT_ACTION_CUSTOM_2 = "INTENT_ACTION_CUSTOM_2";
    public final static String INTENT_ACTION_CUSTOM_3 = "INTENT_ACTION_CUSTOM_3";
    public static final String SHAREROUTE_PREFS_NAME = "SHARE_ROUTE";
    public static final String DISTANCE_TUNER_VALUE = "distance_tuner_value";
    public static final int DEFAULT_DISTANCE_TUNER_VALUE = 15;

    public static int getTunerValueFromPreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CommonUtils.SHAREROUTE_PREFS_NAME, context.MODE_PRIVATE);
        int restoredText = prefs.getInt(CommonUtils.DISTANCE_TUNER_VALUE, CommonUtils.DEFAULT_DISTANCE_TUNER_VALUE);
        return restoredText;
    }

    public static void setTunerValueInPreference(Context context, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CommonUtils.SHAREROUTE_PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putInt(CommonUtils.DISTANCE_TUNER_VALUE, value);
        editor.commit();
    }


    public static void createAlert(final Context context, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(
                context).create();

        // Setting Dialog Title
        alertDialog.setTitle("Alert");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.tick);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                //Toast.makeText(context, "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
