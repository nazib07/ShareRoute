package shareroute.nazib.com.shareroute;

import android.app.Activity;
import android.content.Intent;
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
import static shareroute.nazib.com.shareroute.FileUtils.getSharedRouteNames;

/**
 * Created by nazib on 11/28/2016.
 */
public class SharedRouteFragment extends Fragment {

    private ArrayList<String> sharedRouteNames;
    private ListView lstItems;
    private RouteListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_demo, container, false);
        lstItems = (ListView) v.findViewById(R.id.listView);
        lstItems.setTextFilterEnabled(true);

        fillListView(lstItems);
        return v;
    }

    private void fillListView(ListView lstItems) {
        sharedRouteNames = getSharedRouteNames();
        Log.d("[SHARE_ROUTE]", "Shared fragment fillListView" + sharedRouteNames.toString());
        if (sharedRouteNames != null) {
            //CustomAdapter adapter = new CustomAdapter(this, sharedRouteNames);
            adapter = new RouteListAdapter(this, sharedRouteNames);
            lstItems.setAdapter(adapter);
        }
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

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
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
        public boolean onQueryTextChange(String newText) {
            // TODO Auto-generated method stub
            Log.d("[SHARE_ROUTE]", "search text " + newText);
            adapter.getFilter().filter(newText);
            return false;
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            /** EDIT **/
            case R.id.frag_menu:
                Intent intent = new Intent(this.getActivity().getBaseContext(), HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//end switch
    }//end onOptionsItemSelected

    @Override
    public void onResume() {
        Log.d("[SHARE_ROUTE]", "onResume of Fragment");
        super.onResume();

        fillListView(lstItems);

    }

}
