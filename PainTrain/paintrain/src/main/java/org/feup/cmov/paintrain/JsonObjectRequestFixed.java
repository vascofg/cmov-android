package org.feup.cmov.paintrain;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

/**
 * Created by vascofg on 09-11-2015.
 */
public class JsonObjectRequestFixed extends JsonObjectRequest {

    public JsonObjectRequestFixed(int method,
                             java.lang.String url,
                             org.json.JSONObject jsonRequest,
                             com.android.volley.Response.Listener<org.json.JSONObject> listener,
                             com.android.volley.Response.ErrorListener errorListener) {
        super(method,url,jsonRequest,listener,errorListener);
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
