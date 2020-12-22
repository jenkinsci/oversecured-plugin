package oversecured.android.network.model;

import com.google.gson.annotations.SerializedName;

public class Error {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}