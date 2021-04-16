package kolblibrary.kolblibrary.adminactivities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.useractivities.HomeActivity;

/**
 * class ReturnBookActivity creates Book return screen. This is an administrator function.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */
public class ReturnBookActivity extends BaseActivity {

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);


        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.bookrequested");
        final UserInfo user = (UserInfo) extras.get("kolblibrary.kolblibrary.user");
        final int type = (int) extras.get("kolblibrary.kolblibrary.type");

        final ImageView largeImageCover = findViewById(R.id.RequestImg);
        new ReturnBookActivity.DownloadImageTask(largeImageCover).execute(about.getLargeImage());

        final String title = about.getTitle();
        final String ISBN = about.getISBN();
        String author = about.getAuthor();
        String name = user.firstName + " " + user.lastName;

        TextView returnview = (TextView) findViewById(R.id.ReturnStatementView);
        returnview.setText("Press the button below to mark '" + title + "' (ISBN: " + ISBN + "), checked out by " + name + ", as returned.");

        Button returnBtn = (Button) findViewById( R.id.ReturnBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String ret = "boi";
                    try{ret = Networking.returnBook(about, user);}catch(Exception e){e.printStackTrace();}
                if(ret == null) {
                    Intent i = new Intent(ReturnBookActivity.this, AllCheckoutsActivity.class);
                    UI.closeLoadingDialog();
                    Networking.update(ReturnBookActivity.this, i);
                    UI.displayText(ReturnBookActivity.this, "Successfully returned \"" + title + "\".");
                }
                else{
                        Log.d("beebwoop", ret);
                    UI.displayText(ReturnBookActivity.this, "Network failed to return book, please try again later.");
                }
                }catch(Exception e){e.printStackTrace();}
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
         * This method loads image when background processing is complete
         */
        protected void onPostExecute(Bitmap result) {
            view.setImageBitmap(result);
        }



    }
}
