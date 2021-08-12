package com.rebel.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@TargetApi(value = Build.VERSION_CODES.M)
public class BaseActivity extends Activity {

    static SafeActionPerformer.ActionPerformer actionPerformer;
    String positiveButtonName = "Continue";
    String negativeButtonName = "Close";
//  -----------------------------------Constants---------------------------------------------------
    public static final String PERMISSIONS_EXTRA = "PERMISSIONS_EXTRA";
    public static final String RATIONALE_EXTRA = "RATIONALE_EXTRA";
    private final int RQ_PERMISSIONS = 9091;
    private final int RQ_SETTINGS = 9092;

//  -----------------------------------Intent Extras------------------------------------------------
    String[] requiredPermissions;
    String rationaleInfo;

    List<String> unavailablePermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        getWindow().setStatusBarColor(0);

        Intent intent = getIntent();
        if (intent == null) finish();
        requiredPermissions = intent.getStringArrayExtra(PERMISSIONS_EXTRA);
        rationaleInfo = intent.getStringExtra(RATIONALE_EXTRA);

        unavailablePermissions = new ArrayList<>();

//      For a fresh application we don't have to showRationale
        boolean showRationale = false;

//      Get unavailable Permissions List and also check if we are asking permissions for the first time or the second time
//      shouldShowRationale will be false for a fresh application and for blocked permissions
//        Toast.makeText(this, SafeActionPerformer.getRequiredPermissionsString(Arrays.asList(requiredPermissions)) + " permissions are required", Toast.LENGTH_SHORT).show();
        for (String permission : requiredPermissions) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                unavailablePermissions.add(permission);
                if (shouldShowRequestPermissionRationale(permission)){
//                  It means the user is confused and we are asking permissions for the second time
                    showRationale = true;
                }
            }
        }

//      If all permissions Granted.. doTask()
        if (unavailablePermissions.isEmpty())
            doTask();

//      If permission is being asked for the second time, showRationale
        if (showRationale) {
            SafeActionPerformer.showDialog(this, rationaleInfo,
                    new SafeActionPerformer.DialogButton(positiveButtonName, (dialog, which) -> requestPermissions(unavailablePermissions.toArray(new String[0]), RQ_PERMISSIONS)),
                    new SafeActionPerformer.DialogButton(negativeButtonName, (dialog, which) ->  { dialog.dismiss(); finish(); })
            );
//            Toast.makeText(this, "Second time", Toast.LENGTH_SHORT).show();
        } else {
//          Asking permissions directly because it is our first time
            requestPermissions(unavailablePermissions.toArray(new String[0]), RQ_PERMISSIONS);
//            Toast.makeText(this, "First time", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Toast.makeText(this, Arrays.toString(grantResults).replace("0", "Granted, ").replace("-1", "Denied, "), Toast.LENGTH_SHORT).show();

        if (requestCode == RQ_PERMISSIONS && grantResults.length > 0) {
//          Check how many permissions were denied
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    deniedPermissions.add(permissions[i]);
            }

//          If none of the asked permissions were denied. Therefore, all permissions were granted.. doTask()
            if (deniedPermissions.size() == 0)
                doTask();
            else {
//              If some of the permissions were denied
                ArrayList<String> justDenied = new ArrayList<>();
                ArrayList<String> justBlocked = new ArrayList<>();      //      Just set not to ask again.

                for (String permission : deniedPermissions) {

                    if (shouldShowRequestPermissionRationale(permission)) {
                        justDenied.add(permission);             //  Just denied
//                        Toast.makeText(this, permission + " just denied", Toast.LENGTH_SHORT).show();
                    } else {
//                        if (!permission.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) { // This permission has not been implemented yet
                            justBlocked.add(permission);            //  Just blocked (just set never ask again)
//                            Toast.makeText(this, permission + " just blocked", Toast.LENGTH_SHORT).show();
//                        }
                    }
                }

                String perms = "";
                if (justDenied.size() > 0) {
                    perms += SafeActionPerformer.getRequiredPermissionsString(justDenied);
//                    Toast.makeText(this, justDenied.size() + " permissions were denied", Toast.LENGTH_SHORT).show();
                }

                if (justDenied.size() > 0 || justBlocked.size() > 0) {
                    perms += SafeActionPerformer.getRequiredPermissionsString(justBlocked);
                    showSettingsDialog(perms);
//                    Toast.makeText(this, justBlocked.size() + " permissions were blocked", Toast.LENGTH_SHORT).show();
                }
                else finish();
            }
        }
    }

    private void showSettingsDialog(String permissions) {
        SafeActionPerformer.showDialog(this, permissions + " permission(s) are required by this application in order to work. Please enable this/these permission(s) from settings",
                new SafeActionPerformer.DialogButton(positiveButtonName, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, RQ_SETTINGS);
        }), new SafeActionPerformer.DialogButton(negativeButtonName, (dialog, which) -> {
            dialog.dismiss();
            finish();
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_SETTINGS && actionPerformer != null) {
            SafeActionPerformer.performAction(this, requiredPermissions, rationaleInfo, actionPerformer);
        }
        // super, because overridden method will make the handler null, and we don't want that.
        super.finish();
    }

    private void doTask() {
        if (actionPerformer != null) {
            actionPerformer.doTask();
            finish();
        }
    }

}
