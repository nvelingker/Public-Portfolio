package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.InputStream;

import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;

/**
 * This class creates the BookView screen.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */
public class BookViewActivity extends BaseActivity {


    private int status = 0;

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_view);
        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.bookclicked");
        final ImageView largeImageCover = findViewById(R.id.largeImageCover);
        new DownloadImageTask(largeImageCover).execute(about.getLargeImage());
        final Button reserveBtn = findViewById(R.id.reserveBtn);
        final Button CheckoutButton = findViewById(R.id.CheckOutBtn);
        Log.d("statusboi", about.getStatus());
        reserveBtn.setText(getString(R.string.reserve_button_reserve));
        CheckoutButton.setText(getString(R.string.checkout_button_checkout));
        LinearLayout warningSign = findViewById(R.id.WarningLayout);
        status = 0; // 0: not reserved and not checked out; 1: unavailable; 2: reserved; 3 = checked out

        if(about.getStatus().startsWith("0", 0)){
            reserveBtn.setText(getString(R.string.reserve_button_waitlist));
            CheckoutButton.setEnabled(false);
            status = 1;
        }


        String isbn = about.getISBN();
        for(int i = 0; i < SelfUserInfo.reservedBooks.size(); i++) {
            if (isbn.equals(SelfUserInfo.reservedBooks.get(i).getISBN())) {
                reserveBtn.setText(getString(R.string.reserve_button_unreserve));
                CheckoutButton.setEnabled(false);
                status = 2;
            }
        }
        for(int i = 0; i < SelfUserInfo.checkedBooks.size(); i++) {
            if(isbn.equals(SelfUserInfo.checkedBooks.get(i).getISBN())){
                status = 3;

                CheckoutButton.setText("Already Checked Out");
                reserveBtn.setText("Cannot Reserve");
                CheckoutButton.setEnabled(false);
                reserveBtn.setEnabled(false);
                break;
            }
        }
        if((SelfUserInfo.checkedBooksInfo.containsKey(about.getISBN())) && SelfUserInfo.checkedBooksInfo.get(about.getISBN()).daysdue < 0) {
            warningSign.setVisibility(View.VISIBLE);
        } else {
            warningSign.setVisibility(View.INVISIBLE);
        }

        //  Reserve button listener
        reserveBtn.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                if(BookViewActivity.this.status == 0){
                    /*String response = Networking.reserveBook(about);
                    if(response == null) {
                        Log.d("Message", "Success in reserving " + about.getTitle());
                        UI.displayText(BookViewActivity.this, getString(R.string.reserve_success, about.getTitle()));
                        reserveBtn.setText(getString(R.string.reserve_button_unreserve));
                        CheckoutButton.setEnabled(false);
                        BookViewActivity.this.status = 2;
                        // success
                    } else {
                        Log.d("Message", "Failure response: " + response);
                        if(response.equalsIgnoreCase("unavailable")) {
                            UI.displayText(BookViewActivity.this, getString(R.string.reserve_failure_unavailable));
                        } else if(response.equalsIgnoreCase("alreadyholding")) {
                            UI.displayText(BookViewActivity.this, getString(R.string.reserve_failure_holding));
                        } else {
                            UI.displayText(BookViewActivity.this, getString(R.string.reserve_failure_unknown));
                        }
                    }*/
                    Intent temp = new Intent(BookViewActivity.this, GetPswdActivity.class);
                    temp.putExtra("kolblibrary.kolblibrary.bookchecked", about);
                    temp.putExtra("kolblibrary.kolblibrary.transactiontype", 0);
                    startActivity(temp);

                }
                else if(BookViewActivity.this.status == 2) {
                    String response = Networking.unreserveBook(about);
                    if(response == null) {
                        Log.d("Message", "Success in un-reserving " + about.getTitle());
                        UI.displayText(BookViewActivity.this, getString(R.string.unreserve_success, about.getTitle()));
                        reserveBtn.setText(getString(R.string.reserve_button_reserve));
                        CheckoutButton.setEnabled(true);
                        BookViewActivity.this.status = 0;
                        // success
                    } else {
                        Log.d("Message", "Failure response: " + response);
                        UI.displayText(BookViewActivity.this, getString(R.string.unreserve_failure_unknown));
                    }

                }
        }
        });


                //  Checkout button listener
                CheckoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent temp = new Intent(BookViewActivity.this, GetPswdActivity.class);
                        temp.putExtra("kolblibrary.kolblibrary.bookchecked", about);
                        temp.putExtra("kolblibrary.kolblibrary.transactiontype", 1);
                        startActivity(temp);
                    }
                });

                Button share = (Button) findViewById(R.id.BookViewShareBtn);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TweetComposer.Builder builder = new TweetComposer.Builder(BookViewActivity.this)
                                .text("I love the book '" + about.getTitle() + "' by " + about.getAuthor() + "!");
                        builder.show();
                    }
                });

                TextView TitleView = findViewById(R.id.TitleView);
                TitleView.setText(TitleView.getText() + ": " + about.getTitle());

                TextView AuthorView = findViewById(R.id.AuthorView);
                AuthorView.setText(AuthorView.getText() + ": " + about.getAuthor());

                TextView StatusView = findViewById(R.id.AvailabilityView);
                StatusView.setText(StatusView.getText() + ": " + about.getStatus());

                /*String dua = about.getDaysUntilAvailable();
                if(!dua.trim().isEmpty() && Integer.parseInt(about.getDaysUntilAvailable()) > 1) {
                    StatusView.setText(StatusView.getText() + "\nApproximately " + about.getDaysUntilAvailable() + " days until available");
                }else if(!dua.trim().isEmpty() && Integer.parseInt(about.getDaysUntilAvailable()) == 1) {
                    StatusView.setText(StatusView.getText() + "\nApproximately 1 day until available");
                }
                else if(!dua.trim().isEmpty() && Integer.parseInt(about.getDaysUntilAvailable()) < 1) {
                    StatusView.setText(StatusView.getText() + "Unknown");
                }*/

                TextView SubjectView = findViewById(R.id.SubjectView);
                SubjectView.setText(SubjectView.getText() + ": " + about.getSubject());

                TextView PublisherView = findViewById(R.id.PublisherView);
                PublisherView.setText(PublisherView.getText() + ": " + about.getPublisher());

                TextView YearView = findViewById(R.id.YearView);
                YearView.setText(YearView.getText() + ": " + about.getPublishYear());

                TextView ISBNView = findViewById(R.id.ISBNView);
                ISBNView.setText(ISBNView.getText() + ": " + about.getISBN());


                TextView SummaryView = findViewById(R.id.SummaryView);
                SummaryView.setText(SummaryView.getText() + ": " + about.getDesc());

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