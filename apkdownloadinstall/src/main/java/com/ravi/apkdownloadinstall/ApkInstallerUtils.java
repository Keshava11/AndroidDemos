package com.ravi.apkdownloadinstall;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by ravi on 12/12/17.
 */
class ApkInstallerUtils implements ApkConstants {

    static void installApk(Context iContext, File iFile) {
        // Note: Following ACTION_INSTALL_PACKAGE is supported since API 14. So for Android versions lower than 14,
        // Intent.ACTION_VIEW should be used.

        final Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        final Uri fileUri;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            fileUri = Uri.fromFile(iFile);
        } else {
            fileUri = FileProvider.getUriForFile(iContext, AUTHORITY, iFile);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.setDataAndType(fileUri, MIME_TYPE_APK);
        iContext.startActivity(intent);
    }

}
