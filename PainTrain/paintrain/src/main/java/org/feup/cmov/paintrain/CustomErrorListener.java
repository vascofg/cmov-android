package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpURLConnection;

/**
 * Created by vascofg on 09-11-2015.
 */
public class CustomErrorListener implements Response.ErrorListener {

    private Activity activity;
    private View progress;

    public CustomErrorListener(Activity activity) {
        this.activity = activity;
        this.progress = activity.findViewById(R.id.progress_bar);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        try {
            Log.e("VOLLEY", volleyError.toString());

            if (progress != null)
                progress.setVisibility(View.GONE);

            Log.d("VOLLEY", volleyError.networkResponse.toString());
            if (volleyError.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Intent authIntent = new Intent(activity, AuthActivity.class);
                activity.startActivityForResult(authIntent, AuthActivity.RC_AUTH);
                return;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Toast.makeText(activity, R.string.connection_error, Toast.LENGTH_LONG).show();
    }
}
