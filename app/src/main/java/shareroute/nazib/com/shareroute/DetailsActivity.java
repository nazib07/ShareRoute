package shareroute.nazib.com.shareroute;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static shareroute.nazib.com.shareroute.FileUtils.deleteCreatedNewRouteFile;
import static shareroute.nazib.com.shareroute.FileUtils.deleteSharedNewRouteFile;
import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteFileObject;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "[SHARE_ROUTE]";
    private String incomingFileName;
    private Boolean isShared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //handling incoming intent
        incomingFileName = null;
        Intent intent = getIntent();
        String intentAction = intent.getAction();

        TextView textView = (TextView) findViewById(R.id.textView1);

        if(CommonUtils.INTENT_ACTION_CUSTOM_1.equals(intentAction)) {
            Log.d(TAG, "Incoming intent extra " + intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME));
            incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
            if (incomingFileName != null) {
                textView.setText(incomingFileName);
                incomingFileName += ".geojson";
            }
            isShared = false;
        }
        else if(CommonUtils.INTENT_ACTION_CUSTOM_2.equals(intentAction)){
            Log.d(TAG, "Incoming intent extra " + intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME));
            incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
            if (incomingFileName != null) {
                textView.setText(incomingFileName);
                incomingFileName += ".geojson";
            }
            isShared = true;
        }

        ImageButton shareButton = (ImageButton) findViewById(R.id.imageButton1);
        shareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "share button clicked");
                File file = getCreatedRouteFileObject(incomingFileName);

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("*/*");
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
                startActivity(sendIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:
                if(isShared){
                    deleteSharedNewRouteFile(incomingFileName);
                    onBackPressed();
                }else{
                    deleteCreatedNewRouteFile(incomingFileName);
                    onBackPressed();
                }
                return true;
            case R.id.action_help:
                Intent intent =  new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
