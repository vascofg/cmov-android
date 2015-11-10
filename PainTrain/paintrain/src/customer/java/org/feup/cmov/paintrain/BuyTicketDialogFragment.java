package org.feup.cmov.paintrain;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
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
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            final JSONObject ticketsObj = new JSONObject(getArguments().getString("tickets"));
            final JSONArray tickets = ticketsObj.getJSONArray("data");

            //TODO: stations on wrong order from server

            JSONObject arrivalTicket = tickets.getJSONObject(0);
            JSONObject departureTicket = tickets.getJSONObject(tickets.length() - 1);

            JSONObject departureStationObj = departureTicket.getJSONObject("firstStation");
            JSONObject arrivalStationObj = arrivalTicket.getJSONObject("lastStation");

            String departureStation = departureStationObj.getString("station");
            String departureTime = departureStationObj.getString("time");

            String arrivalStation = arrivalStationObj.getString("station");
            String arrivalTime = arrivalStationObj.getString("time");

            double departureTicketCost = departureTicket.getDouble("totalCost");
            double arrivalTicketCost = arrivalTicket.getDouble("totalCost");

            double finalTicketCost = departureTicketCost + arrivalTicketCost;

            builder.setMessage(departureTime + " " + departureStation + "->" + arrivalTime + " " + arrivalStation +
                    "(" + String.format("%.2f", finalTicketCost) + ")")
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onDialogTicketConfirmed(ticketsObj);
                }
            });

            // Create the AlertDialog object and return it
            return builder.create();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
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
        void onDialogTicketConfirmed(JSONObject tickets);
    }
}
