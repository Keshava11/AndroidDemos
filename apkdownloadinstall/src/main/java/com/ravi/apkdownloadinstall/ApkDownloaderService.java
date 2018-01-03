package com.ravi.apkdownloadinstall;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ravi on 12/12/17.
 */
class ApkDownloaderService implements ApkConstants {

    private static final String TAG = ApkDownloaderService.class.getSimpleName();
    private Context mContext;
    private Bundle mRequestBundle;
    private String mDownloadUrl;
    private File mLoadedFile;

    ApkDownloaderService(Context iContext, Bundle iRequestBundle) {
        mContext = iContext;
        mRequestBundle = iRequestBundle;

        mLoadedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
    }

    private DownloadManager getDownloadManager() {
        return (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private boolean validateRequestBundle() {
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    void downloadUsingAvailableDownloader() {
        // Getting an instance of downloader service
        final DownloadManager downloadManager = getDownloadManager();

        // Check request bundle and resolve the params
        if (validateRequestBundle()) {
            mDownloadUrl = mRequestBundle.getString(KEY_APK_URL);
            // TODO check for any other params
        }

        // Fallback check for a hardcoded mDownloadUrl
        if (mDownloadUrl == null) mDownloadUrl = APK_URL;

        // Check for downloader manager
        if (downloadManager != null) {

            Uri uri = Uri.parse(mDownloadUrl);

            final DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME);//uri.getLastPathSegment());
            downloadRequest.setMimeType(MIME_TYPE_APK);
            downloadRequest.allowScanningByMediaScanner();

            final long downloadId = downloadManager.enqueue(downloadRequest);

            // Create a broadcast receiver so that once download completes
            final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final Uri loaderUri = downloadManager.getUriForDownloadedFile(downloadId);
                    final String action = intent.getAction();

                    Log.v(TAG, "File downloaded :" + action + " ---- " + loaderUri);

                    if (action != null && loaderUri != null) {

                        switch (action) {
                            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                            case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                                try {
                                    ApkInstallerUtils.installApk(mContext, mLoadedFile);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Issue in launching apk");
                                }
                                break;
                        }
                    }
                }
            };

            final IntentFilter downloadClickFilter = new IntentFilter();
            downloadClickFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            downloadClickFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);

            // Need to register a broadcast receiver
            mContext.registerReceiver(downloadCompleteReceiver, downloadClickFilter);

        } else

            {
            // We will create a request using HttpUrlConnection

            new AsyncTask<Void, Integer, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {

                    try {
                        final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(mDownloadUrl).openConnection();

                        // Configure request headers and all
                        httpURLConnection.setRequestMethod(REQUEST_METHOD_GET);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(false); // Required only in case of POST and PUT

                        // Connect. Need to check its relevance
                        httpURLConnection.connect();

                        final InputStream is = httpURLConnection.getInputStream();
                        final InputStream es = httpURLConnection.getErrorStream();

                        if (is != null) {
                            // Create a buffer to read the file
                            byte[] buffer = new byte[4096];

                            File path = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS);

                            if (!path.exists())
                                //noinspection ResultOfMethodCallIgnored
                                path.mkdirs();

                            File file = new File(path, FILE_NAME);

                            FileOutputStream fos = new FileOutputStream(file);
                            int read;

                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }

                            fos.close();
                            is.close();

                            Log.v("TAG", "Done with file loading and saving");

                        } else if (es != null) {
                            es.close();

                            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(es));
                            final StringBuilder buffer = new StringBuilder();
                            String line;

                            while ((line = errorReader.readLine()) != null) {
                                buffer.append(line);
                            }

                            line = buffer.toString(); // error response read

                            Log.e(TAG, "Error response : " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    // Add the code to launch downloaded apk

                    try {
                        ApkInstallerUtils.installApk(mContext, mLoadedFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Issue launching apk");
                    }
                }
            }.execute();
        }
    }

}
