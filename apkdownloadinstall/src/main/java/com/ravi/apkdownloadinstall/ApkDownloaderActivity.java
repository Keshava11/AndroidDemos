package com.ravi.apkdownloadinstall;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class ApkDownloaderActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERM_EXTERNAL_STORAGE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_downloader);

        // Update and apk installer
        findViewById(R.id.update_apk_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request permission
                final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                final Activity context = ApkDownloaderActivity.this;

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    configureApiCall();
                } else {
                    ActivityCompat.requestPermissions(context, permissions,
                            REQUEST_CODE_PERM_EXTERNAL_STORAGE);
                }
            }
        });
    }

    /**
     * Configure api call
     */
    private void configureApiCall() {
        final Bundle bundle = new Bundle();
        bundle.putString(ApkConstants.KEY_APK_URL, ApkConstants.APK_URL);

        // Start loading and install service
        new ApkDownloaderService(ApkDownloaderActivity.this, bundle)
                .downloadUsingAvailableDownloader();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERM_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureApiCall();
                } else {
                    Log.e("TAG", "Permission not granted");
                }
                break;
        }
    }
}
