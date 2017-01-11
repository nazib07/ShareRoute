package shareroute.nazib.com.shareroute;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nazib on 1/11/17.
 */

public class RouteListAdapter extends BaseAdapter implements Filterable {


    private RouteFilter routeFilter;
    private ArrayList<String> routeList;
    private ArrayList<String> filteredList;
    private Context context;
    private Boolean isShared;

    /**
     * Initialize context variables
     * @param activity route list activity
     * @param routeList route list
     */
    public RouteListAdapter(CreatedRouteFragment activity, ArrayList<String> routeList) {
        this.routeList = routeList;
        this.filteredList = routeList;
        context = activity.getActivity().getBaseContext();
        isShared = false;
        getFilter();
    }

    public RouteListAdapter(SharedRouteFragment activity, ArrayList<String> routeList) {
        this.routeList = routeList;
        this.filteredList = routeList;
        context = activity.getActivity().getBaseContext();
        isShared = true;
        getFilter();
    }

    /**
     * Get size of route list
     * @return userList size
     */
    @Override
    public int getCount() {
        return filteredList.size();
    }

    /**
     * Get specific item from route list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return filteredList.get(i);
    }

    /**
     * Get route list item id
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     * @param position index
     * @param view current list item view
     * @param parent parent
     * @return view
     */
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final Holder holder;
        final String route = (String) getItem(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.program_list, parent, false);
            holder = new Holder();
            holder.tv=(TextView) view.findViewById(R.id.textView1);
            holder.img=(ImageView) view.findViewById(R.id.imageView1);
            holder.btn_details =(ImageButton) view.findViewById(R.id.imageButton1);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (Holder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.tv.setText(route);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked "+ routeList.get(position), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, MapActivity.class);
                Log.d("[SHARE_ROUTE]", "isShared " + isShared.toString());
                if(!isShared) {
                    intent.setAction(CommonUtils.INTENT_ACTION_CUSTOM_1);
                }else if(isShared){
                    intent.setAction(CommonUtils.INTENT_ACTION_CUSTOM_2);
                }
                intent.putExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME, routeList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.btn_details.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(context, "details "+ routeList.get(position), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, DetailsActivity.class);
                if(!isShared) {
                    intent.setAction(CommonUtils.INTENT_ACTION_CUSTOM_1);
                }else if(isShared){
                    intent.setAction(CommonUtils.INTENT_ACTION_CUSTOM_2);
                }
                intent.putExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME, routeList.get(position));
                context.startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (routeFilter == null) {
            routeFilter = new RouteFilter();
        }

        return routeFilter;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    public class Holder
    {
        TextView tv;
        ImageView img;
        ImageButton btn_details;
    }

    /**
     * Custom filter for route list
     * Filter content in route list according to the search text
     */
    private class RouteFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<String> tempList = new ArrayList<>();

                // search content in route list
                for (String route : routeList) {
                    if (route.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(route);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = routeList.size();
                filterResults.values = routeList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<String>) results.values;
            Log.d("[SHARE_ROUTE]", "filtered list " + filteredList.toString());
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }

}