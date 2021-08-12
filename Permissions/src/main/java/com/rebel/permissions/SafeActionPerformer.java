package com.rebel.permissions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.*;

@SuppressWarnings("unused")
public class SafeActionPerformer {

    public static void performAction(Context context, String[] permissions, String rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, permissions, false, rationaleInfo, actionPerformer);
    }

    public static void performAction(Context context, String[] permissions, int rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, permissions, false, context.getString(rationaleInfo), actionPerformer);
    }

    public static void performAction(Context context, String permission, String rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, new String[]{permission}, false, rationaleInfo, actionPerformer);
    }

    public static void performAction(Context context, String permission, int rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, new String[]{permission}, false, context.getString(rationaleInfo), actionPerformer);
    }

    public static void performAction(Context context, String permission, boolean enablePermissionString, String rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, new String[]{permission}, enablePermissionString, rationaleInfo, actionPerformer);
    }

    public static void performAction(Context context, String permission, boolean enablePermissionString, int rationaleInfo, ActionPerformer actionPerformer) {
        performAction(context, new String[]{permission}, enablePermissionString, context.getString(rationaleInfo), actionPerformer);
    }

    public static void performAction(Context context, String[] permissions, boolean enablePermissionString, String rationaleInfo, ActionPerformer actionPerformer) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//          Below marshmello permissions are already granted
            actionPerformer.doTask();
        } else {
//          Above marshmello we will have to ask permissions
//          Removing duplicate permissions in the list
            Set<String> permissionsSet = new HashSet<>();
            Collections.addAll(permissionsSet, permissions);

//          Check if all the required permissions are already available or not
            boolean arePermissionAvailable = true;
            for (String permission : permissionsSet) {
                if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    arePermissionAvailable = false;
                    break;
                }
            }

//          Permissions available.. doTask()
            if (arePermissionAvailable) {
                actionPerformer.doTask();
//              This line is required because this function is also being called from the BaseActivity class so it is necessary to nullify the BaseActivity
                BaseActivity.actionPerformer = null;
            } else {
//              Permissions not available ask for it
                BaseActivity.actionPerformer = actionPerformer;

                Intent intent = new Intent(context, BaseActivity.class);
                intent.putExtra(BaseActivity.PERMISSIONS_EXTRA, permissionsSet.toArray(new String[0]));
                intent.putExtra(BaseActivity.RATIONALE_EXTRA, enablePermissionString ?
                                rationaleInfo.replace("{%permissions%}",
                                getRequiredPermissionsString(new ArrayList<>(permissionsSet))) :
                                rationaleInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    public static String getRequiredPermissionsString(List<String> justBlockedPermissions) {
        StringBuilder builder = new StringBuilder();
        for (String permission : justBlockedPermissions)
            builder.append(permission.replace("android.permission.", "").concat(", "));
        return builder.length() > 2 ? builder.substring(0, builder.length() - 2): builder.toString();
    }

    public static void showDialog(Context context, String info, DialogButton positiveButton, DialogButton negativeButton ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(info);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButton.getName(), positiveButton.getOnClickListener());
        builder.setNegativeButton(negativeButton.getName(), negativeButton.getOnClickListener());
        builder.show();
    }

    public interface ActionPerformer {
        void doTask();
    }

    static class DialogButton {
        private String name;
        private DialogInterface.OnClickListener onClickListener;

        public DialogButton(String name, DialogInterface.OnClickListener onClickListener) {
            this.name = name;
            this.onClickListener = onClickListener;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DialogInterface.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

    }

}
