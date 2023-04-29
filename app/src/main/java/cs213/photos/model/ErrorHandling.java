package cs213.photos.model;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class ErrorHandling {
    public static void alertDialog(Context context, Exception e) {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle("Error occurred");
        messageBox.setMessage(e.toString());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
}
