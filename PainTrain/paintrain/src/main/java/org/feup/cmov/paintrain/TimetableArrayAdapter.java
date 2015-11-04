package org.feup.cmov.paintrain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.w3c.dom.Text;

/**
 * Created by vascofg on 04-11-2015.
 */
/* --- NOT IN USE - USING JSONARRAYADAPTER --- */
public class TimetableArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public TimetableArrayAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.timetable_row, parent, false);
        TextView departure, arrival, departureTime, arrivalTime;
        departure = (TextView) rowView.findViewById(R.id.timetable_row_departure);
        arrival = (TextView) rowView.findViewById(R.id.timetable_row_arrival);
        departureTime = (TextView) rowView.findViewById(R.id.timetable_row_departure_time);
        arrivalTime = (TextView) rowView.findViewById(R.id.timetable_row_arrival_time);

    }*/
}
