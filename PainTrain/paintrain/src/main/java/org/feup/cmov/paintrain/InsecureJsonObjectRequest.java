package org.feup.cmov.paintrain;

import android.app.Activity;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

/**
 * Created by vascofg on 09-11-2015.
 */
public class InsecureJsonObjectRequest extends JsonObjectRequest {

    private Activity activity;

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Activity activity) {
        super(method, url, jsonRequest, listener, new CustomErrorListener(activity));
        this.activity = activity;
    }

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     Response.Listener<JSONObject> listener,
                                     Activity activity) {
        super(method, url, listener, new CustomErrorListener(activity));
        this.activity = activity;
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
