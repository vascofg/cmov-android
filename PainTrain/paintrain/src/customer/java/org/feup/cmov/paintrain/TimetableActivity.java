package org.feup.cmov.paintrain;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
public class TimetableActivity extends ListActivity {
    private static final String TAG = "TimetableActivity";

    private List<JSONObject> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* VOLLEY */
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.base_url) + "/timetable";

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Log.d(TAG, "Clicked on list: " + position);
                Log.d(TAG, mItems.get(position).toString());
                DialogFragment f = TimetableDetailsDialogFragment.newInstance(mItems.get(position).toString());
                f.show(TimetableActivity.this.getFragmentManager(),"details");
            }
        });

        // Request a string response from the provided URL.
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
                                getListView().setAdapter(new TimetableArrayAdapter(getBaseContext(), mItems));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "Error getting timetable", Toast.LENGTH_LONG).show();
                Log.e(TAG, "VOLLEY: " + error.getMessage());
            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
}
