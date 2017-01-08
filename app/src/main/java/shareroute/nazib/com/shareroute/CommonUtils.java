package shareroute.nazib.com.shareroute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Created by nazib on 1/8/17.
 */

public class CommonUtils {
    public static void createAlert(final Context context, String message){
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
