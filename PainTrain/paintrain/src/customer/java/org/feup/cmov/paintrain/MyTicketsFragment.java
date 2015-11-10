package org.feup.cmov.paintrain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by vascofg on 09-11-2015.
 */
public class MyTicketsFragment extends DrawerViewFragment {

    private static final String TAG = "MyTickets";

    private List<JSONObject> mItems;

    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(android.R.layout.list_content, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mItems = new ArrayList<JSONObject>();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        Set<String> tickets = settings.getStringSet("tickets", null);

        if (tickets != null) {

            Log.d(TAG, tickets.toString());

            try {
                for (String ticket : tickets) {
                    mItems.add(new JSONObject(ticket));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListView.setAdapter(new MyTicketsArrayAdapter(getActivity(), mItems));

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    Log.d(TAG, "Clicked on list: " + position);
                    Log.d(TAG, mItems.get(position).toString());
                    try {
                        showQRCode(mItems.get(position).getJSONArray("tickets").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        return rootView;
    }

    private void showQRCode(String text) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
            intent.putExtra("ENCODE_DATA", text);
            intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
            intent.putExtra("ENCODE_SHOW_CONTENTS", false);

            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }
}
