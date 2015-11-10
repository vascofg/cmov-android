package org.feup.cmov.paintrain;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
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
        JsonObjectRequest jsonObjectRequest = new InsecureJsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d(TAG, jsonObject.toString());
                        /*getListView().setAdapter(new JSONArrayAdapter(getBaseContext(),
                                jsonArray,                                                   // JSONArray data
                                R.layout.timetable_row,                    // a layout resource to display a row
                                new String[] {"departure", "departureTime", "arrival", "arrivalTime"},                     // field names from JSONObjects
                                new int[] {R.id.timetable_row_departure, R.id.timetable_row_departure_time, R.id.timetable_row_arrival, R.id.timetable_row_arrival_time},     // corresponding View ids to map field names to
                                "id" ));*/
                        try {
                            if (jsonObject != null) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                mItems = new ArrayList<JSONObject>(data.length());
                                for (int i = 0; i < data.length(); i++) {
                                    mItems.add(data.getJSONObject(i));
                                }
                                mListView.setAdapter(new TimetableArrayAdapter(getActivity(), mItems));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, getActivity());
// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

        return rootView;
    }


}
