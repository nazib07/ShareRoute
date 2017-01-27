package shareroute.nazib.com.shareroute;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static shareroute.nazib.com.shareroute.FileUtils.deleteAllCretedRoteFile;
import static shareroute.nazib.com.shareroute.FileUtils.deleteAllSharedRoteFile;
import static shareroute.nazib.com.shareroute.FileUtils.deleteSharedNewRouteFile;

public class SettingsActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        final ArrayList<String> list = new ArrayList<>();
        list.add("Clear All Routes");
        list.add("Tune Record Distance");
        ListView listView = (ListView) findViewById(R.id.setting_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (list.get(i).equals("Clear All Routes")) {
                    deleteAllCretedRoteFile();
                    deleteAllSharedRoteFile();
                    onBackPressed();
                } else if (list.get(i).equals("Tune Record Distance")) {
                    SliderDialog sliderDialog = new SliderDialog(context);
                    sliderDialog.show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
