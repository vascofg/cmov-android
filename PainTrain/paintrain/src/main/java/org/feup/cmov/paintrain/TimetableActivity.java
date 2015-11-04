package org.feup.cmov.paintrain;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vascofg on 31-10-2015.
 */
public class TimetableActivity extends ListActivity {
    private static final String TAG = "TimetableActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* VOLLEY */
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://172.30.27.161:3000/trips";

// Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG,jsonArray.toString());
                        getListView().setAdapter(new JSONArrayAdapter(getBaseContext(),
                                jsonArray,                                                   // JSONArray data
                                R.layout.timetable_row,                    // a layout resource to display a row
                                new String[] {"departure", "departureTime", "arrival", "arrivalTime"},                     // field names from JSONObjects
                                new int[] {R.id.timetable_row_departure, R.id.timetable_row_departure_time, R.id.timetable_row_arrival, R.id.timetable_row_arrival_time},     // corresponding View ids to map field names to
                                "id" ));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(),"Error getting timetable",Toast.LENGTH_LONG).show();
                Log.e(TAG,"VOLLEY: " + error.getMessage());
            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
}
