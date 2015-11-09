package org.feup.cmov.paintrain;

import android.app.*;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "TimePickerFragment";
    // Use this instance of the interface to deliver action events
    TimePickerListener mListener;

    public static TimePickerFragment newInstance(int hourOfDay, int minute) {
        TimePickerFragment f = new TimePickerFragment();

        // Supply input as an argument.
        Bundle args = new Bundle();
        args.putInt("hourOfDay", hourOfDay);
        args.putInt("minute", minute);
        f.setArguments(args);

        return f;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TimePickerListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TimePickerListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hourOfDay = getArguments().getInt("hourOfDay");
        int minute = getArguments().getInt("minute");

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onDialogTimeChosen(hourOfDay, minute);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
        }
    }

    public interface TimePickerListener {
        void onDialogTimeChosen(int hourOfDay, int minute);
    }
}