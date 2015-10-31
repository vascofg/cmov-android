package org.feup.cmov.paintrain;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by vascofg on 31-10-2015.
 */
public class BuyTicketsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_tickets_activity);

        Spinner from_stations_spinner = (Spinner) findViewById(R.id.from_stations_spinner);
        Spinner to_stations_spinner = (Spinner) findViewById(R.id.to_stations_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.stations_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        from_stations_spinner.setAdapter(adapter);
        to_stations_spinner.setAdapter(adapter);
    }

    public void buy_tickets(View view) {
        Toast.makeText(getBaseContext(), R.string.buy_tickets, Toast.LENGTH_LONG).show();
    }

}
