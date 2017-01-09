package shareroute.nazib.com.shareroute;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

import static shareroute.nazib.com.shareroute.FileUtils.createDir;
import static shareroute.nazib.com.shareroute.FileUtils.createNewRouteFile;
import static shareroute.nazib.com.shareroute.FileUtils.deleteCreatedNewRouteFile;
import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteFileObject;
import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteNames;
import static shareroute.nazib.com.shareroute.FileUtils.readFromFile;
import static shareroute.nazib.com.shareroute.FileUtils.writeToFile;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        FileUtils.setContext(context);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new Fragment();

        int id = item.getItemId();

/*        if (id == R.id.nav_camera) {
            // Handle the camera action
            Log.d("NAZIB", "nav_camera");
            fragment = new TestFragment_1();
        } else */
        if (id == R.id.nav_create_route) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_input_route_name, null))
                    // Add action buttons
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // sign in the user ...
                            Intent intent = new Intent(context, MapActivity.class);
                            startActivity(intent);
                            createNewRouteFile("test1.geojson");
                            //File file = getCreatedRouteFileObject("test1.geojson");
//                            String data = readFromFile(file.getAbsolutePath());
//                            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
                            //getCreatedRouteNames();
                            //deleteCreatedNewRouteFile("test2.geojson");

                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).create().show();

        } else if (id == R.id.nav_created_by_me) {
            fragment = new TestListFragment();
            transaction.replace(R.id.flFragments, fragment);
            transaction.commit();
            getSupportActionBar().setTitle("Created by me");

        } else if (id == R.id.nav_shared_with_me) {
            fragment = new TestListFragment();
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

}
