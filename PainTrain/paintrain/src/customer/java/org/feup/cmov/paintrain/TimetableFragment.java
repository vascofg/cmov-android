package org.feup.cmov.paintrain;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vascofg on 31-10-2015.
 */
public class TimetableFragment extends DrawerViewFragment {
    private static final String TAG = "TimetableFragment";

    private List<JSONObject> mItems;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(android.R.layout.list_content, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Log.d(TAG, "Clicked on list: " + position);
                Log.d(TAG, mItems.get(position).toString());
                DialogFragment f = TimetableDetailsDialogFragment.newInstance(mItems.get(position).toString());
                f.show(TimetableFragment.this.getFragmentManager(), "details");
            }
        });

        /* VOLLEY */
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = getResources().getString(R.string.base_url) + "/timetable";

        // Request a JSON response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG, jsonArray.toString());
                        /*getListView().setAdapter(new JSONArrayAdapter(getBaseContext(),
                                jsonArray,                                                   // JSONArray data
                                R.layout.timetable_row,                    // a layout resource to display a row
                                new String[] {"departure", "departureTime", "arrival", "arrivalTime"},                     // field names from JSONObjects
                                new int[] {R.id.timetable_row_departure, R.id.timetable_row_departure_time, R.id.timetable_row_arrival, R.id.timetable_row_arrival_time},     // corresponding View ids to map field names to
                                "id" ));*/
                        try {
                            if (jsonArray != null) {
                                mItems = new ArrayList<JSONObject>(jsonArray.length());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    mItems.add(jsonArray.getJSONObject(i));
                                }
                                mListView.setAdapter(new TimetableArrayAdapter(getActivity(), mItems));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Error getting timetable", Toast.LENGTH_LONG).show();
                Log.e(TAG, "VOLLEY: " + error.getMessage());
            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);

        return rootView;
    }

    public static class TimetableDetailsDialogFragment extends DialogFragment {

        public static TimetableDetailsDialogFragment newInstance(String trips) {
            TimetableDetailsDialogFragment f = new TimetableDetailsDialogFragment();

            // Supply input as an argument.
            Bundle args = new Bundle();
            args.putString("trips", trips);
            f.setArguments(args);

            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            JSONArray trips;
            try {
                trips = new JSONObject(getArguments().getString("trips")).getJSONArray("times");
            } catch (JSONException e) {
                throw new RuntimeException("JSON Error");
            }
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String[] tripsStr = new String[trips.length()];
            for(int i=0;i<trips.length();i++) {
                try {
                    JSONObject trip = trips.getJSONObject(i);
                    String station = trip.getString("station");
                    String time = trip.getString("time");
                    tripsStr[i] = time + ": " + station;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            builder.setItems(tripsStr, null)
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void show(FragmentManager manager, String tag) {
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        }
    }
}
