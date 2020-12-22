package oversecured.android.network;

import com.google.gson.Gson;

import okhttp3.ResponseBody;

import oversecured.android.network.model.Error;

public class Utils {
    public static String getServerError(ResponseBody errorBody) {
        Error err = new Gson().fromJson(errorBody.charStream(), Error.class);
        return err != null ? err.getMessage() : null;
    }
}