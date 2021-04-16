package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.adapters.MyReservedAdapter;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;

/**
*class allows for a user to view their currently reserved books
*/

/**
 * This class allows for a user to view their currently reserved books
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */
public class MyReservedActivity extends BaseActivity {

    ListView MyReservedView;
    ArrayList<Book> booklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reserved);

        MyReservedView = findViewById(R.id.UserReservedListView);
        booklist = SelfUserInfo.reservedBooks;



        MyReservedView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showBook = new Intent(getApplicationContext(), BookViewActivity.class);
                showBook.putExtra("kolblibrary.kolblibrary.bookclicked", booklist.get(i));
                startActivity(showBook);
            }

        });
        List<String> urls = new ArrayList<>();
        for(int i = 0; i < booklist.size(); i++) {
            urls.add(booklist.get(i).getMediumImage());
        }
        UI.doLoadingDialog(this);
        new GetImagesTask(urls).execute();
    }

    /**
     * this is AsyncTask method to load book image asynchronously
     */
    public class GetImagesTask extends AsyncTask<Void, Void, HashMap<Integer, Bitmap>> {
        private boolean success = false;
        private List<String> urls;

        public GetImagesTask(List<String> urls) {
            this.urls = urls;
        }

        protected HashMap<Integer, Bitmap> doInBackground(Void... nulls) {
            HashMap<Integer, Bitmap> bookImages = new HashMap<>();
            for(int i = 0; i < urls.size(); i++) {
                try {
                    String url = urls.get(i);
                    Log.d("MessUrl", "id=" + i + " and url=" + url);
                    Bitmap mIcon11 = null;
                    try {
                        InputStream in = new java.net.URL(url).openStream();
                        mIcon11 = BitmapFactory.decodeStream(in);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                    bookImages.put(i, mIcon11);
                    Log.d("Message", (i+1) + "/" + urls.size() + " complete");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            success = true;
            return bookImages;
        }

        /**
         * This method updates the screen when background processing is complete
         */
        protected void onPostExecute(HashMap<Integer, Bitmap> bookImages) {
            if(success) {
                final MyReservedAdapter mba = new MyReservedAdapter(MyReservedActivity.this, booklist, bookImages);
                MyReservedView.setAdapter(mba);
            }
            UI.closeLoadingDialog();
        }
    }
}
