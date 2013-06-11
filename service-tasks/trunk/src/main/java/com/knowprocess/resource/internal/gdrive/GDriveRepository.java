package com.knowprocess.resource.internal.gdrive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.knowprocess.resource.spi.Repository;

/**
 * Store resource to Google Drive.
 * 
 * @author tstephen
 * 
 */
public class GDriveRepository implements Repository,
        MediaHttpUploaderProgressListener {

    private static final String ACCESS_TOKEN = "ya29.AHES6ZSbw6v2SauVXGY5Gt2dJxJewejDhC70lWcpaPOUjy8";
    private static final String REFRESH_TOKEN = "1/uyYRa3NWjAEOwqTk801XGWkvwS9mnsNjXFY4uIRS0UE";
    /**
   * 
   */
    private static final String SERVICE_ACCOUNT_EMAIL = "262870947719@developer.gserviceaccount.com";

    /**
   * 
   */
    private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/home/tstephen/workspace/cloudcast/src/main/resources/50e5345d47fd186d1dc9e23f45beba514b01a72a-privatekey.p12";

    /**
     * Be sure to specify the name of your application. If the application name
     * is {@code null} or blank, the application will log a warning. Suggested
     * format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "knowprocess-cloudcast/1.0";

    private static final String UPLOAD_FILE_PATH = "/home/tstephen/workspace/cloudcast/src/main/resources/ic_mailboxes_accounts.png";
    private static final String DIR_FOR_DOWNLOADS = "downloads";
    private static final java.io.File UPLOAD_FILE = new java.io.File(
            UPLOAD_FILE_PATH);

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT; // = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global Drive API client. */
    private Drive drive;
    private File metadata;
    private boolean debug = true;

    public GDriveRepository() {
        init();
    }

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, GDriveRepository.class
                        .getResourceAsStream("/client_secrets.json"));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret()
                        .startsWith("Enter ")) {
            System.out
                    .println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
                            + "into drive-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up file credential store
        FileCredentialStore credentialStore = new FileCredentialStore(
                new java.io.File(System.getProperty("user.home"),
                        ".credentials/drive.json"), JSON_FACTORY);
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE))
                .setCredentialStore(credentialStore).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize("user");
    }

    public void init() {
        long start = new Date().getTime();
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            // authorization
            Credential credential = authorize();
            // set up the global Drive instance
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                    .setHttpRequestInitializer(credential)
                    .setApplicationName("knowprocess-cloudcast").build();

        } catch (Throwable e) {
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
            throw new RuntimeException(
                    "Unable to init access to GDrive, have you provided the secret?",
                    e);
        }
        System.out.println("initialisation took: "
                + (new Date().getTime() - start));
    }

    /** Uploads a file using either resumable or direct media upload. */
    private File uploadFile(boolean useDirectUpload) throws IOException {
        long start = new Date().getTime();

        File fileMetadata = new File();
        fileMetadata.setTitle(UPLOAD_FILE.getName());

        FileContent mediaContent = new FileContent("image/jpeg", UPLOAD_FILE);

        Drive.Files.Insert insert = drive.files().insert(fileMetadata,
                mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        // uploader.setProgressListener(new FileUploadProgressListener());
        File f = insert.execute();
        System.out.println("upload took: " + (new Date().getTime() - start));
        return f;
    }

    /** Updates the name of the uploaded file to have a "drivetest-" prefix. */
    private File updateFileWithTestSuffix(String id) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setTitle("drivetest-" + UPLOAD_FILE.getName());

        Drive.Files.Update update = drive.files().update(id, fileMetadata);
        return update.execute();
    }

    /** Downloads a file using either resumable or direct media download. */
    protected void downloadFile(java.io.File downloadsDir,
            boolean useDirectDownload, File uploadedFile) throws IOException {
        // create parent directory (if necessary)
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            throw new IOException("Unable to create parent directory");
        }
        OutputStream out = new FileOutputStream(new java.io.File(downloadsDir,
                uploadedFile.getTitle()));

        MediaHttpDownloader downloader = new MediaHttpDownloader(
                HTTP_TRANSPORT, drive.getRequestFactory().getInitializer());
        downloader.setDirectDownloadEnabled(useDirectDownload);
        // downloader.setProgressListener(new FileDownloadProgressListener());
        downloader.download(new GenericUrl(uploadedFile.getDownloadUrl()), out);
    }

    protected List<File> search(String q) throws IOException {
        List<File> result = new ArrayList<File>();
        Files.List request = drive.files().list();

        do {
            try {
                System.out.println("Searching for: " + q);
                FileList files = request.setQ(q).execute();

                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);

        return result;
    }

    @Override
    public void write(String resourceName, String mimeType, Date created,
            InputStream is) throws IOException {
        long start = new Date().getTime();

        metadata = new File();
        metadata.setTitle(resourceName);
        if (created != null) {
            metadata.setCreatedDate(new DateTime(created));
        }
        List<ParentReference> parents = new ArrayList<ParentReference>();
        // ParentReference ref = new ParentReference();
        // ref.setKind("drive#fileLink");
        // ref.setId("");
        // parents.add(ref);
        // metadata.setParents(parents);

        InputStreamContent content = new InputStreamContent(mimeType, is);

        if (search("title = '" + metadata.getTitle() + "'").size() == 0) {
            Drive.Files.Insert insert = drive.files().insert(metadata, content);
            MediaHttpUploader uploader = insert.getMediaHttpUploader();
            boolean useDirectUpload = true;
            uploader.setDirectUploadEnabled(useDirectUpload);
            uploader.setProgressListener(this);
            metadata = insert.execute();
            System.out.println("upload took: " + (new Date().getTime() - start)
                    + "ms");
        } else {
            System.out.println(String.format(
                    "'%1$s' already exists, skipping...", resourceName));
        }
    }

    /**
     * Only intended for use in testing!
     */
    protected File getMetadataOfLastUpload() {
        return metadata;
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        if (debug) {
            System.out.println("Uploaded: " + uploader.getNumBytesUploaded());
        }
    }
}
