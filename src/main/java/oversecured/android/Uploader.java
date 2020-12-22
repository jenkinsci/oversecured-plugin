package oversecured.android;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import oversecured.android.network.Utils;
import oversecured.android.network.NetworkModule;
import oversecured.android.network.OversecuredService;
import oversecured.android.network.model.AddVersionRequest;
import oversecured.android.network.model.AppSignRequest;
import oversecured.android.network.model.AppSignResponse;
import retrofit2.Response;

public class Uploader {
    private static final String PLATFORM = "android";
    private static final MediaType APP_CONTENT_TYPE = MediaType.parse("application/octet-stream");
    
    private OversecuredService service;
    private String integrationId;
    private PrintStream logger;
    
    public Uploader(String accessToken, String integrationId) {
        service = NetworkModule.getService(accessToken);
        this.integrationId = integrationId;
    }

    public void upload(File target) throws IOException {
        log("oversecured: starting version upload");

        Response<AppSignResponse> appSignResp = service.getSignedLink(new AppSignRequest(PLATFORM, target.getName()))
                .execute();
        if (appSignResp.code() != 200) {
            throw requestErr("Signed URL", appSignResp);
        }

        AppSignResponse signInfo = appSignResp.body();
        Response<Void> uploadResp = service
                .uploadAppFile(signInfo.getUrl(), RequestBody.create(APP_CONTENT_TYPE, target)).execute();

        if (uploadResp.code() != 200) {
            throw requestErr("S3 Upload", uploadResp);
        }

        AddVersionRequest addVersion = new AddVersionRequest(target.getName(), signInfo.getBucketKey());
        Response<Void> addVersionResp = service.scanVersion(integrationId, addVersion).execute();
        if (addVersionResp.code() != 200) {
            throw requestErr("Scan Version", addVersionResp);
        }
        log("oversecured: success");
    }
    
    public void setLogger(PrintStream logger) {
        this.logger = logger;
    }
    
    private void log(String s) {
        if(logger != null) {
            logger.println(s);
        }
    }

    private IOException requestErr(String msg, Response<?> resp) {
        ResponseBody errorBody = resp.errorBody();
        String informativeMessage = String.format("oversecured: Step %s failed with code %d, server message: %s", msg,
                resp.code(), Utils.getServerError(errorBody));
        return new IOException(informativeMessage);
    }
}
