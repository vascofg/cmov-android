package org.feup.cmov.paintrain;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vascofg on 09-11-2015.
 */
public class BuyTicketDialogFragment extends DialogFragment {

    private static final String TAG = "BuyTicketDialog";

    BuyTicketDialogListener mListener;

    public static BuyTicketDialogFragment newInstance(String tickets) {
        BuyTicketDialogFragment f = new BuyTicketDialogFragment();

        // Supply input as an argument.
        Bundle args = new Bundle();
        args.putString("tickets", tickets);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final JSONArray tickets;
        try {
            tickets = new JSONObject(getArguments().getString("tickets")).getJSONArray("data");
        } catch (JSONException e) {
            throw new RuntimeException("JSON Error");
        }
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] tripsStr = new String[tickets.length()];
        for (int i = 0; i < tickets.length(); i++) {
            try {
                JSONObject ticket = tickets.getJSONObject(i);
                JSONObject departure = ticket.getJSONObject("firstStation");
                String departureStation = departure.getString("station");
                String departureTime = departure.getString("time");

                JSONObject arrival = ticket.getJSONObject("lastStation");
                String arrivalStation = arrival.getString("station");
                String arrivalTime = arrival.getString("time");

                tripsStr[i] = departureTime + " " + departureStation + "->" + arrivalTime + " " + arrivalStation;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        builder.setItems(tripsStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Clicked item #" + i);
                try {
                    mListener.onDialogTicketChosen(tickets.getJSONObject(i));
                } catch (JSONException e) {
                    throw new RuntimeException("JSON Error");
                }
            }
        }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (BuyTicketDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement BuyTicketDialogListener");
        }
    }

    public interface BuyTicketDialogListener {
        void onDialogTicketChosen(JSONObject ticket);
    }
}
