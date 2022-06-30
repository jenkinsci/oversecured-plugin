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
    private static final MediaType APP_CONTENT_TYPE = MediaType.parse("application/octet-stream");

    private static final String PLATFORM_ANDROID = "android";
    private static final String PLATFORM_IOS = "ios";

    private OversecuredService service;
    private String integrationId;
    private String branchName;
    private PrintStream logger;
    
    public Uploader(String accessToken, String integrationId, String branchName) {
        service = NetworkModule.getService(accessToken);
        this.integrationId = integrationId;
        this.branchName = branchName;
    }

    public void upload(File target) throws IOException {
        log("oversecured: file upload");

        String fileName = target.getName();
        String platform = getPlatform(fileName);
        Response<AppSignResponse> appSignResp = service.getSignedLink(new AppSignRequest(platform, fileName))
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
        Response<Void> addVersionResp = service.scanVersion(integrationId, branchName, addVersion).execute();
        if (addVersionResp.code() != 200) {
            throw requestErr("Scan Version", addVersionResp);
        }
        log("oversecured: success");
    }

    private String getPlatform(String fileName) throws IOException {
        String lowercasedName = fileName.toLowerCase();
        if (lowercasedName.endsWith(".apk") || lowercasedName.endsWith(".aab")) {
            return PLATFORM_ANDROID;
        }
        if (lowercasedName.endsWith(".zip")) {
            return PLATFORM_IOS;
        }
        throw new IOException("App file '" + fileName + "' has invalid extension. Only '.apk', '.aab' and `.zip` are allowed");
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
