package org.feup.cmov.paintrain;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by vascofg on 09-11-2015.
 */
public abstract class DrawerViewFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static <T extends DrawerViewFragment> T newInstance(Class<T> cl, int sectionNumber) {
        Constructor<T> c = null;
        try {
            c = cl.getConstructor();
            T fragment = c.newInstance();

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DrawerViewFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
