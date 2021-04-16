/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adminactivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;

/**
 * lass RequestViewActivity creates the Request View. Administrators can accept or decline the reservation.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class RequestViewActivity extends BaseActivity {


    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_view);

        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.bookrequested");
        final UserInfo user = (UserInfo) extras.get("kolblibrary.kolblibrary.user");
        final int type = (int) extras.get("kolblibrary.kolblibrary.type");

        final ImageView largeImageCover = findViewById(R.id.RequestImg);
        new RequestViewActivity.DownloadImageTask(largeImageCover).execute(about.getLargeImage());

        String title = about.getTitle();
        final String ISBN = about.getISBN();
        String author = about.getAuthor();
        String name = user.firstName + " " + user.lastName;

        if(type == 0){
            String prepare = name + " has reserved book '" + title + "' (ISBN: " + ISBN + ") by " + author + ".  Press DECLINE to remove the reservation.  Press ACCEPT to make the book available for pickup.";
            TextView main = (TextView) findViewById(R.id.ReturnStatementView);
            main.setText(prepare);
        }

        else if(type == 1){
            String prepare = name + " would like to check out book '" + title + "' (ISBN: " + ISBN + ") by " + author + ".  Press DECLINE to decline the checkout.  Press ACCEPT to accept the checkout.";
            TextView main = (TextView) findViewById(R.id.ReturnStatementView);
            main.setText(prepare);
        }

        Button decline = (Button) findViewById( R.id.GoogleCreateUserBtn);
        Button accept = (Button) findViewById(R.id.DeleteUserBtn);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NOTE: You don't actually need to differentiate between declining a reservation or checkout, the server can handle it the same way
              //  if(type == 0){
                    if(Networking.declineReserveOrCheckout(user.userName, ISBN)) {
                        UI.displayText(RequestViewActivity.this, "You have successfully declined the request.");
                        if(type == 0) {
                            AdminInfo.reserverequests.remove(user);
                            Intent i = new Intent(RequestViewActivity.this, ReserveRequestActivity.class);
                            Networking.update(RequestViewActivity.this, i);
                        } else if(type == 1) {
                            AdminInfo.checkoutrequests.remove(user);
                            Intent i = new Intent(RequestViewActivity.this, CheckoutRequestActivity.class);
                            Networking.update(RequestViewActivity.this, i);
                        }

                    } else {
                        UI.displayText(RequestViewActivity.this, "A server error has occurred. Please try again later.");
                    }
                    //if declining a reservation
               // }
                //else if(type == 1){
                //    //if declining a checkout
                //}
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type == 0){
                    if(Networking.approveReserve(user.userName, ISBN)) {
                        UI.displayText(RequestViewActivity.this, "You have successfully accepted the request for a reservation.");
                        AdminInfo.reserverequests.remove(user);
                        Intent i = new Intent(RequestViewActivity.this, ReserveRequestActivity.class);
                        Networking.update(RequestViewActivity.this, i);
                    } else {
                        UI.displayText(RequestViewActivity.this, "A server error has occurred. Please try again later.");
                    }
                    //if accepting a reservation
                }
                else if(type == 1){
                    if(Networking.approveCheckout(user.userName, ISBN)) {
                        UI.displayText(RequestViewActivity.this, "You have successfully accepted the request for checkout.");
                        AdminInfo.checkoutrequests.remove(user);
                        Intent i = new Intent(RequestViewActivity.this, CheckoutRequestActivity.class);
                        Networking.update(RequestViewActivity.this, i);
                    } else {
                        UI.displayText(RequestViewActivity.this, "A server error has occurred. Please try again later.");
                    }
                    //if accepting a checkout
                }
            }
        });


    }

    /**
     * this is AsyncTask method to load book image asynchronously
     */
    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView view;
        public DownloadImageTask(ImageView view) {
            this.view = view;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("MessUrl", "BV url="+urls[0]);
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        /**
         * This method creates screen when background processing is complete
         */
        protected void onPostExecute(Bitmap result) {
            view.setImageBitmap(result);
        }
    }
}
