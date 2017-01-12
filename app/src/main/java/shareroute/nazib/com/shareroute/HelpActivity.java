package shareroute.nazib.com.shareroute;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        String str = "<h3 style=\"color: #5e9ca0;\"><span style=\"color: #3366ff;\">How to use the&nbsp;ShareRoute?</span></h3>\n" +
                "<p style=\"text-align: justify;\"><strong><span style=\"color: #000000;\">Create Route</span></strong></p>\n" +
                "<ol>\n" +
                "<li><span style=\"color: #000000;\">Press \"Create Route\" from Navigation Drawer or Press '+' floating icon</span></li>\n" +
                "<li><span style=\"color: #000000;\">A Map will be loaded, Press edit icon at bottom right corner</span></li>\n" +
                "<li>Editing tools will be shown</li>\n" +
                "<li>Move map to adjust the desired location with cosshair icon</li>\n" +
                "<li>Press Place icon to add Marker on Map</li>\n" +
                "<li>Press Polyline icon to add new line on map</li>\n" +
                "<li>Press My location icon to move crosshair to current location</li>\n" +
                "<li>Press Delete icon to delete last added marker or line segment</li>\n" +
                "</ol>\n" +
                "<p style=\"text-align: justify;\"><strong>Share Route</strong></p>\n" +
                "<ol>\n" +
                "<li style=\"text-align: justify;\">From Route list from Created Route or Shared Route press info icon</li>\n" +
                "<li style=\"text-align: justify;\">Press Share icon to Share</li>\n" +
                "<li style=\"text-align: justify;\">choose which appcontrol by which map will be shared</li>\n" +
                "</ol>\n" +
                "<p style=\"text-align: justify;\"><strong>Delete Route</strong></p>\n" +
                "<ol>\n" +
                "<li style=\"text-align: justify;\">From Route list from Created Route or Shared Route press info icon</li>\n" +
                "<li style=\"text-align: justify;\">From Option Menu Press Delete</li>\n" +
                "<li style=\"text-align: justify;\">Route will be deleted</li>\n" +
                "</ol>\n" +
                "<p style=\"text-align: justify;\"><strong>Open Route (*.geojson) file</strong></p>\n" +
                "<ol>\n" +
                "<li style=\"text-align: justify;\">Click geojson file created by ShareRoute app from anywhere in device</li>\n" +
                "<li style=\"text-align: justify;\">Route&nbsp;will opened in mapview.</li>\n" +
                "<li style=\"text-align: justify;\">give a name to save this route in your shared list</li>\n" +
                "</ol>\n" +
                "<p style=\"text-align: justify;\"><strong>Delete All Routes</strong></p>\n" +
                "<ol>\n" +
                "<li style=\"text-align: justify;\">From Settings, Select Clear all routes, all route will be deleted</li>\n" +
                "</ol>\n" +
                "<p style=\"text-align: justify;\"><strong>Help &amp; Feedback</strong></p>\n" +
                "<ol>\n" +
                "<li style=\"text-align: justify;\">Click Help from Navigation Drawer or Options menu to go to Help Page</li>\n" +
                "</ol>\n" +
                "<p>Please give your feedback or any query to below e-mail</p>\n" +
                "<p><a href=\"mailto:nazib.cse@gmail.com\">nazib.cse@gmail.com</a></p>\n" +
                "<p><strong>&nbsp;</strong></p>";

        TextView textView = (TextView)findViewById(R.id.textView2);
        textView.setText(Html.fromHtml(str));
    }
}
