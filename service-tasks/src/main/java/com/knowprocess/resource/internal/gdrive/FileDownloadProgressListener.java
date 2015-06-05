package com.knowprocess.resource.internal.gdrive;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;

/**
 * The File Download Progress Listener.
 *
 */
public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {

  @Override
  public void progressChanged(MediaHttpDownloader downloader) {
    switch (downloader.getDownloadState()) {
      case MEDIA_IN_PROGRESS:
        View.header2("Download is in progress: " + downloader.getProgress());
        break;
      case MEDIA_COMPLETE:
        View.header2("Download is Complete!");
        break;
    }
  }
}
