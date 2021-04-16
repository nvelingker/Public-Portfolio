package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.InputStream;
import java.util.Date;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.UserInfoCDetails;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.adapters.UserInfoRDetails;

/**
 * This class class shows a user that their reserve/checkout requests have been or have not been
 * recieved by the server and allows them to continue to their respective activities
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */


public class TransactionConfirmationActivity extends BaseActivity {


    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);

        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.bookchecked");
        final int type = (int) extras.get("kolblibrary.kolblibrary.type");
        final int duration = (int) extras.get("kolblibrary.kolblibrary.duration");

        final ImageView largeImageCover = findViewById(R.id.TransactionImg);
        new TransactionConfirmationActivity.DownloadImageTask(largeImageCover).execute(about.getLargeImage());

        TextView title = (TextView) findViewById(R.id.TransactionTitleView);
        TextView author = (TextView) findViewById(R.id.TransactionAuthorView);
        TextView ISBN = (TextView) findViewById(R.id.TransactionISBNView);
        TextView transactiontitle = (TextView) findViewById(R.id.TransactionMsg);
        TextView transactiondetails = (TextView) findViewById(R.id.TransactionDetailMsg);



        title.setText(title.getText() + about.getTitle());
        author.setText(author.getText() + about.getAuthor());
        ISBN.setText(ISBN.getText() + about.getISBN());

        Button share = (Button) findViewById(R.id.TransactionShareBtn);
        if(type == 0){
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TweetComposer.Builder builder = new TweetComposer.Builder(TransactionConfirmationActivity.this)
                            .text("I just reserved '" + about.getTitle() + "' by " + about.getAuthor() + "! I can't wait to pick it up!");
                    builder.show();
                }
            });

            String response = Networking.reserveBook(about, String.valueOf(duration));
            if(response == null) {
                Log.d("Message", "Success in reserving " + about.getTitle());
                transactiontitle.setText(R.string.transaction_reservation_title);
                transactiondetails.setText(R.string.transaction_reservation_detail);
                // success
            } else {
                Log.d("Message", "Failure response: " + response);
                UI.displayText(this, getString(R.string.reserve_failure_unknown));
            }
        }

        else if(type == 1){
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TweetComposer.Builder builder = new TweetComposer.Builder(TransactionConfirmationActivity.this)
                            .text("I just checked out '" + about.getTitle() + "' by " + about.getAuthor() + "! So excited to read!");
                    builder.show();
                }
            });
            String response = Networking.checkoutBook(about, String.valueOf(duration));
            if(response == null) {
                Log.d("Message", "Success in placing checkout " + about.getTitle());
                transactiontitle.setText(R.string.transaction_checkout_title);
                transactiondetails.setText(R.string.transaction_checkout_detail);
                // success
            } else {
                Log.d("Message", "Failure response: " + response);
                UI.displayText(this, getString(R.string.reserve_failure_unknown));
                transactiontitle.setText("Your checkout could not be processed.");
                transactiondetails.setText("Please see a librarian.");
            }
        }


        Button finish  = (Button) findViewById(R.id.TransactionFinishBtn);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type == 0){
                    Intent i = new Intent(TransactionConfirmationActivity.this, MyReservedActivity.class);
                    startActivity(i);
                }
                else if(type == 1){
                    Intent i = new Intent(TransactionConfirmationActivity.this, CatalogSearchActivity.class);
                    startActivity(i);
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
         * This method updates the Image when background processing is complete
         */
        protected void onPostExecute(Bitmap result) {
            view.setImageBitmap(result);
        }
    }
}


