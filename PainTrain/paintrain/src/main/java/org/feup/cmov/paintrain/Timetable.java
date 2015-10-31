package org.feup.cmov.paintrain;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by vascofg on 31-10-2015.
 */
public class Timetable extends ListActivity {
    String[] testItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testItems = new String[]{"Vegetables", "Fruits", "Flower Buds", "Legumes", "Bulbs", "Tubers"};
        this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testItems));
    }
}
