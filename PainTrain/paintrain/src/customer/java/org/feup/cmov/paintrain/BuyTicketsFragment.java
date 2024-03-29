package org.feup.cmov.paintrain;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vascofg on 31-10-2015.
 */
public class BuyTicketsFragment extends DrawerViewFragment implements TimePickerFragment.TimePickerListener, View.OnClickListener, BuyTicketDialogFragment.BuyTicketDialogListener {

    private static final String TAG = "BuyTickets";

    private TextView mTime;
    private Spinner from_stations_spinner, to_stations_spinner, arrival_departure_spinner;

    private int hourOfDay, minute;

    private View mProgress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy_tickets, container, false);

        mTime = (TextView) rootView.findViewById(R.id.buy_tickets_time);
        Log.d(TAG, "Setting calendar time");
        final Calendar c = Calendar.getInstance();
        setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        mProgress = getActivity().findViewById(R.id.progress_bar);

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
        to_stations_spinner.setSelection(3);

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

            JsonObjectRequest jsonObjectRequest = new SecureJsonObjectRequest(Request.Method.POST, url, jo, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.d(TAG, jsonObject.toString());
                    mProgress.setVisibility(View.GONE);

                    DialogFragment f = BuyTicketDialogFragment.newInstance(jsonObject.toString());
                    f.setTargetFragment(BuyTicketsFragment.this, 0);
                    f.show(BuyTicketsFragment.this.getFragmentManager(), "details");
                }
            }, getActivity());

            queue.add(jsonObjectRequest);
            mProgress.setVisibility(View.VISIBLE);
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

    @Override
    public void onDialogTicketConfirmed(JSONObject tickets) {
        //CONFIRM TICKETS
        Log.d(TAG, tickets.toString());

        final JSONArray oldTickets;
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        try {
            oldTickets = tickets.getJSONArray("data");

            String url = getResources().getString(R.string.base_url) + "/pay";

            //TODO: CONFIRM AND PAY TICKETS
            JsonObjectRequest jsonObjectRequest = new SecureJsonObjectRequest(Request.Method.POST, url, tickets, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject ticketsObj) {
                    try {
                        Log.d(TAG, ticketsObj.toString());
                        JSONArray newTickets = ticketsObj.getJSONArray("data");

                        JSONObject arrivalTicketObj = oldTickets.getJSONObject(0);
                        JSONObject departureTicketObj = oldTickets.getJSONObject(oldTickets.length() - 1);


                        //TODO: tickets wrong order
                        String newArrivalTicket = newTickets.getString(0);
                        String newDepartureTicket = newTickets.getString(newTickets.length() - 1);

                        JSONObject departureStationObj = departureTicketObj.getJSONObject("firstStation");
                        JSONObject arrivalStationObj = arrivalTicketObj.getJSONObject("lastStation");

                        //combined tickets array
                        JSONArray finalTicketsArray = new JSONArray();
                        if(newTickets.length()>1) {
                            finalTicketsArray.put(newDepartureTicket);
                        }
                        finalTicketsArray.put(newArrivalTicket);

                        JSONObject finalTicket = new JSONObject();
                        finalTicket.put("firstStation", departureStationObj);
                        finalTicket.put("lastStation", arrivalStationObj);
                        finalTicket.put("tickets", finalTicketsArray);

                        String newTicket = finalTicket.toString();

                        //update ticket set
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

                        Set<String> ticketSet = settings.getStringSet("tickets", null);

                        if (ticketSet == null)
                            ticketSet = new HashSet<String>();

                        ticketSet.add(newTicket);
                        Log.d(TAG, newTickets.toString());

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putStringSet("tickets", ticketSet);

                        editor.commit();

                        mProgress.setVisibility(View.GONE);
                        Log.d(TAG, "Saved ticket on SharedPreferences set");
                        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, getActivity());
            queue.add(jsonObjectRequest);
            getActivity().findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
