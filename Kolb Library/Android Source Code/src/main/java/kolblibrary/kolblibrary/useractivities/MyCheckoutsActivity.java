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
import kolblibrary.kolblibrary.adapters.MyBooksAdapter;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;
/**
 * This is an activity class that allows for a user to view the books they currently have checked out
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
*/
public class MyCheckoutsActivity extends BaseActivity {

    ListView MyBookView;
    ArrayList<Book> booklist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books_results);
        MyBookView = findViewById(R.id.MyBookView);
        booklist = SelfUserInfo.checkedBooks;


        /**
         * this method is called when an item is selected
         */

        MyBookView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

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
         * This method creates screen when background processing is complete
         */
        protected void onPostExecute(HashMap<Integer, Bitmap> bookImages) {
            if(success) {
                final MyBooksAdapter mba = new MyBooksAdapter(MyCheckoutsActivity.this, booklist, bookImages);
                MyBookView.setAdapter(mba);
            }
            UI.closeLoadingDialog();
        }
    }
}
