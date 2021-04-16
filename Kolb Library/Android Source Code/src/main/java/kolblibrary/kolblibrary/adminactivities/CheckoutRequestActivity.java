/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adminactivities;

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
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.adapters.CheckoutRequestsAdapter;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.objects.Book;

/**
 * class CheckoutRequestActivity provides a list of all pending checkout requests. This is an administrator function.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class CheckoutRequestActivity extends BaseActivity{

    ListView mainView;

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_requests);

        mainView = findViewById(R.id.CheckoutRequestView);
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent j = new Intent(CheckoutRequestActivity.this, RequestViewActivity.class);
                Book book = (new ArrayList<>(AdminInfo.checkoutrequests.values())).get(i);
                j.putExtra("kolblibrary.kolblibrary.bookrequested", book);
                UserInfo info = (new ArrayList<>(AdminInfo.checkoutrequests.keySet()).get(i));
                j.putExtra("kolblibrary.kolblibrary.user", info);
                j.putExtra("kolblibrary.kolblibrary.type", 1);
                startActivity(j);
            }
        });

        List<String> urls = new ArrayList<String>();
        for(Map.Entry<UserInfo, Book> entry : AdminInfo.checkoutrequests.entrySet()) {
            urls.add(entry.getValue().getMediumImage());
        }
        UI.doLoadingDialog(this);
        new GetImagesTask(urls).execute();
    }

    /**
     * this is AsyncTask method to load book images asynchronously
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
         * This method updates the Images when background processing is complete
         */
        protected void onPostExecute(HashMap<Integer, Bitmap> bookImages) {
            if(success) {
                final CheckoutRequestsAdapter mba = new CheckoutRequestsAdapter(CheckoutRequestActivity.this, AdminInfo.checkoutrequests, bookImages);
                mainView.setAdapter(mba);
            }
            UI.closeLoadingDialog();
        }
    }
}
