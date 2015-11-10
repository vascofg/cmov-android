package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vascofg on 09-11-2015.
 */
public class SecureJsonObjectRequest extends JsonObjectRequest {

    private Activity activity;

    public SecureJsonObjectRequest(int method,
                                   java.lang.String url,
                                   org.json.JSONObject jsonRequest,
                                   com.android.volley.Response.Listener<org.json.JSONObject> listener,
                                   Activity activity) {
        super(method, url, jsonRequest, listener, new CustomErrorListener(activity));
        this.activity = activity;
    }

    public SecureJsonObjectRequest(int method,
                                   java.lang.String url,
                                   com.android.volley.Response.Listener<org.json.JSONObject> listener,
                                   Activity activity) {
        super(method, url, listener, new CustomErrorListener(activity));
        this.activity = activity;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        //get token from storage
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String token = settings.getString(AuthActivity.TOKEN_KEY, null);

        Map<String, String> headers = new HashMap<String, String>();
        String auth = "Bearer " + token;
        headers.put("Authorization", auth);
        return headers;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        //if response empty, make it into the empty json object "{}"
        if (response.data.length == 0) {
            byte[] responseData = "{}".getBytes();
            response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
        }
        return super.parseNetworkResponse(response);
    }
}
