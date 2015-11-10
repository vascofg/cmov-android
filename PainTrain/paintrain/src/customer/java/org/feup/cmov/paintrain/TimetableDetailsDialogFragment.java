package org.feup.cmov.paintrain;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TimetableDetailsDialogFragment extends DialogFragment {

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
        for (int i = 0; i < trips.length(); i++) {
            try {
                JSONObject trip = trips.getJSONObject(i);
                String station = trip.getString("station");
                String time = trip.getString("time");
                tripsStr[i] = time + " " + station;
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