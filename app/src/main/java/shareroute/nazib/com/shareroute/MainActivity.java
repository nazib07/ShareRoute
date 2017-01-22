package shareroute.nazib.com.shareroute;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.GoogleApiClient;

import static shareroute.nazib.com.shareroute.CommonUtils.INTENT_ACTION_CUSTOM_1;
import static shareroute.nazib.com.shareroute.CommonUtils.SELECTED_ROUTE_FILE_NAME;
import static shareroute.nazib.com.shareroute.FileUtils.createNewRouteFile;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Context context;
    private FragmentTransaction transaction = null;
    private Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        FileUtils.setContext(context);


        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions_left);
        menuMultipleActions.collapse();

        final FloatingActionButton fabCreateRoute = (FloatingActionButton) findViewById(R.id.fab_create_route);
        fabCreateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.collapse();
                Log.d("[SHARE_ROUTE]", "fab create button clicked");
                createAddRouteDialog();
            }
        });

        final FloatingActionButton fabRecordRoute = (FloatingActionButton) findViewById(R.id.fab_record_route);
        fabRecordRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.collapse();
                Log.d("[SHARE_ROUTE]", "fab RecordRoute button clicked");
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_created_by_me);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_created_by_me));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            Intent intent =  new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        fragment = new Fragment();

        int id = item.getItemId();

        if (id == R.id.nav_create_route) {
            createAddRouteDialog();

        }else if(id == R.id.nav_record_route){
            Log.d("[SHARE_ROUTE]", "nav record route");
        }else if (id == R.id.nav_created_by_me) {
            fragment = new CreatedRouteFragment();
            transaction.replace(R.id.flFragments, fragment);
            transaction.commit();
            getSupportActionBar().setTitle("Created by me");

        } else if (id == R.id.nav_shared_with_me) {
            fragment = new SharedRouteFragment();
            transaction.replace(R.id.flFragments, fragment);
            transaction.commit();
            getSupportActionBar().setTitle("Shared with me");

        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);

        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(context, HelpActivity.class);
            context.startActivity(intent);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createAddRouteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View textEntryView = inflater.inflate(R.layout.dialog_input_route_name, null);
        TextView textview = (TextView) textEntryView.findViewById(R.id.textView);
        textview.setText("Create Route");
        builder.setView(textEntryView)
                // Add action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        try {

                            EditText editText = (EditText) textEntryView.findViewById(R.id.username);

                            String route_name;
                            route_name = editText.getText().toString();
                            if(route_name.length() > 0){
                                createNewRouteFile(route_name+".geojson");
                                Intent intent = new Intent(context, MapActivity.class);
                                intent.setAction(CommonUtils.INTENT_ACTION_CUSTOM_1);
                                intent.putExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME, route_name);
                                context.startActivity(intent);
                            }

                            }
                        catch (Exception e){
                                e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).create().show();
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        Log.d("[SHARE_ROUTE]", "onResume MainActivity");
        super.onResume();
        //Refresh your stuff here
        if(fragment != null){
            fragment.onResume();
        }
    }
}
