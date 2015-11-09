package org.feup.cmov.paintrain;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by vascofg on 31-10-2015.
 */
public class BuyTicketsFragment extends DrawerViewFragment implements TimePickerFragment.TimePickerListener, View.OnClickListener {

    private static final String TAG = "BuyTickets";

    private TextView mTime;
    private Spinner from_stations_spinner, to_stations_spinner, arrival_departure_spinner;

    private int hourOfDay, minute;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy_tickets, container, false);

        mTime = (TextView) rootView.findViewById(R.id.buy_tickets_time);
        Log.d(TAG, "Setting calendar time");
        final Calendar c = Calendar.getInstance();
        setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        Button button = (Button) rootView.findViewById(R.id.buy_tickets_button);
        button.setOnClickListener(this);

        TextView time = (TextView) rootView.findViewById(R.id.buy_tickets_time);
        time.setOnClickListener(this);

        from_stations_spinner = (Spinner) rootView.findViewById(R.id.from_stations_spinner);
        to_stations_spinner = (Spinner) rootView.findViewById(R.id.to_stations_spinner);
        arrival_departure_spinner = (Spinner) rootView.findViewById(R.id.buy_tickets_arrival_departure_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.stations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.arrival_departure_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        from_stations_spinner.setAdapter(adapter);
        to_stations_spinner.setAdapter(adapter);
        arrival_departure_spinner.setAdapter(adapter2);

        return rootView;
    }

    private void buy_tickets() {
        Log.d(TAG, from_stations_spinner.getSelectedItem() + "->" +
                to_stations_spinner.getSelectedItem() + ":" +
                arrival_departure_spinner.getSelectedItem() + "@" +
                mTime.getText());

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = getResources().getString(R.string.base_url) + "/ticket";

        try {
            JSONObject jo = new JSONObject();
            jo.put("initialStation", from_stations_spinner.getSelectedItem());
            jo.put("finalStation", to_stations_spinner.getSelectedItem());
            if (arrival_departure_spinner.getSelectedItemPosition() == 0)
                jo.put("tripFinalTime", mTime.getText());
            else
                jo.put("tripInitialTime", mTime.getText());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequestFixed(Request.Method.POST, url, jo, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.d(TAG, jsonObject.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            });

            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void pickTime() {
        DialogFragment f = TimePickerFragment.newInstance(this.hourOfDay, this.minute);
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "pickTime");
    }

    private void setTime(int hourOfDay, int minute) {
        String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
        mTime.setText(formattedTime);
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    @Override
    public void onDialogTimeChosen(int hourOfDay, int minute) {
        setTime(hourOfDay, minute);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buy_tickets_button:
                buy_tickets();
                break;
            case R.id.buy_tickets_time:
                pickTime();
                break;
        }
    }
}
