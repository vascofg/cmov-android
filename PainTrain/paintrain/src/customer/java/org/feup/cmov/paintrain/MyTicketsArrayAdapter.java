package org.feup.cmov.paintrain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by vascofg on 09-11-2015.
 */
public class MyTicketsArrayAdapter extends ArrayAdapter<JSONObject> {

    private final Context context;

    public MyTicketsArrayAdapter(Context context, List<JSONObject> values) {
        super(context, -1, values);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.timetable_row, parent, false);
        TextView departure, arrival, departureTime, arrivalTime;
        departure = (TextView) rowView.findViewById(R.id.timetable_row_departure);
        arrival = (TextView) rowView.findViewById(R.id.timetable_row_arrival);
        departureTime = (TextView) rowView.findViewById(R.id.timetable_row_departure_time);
        arrivalTime = (TextView) rowView.findViewById(R.id.timetable_row_arrival_time);


        try {
            JSONObject ticket = getItem(position);
            JSONObject departureObj = ticket.getJSONObject("firstStation");
            JSONObject arrivalObj = ticket.getJSONObject("lastStation");

            departure.setText(departureObj.getString("station"));
            departureTime.setText(departureObj.getString("time"));

            arrival.setText(arrivalObj.getString("station"));
            arrivalTime.setText(arrivalObj.getString("time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
