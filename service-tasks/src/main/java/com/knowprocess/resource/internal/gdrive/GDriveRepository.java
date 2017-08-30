package com.knowprocess.resource.internal.gdrive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

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
import com.knowprocess.resource.spi.Repository;
import com.knowprocess.resource.spi.RestService;

/**
 * Store resource in Google Drive.
 *
 * <p>
 * NOT thread-safe.
 *
 * @author Tim Stephenson
 * 
 */
public class GDriveRepository implements Repository,
        MediaHttpUploaderProgressListener {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(GDriveRepository.class);

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global Drive API client. */
    private Drive drive;
    private File metadata;
    private String url;
    private boolean debug = true;

    @Value("${omny.app.dir:.}")
    private String appDir;

	public GDriveRepository() throws IOException {
        init();
    }

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws Exception {
        java.io.File secretFile = new java.io.File(appDir, ".goog_secret.json");
		if (secretFile == null || !secretFile.exists()) {
			throw new IllegalStateException(
					String.format("No client secret found in %1$s", secretFile.getCanonicalPath()));
		}
		java.io.File tokenFile = new java.io.File(appDir, ".goog_token.json");
		if (tokenFile == null || !tokenFile.exists()) {
			throw new IllegalStateException(
					String.format("No credentials found in %1$s", tokenFile.getCanonicalPath()));
		}

		InputStream secretStream = null;
		try {
			secretStream = new FileInputStream(secretFile);
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
	                JSON_FACTORY, secretStream);
			FileCredentialStore credentialStore = new FileCredentialStore(
	                tokenFile, JSON_FACTORY);
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
	                Collections.singleton(DriveScopes.DRIVE_FILE))
	                .setCredentialStore(credentialStore)
	                .setAccessType("offline").build();
	        return new AuthorizationCodeInstalledApp(flow,
	                new LocalServerReceiver()).authorize("user");
		} finally {
			secretStream.close();
		}
    }

	public void init() throws IOException {
        long start = new Date().getTime();
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize();
            // set up the global Drive instance
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                    .setHttpRequestInitializer(credential)
					.setApplicationName(RestService.USER_AGENT).build();

		} catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
			throw new GDriveConfigurationException(
                    "Unable to init access to GDrive, have you provided the secret?",
                    e);
        }
        LOGGER.info("initialisation took: "
                + (new Date().getTime() - start));
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
                LOGGER.info("Searching for: " + q);
                FileList files = request.setQ(q).execute();

                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                LOGGER.error("An error occurred: " + e);
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

        InputStreamContent content = new InputStreamContent(mimeType, is);

        if (search("title = '" + metadata.getTitle() + "'").size() == 0) {
            Drive.Files.Insert insert = drive.files().insert(metadata, content);
            MediaHttpUploader uploader = insert.getMediaHttpUploader();
            boolean useDirectUpload = true;
            uploader.setDirectUploadEnabled(useDirectUpload);
            uploader.setProgressListener(this);
            metadata = insert.execute();
            url = metadata.getAlternateLink();
            LOGGER.info("upload took: " + (new Date().getTime() - start)
                    + "ms");
        } else {
            LOGGER.warn(String.format(
                    "'%1$s' already exists, skipping...", resourceName));
        }
    }

    /**
     * Only intended for use in testing!
     */
    protected File getMetadataOfLastUpload() {
        return metadata;
    }

    /**
     * @return The last object written to this repo cast to a String.
     */
    public String getDriveUrl() {
        return (String) url;
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        if (debug) {
            LOGGER.info("Uploaded: " + uploader.getNumBytesUploaded());
        }
    }
}
