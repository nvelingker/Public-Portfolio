/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.useractivities.UserHomeScreenActivity;


public class UI {
    private static ProgressDialog progressDialog = null;
    public static void hideKeyboard(Window window, Context ctx) {
        View view = window.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static void displayText(Activity currentActivity, String text) {
        Context context = currentActivity.getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    public static void networkTimedOut(Activity currentActivity) {
        displayText(currentActivity, currentActivity.getString(R.string.credentials_timeout));
    }
    public static void doLoadingDialog(Activity currentActivity) {
        progressDialog = new ProgressDialog(currentActivity);
        progressDialog.setMessage(currentActivity.getString(R.string.search_loading_body));
        progressDialog.setTitle(currentActivity.getString(R.string.search_loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public static void closeLoadingDialog() {
        progressDialog.hide();
    }
}
