/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adminactivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.CheckoutRequestsAdapter;
import kolblibrary.kolblibrary.adapters.ReserveRequestsAdapter;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;

/**
 * class ReserveRequestActivity provides a list of all pending reservation requests. This is an administrator function.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class ReserveRequestActivity extends BaseActivity {

    ListView mainView;

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_request);

        mainView = findViewById(R.id.ReserveRequestListView);
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent j = new Intent(ReserveRequestActivity.this, RequestViewActivity.class);
                Book book = (new ArrayList<>(AdminInfo.reserverequests.values())).get(i);
                j.putExtra("kolblibrary.kolblibrary.bookrequested", book);
                UserInfo info = (new ArrayList<>(AdminInfo.reserverequests.keySet()).get(i));
                j.putExtra("kolblibrary.kolblibrary.user", info);
                j.putExtra("kolblibrary.kolblibrary.type", 0);
                startActivity(j);
            }
        });

        List<String> urls = new ArrayList<String>();
        for(Map.Entry<UserInfo, Book> entry : AdminInfo.reserverequests.entrySet()) {
            urls.add(entry.getValue().getMediumImage());
        }
        UI.doLoadingDialog(this);
        new ReserveRequestActivity.GetImagesTask(urls).execute();
        // final CheckoutRequestsAdapter mba = new MyBooksAdapter(this, AdminInfo.checkoutrequests, new HashMap<Integer, Bitmap>());
        // MyBookView.setAdapter(mba);
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
         * This method is called when background processing is complete
         */
        protected void onPostExecute(HashMap<Integer, Bitmap> bookImages) {
            if(success) {
                final ReserveRequestsAdapter mba = new ReserveRequestsAdapter(ReserveRequestActivity.this, AdminInfo.reserverequests, bookImages);
                mainView.setAdapter(mba);
            }
            UI.closeLoadingDialog();
        }
    }
}
