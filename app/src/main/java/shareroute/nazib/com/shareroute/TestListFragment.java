package shareroute.nazib.com.shareroute;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteNames;

/**
 * Created by nazib on 11/28/2016.
 */
public class TestListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_demo, container, false);
        ListView lstItems = (ListView)v.findViewById(R.id.listView);

        ArrayList<String> createdRouteNames;
        createdRouteNames = getCreatedRouteNames();
        if(!createdRouteNames.isEmpty()){
            CustomAdapter adapter = new CustomAdapter(this, createdRouteNames);
            lstItems.setAdapter(adapter);
        }
        return v;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.frag_menu, menu);

        //Handle the Search Menu
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint("Search here");

        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.colorAccent));
        searchView.setOnQueryTextListener(OnQuerySearchView);


    }

    private SearchView.OnQueryTextListener OnQuerySearchView = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }
        @Override
        public boolean onQueryTextChange(String arg0) {
            // TODO Auto-generated method stub
//            if (mSearchCheck){
//                // implement your search here
//            }
            return false;
        }
    };//end OnQueryTextListener


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            /** EDIT **/
            case R.id.frag_menu:
                //openEditProfile(); //Open Edit Profile Fragment
                Log.d( "NAZIB", "frag menu");
                return true;



            default:
                return super.onOptionsItemSelected(item);
        }//end switch
    }//end onOptionsItemSelected



}
