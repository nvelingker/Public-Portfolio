package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.adapters.CatalogSearchAdapter;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.UI;


/**
 * This class allows for a user to search for a specific book
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class CatalogSearchActivity extends BaseActivity {

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CatalogSearchAdapter.instance != null) {
            CatalogSearchAdapter.instance.deactivate();
        }
        setContentView(R.layout.activity_catalog_search);

        final Spinner searchTypeSpinner = findViewById(R.id.searchTypeSpinner);
        final ArrayList<String> searchList = new ArrayList<>();
        searchList.add("Keyword");
        searchList.add("Title");
        searchList.add("Author");
        searchList.add("Subject");
        //searchList.add("Series");

        final ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, searchList);
        searchTypeSpinner.setAdapter(searchAdapter);

        final EditText searchtxt = findViewById(R.id.searchText);
        final Button search = findViewById(R.id.searchBtn);

        // called when search catalog button is clicked.
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CatalogSearchAdapter.instance != null) {
                    CatalogSearchAdapter.instance.deactivate();
                }
                UI.hideKeyboard(getWindow(), getApplicationContext());
                String stxt = searchTypeSpinner.getSelectedItem().toString();
                int index = searchList.indexOf(stxt);
                String search = searchtxt.getText().toString();
                if (!search.trim().isEmpty()) {
                    if(!isSearching) {
                        UI.doLoadingDialog(CatalogSearchActivity.this);
                        new DoSearchTask(index).execute(search);
                        isSearching = true;
                    }
                } else {
                    UI.displayText(CatalogSearchActivity.this, getString(R.string.blank_search));
                }
            }
        });
    }
    private boolean isSearching = false;

    /**
     * this is AsyncTask method to load book images asynchronously
     */

    public class DoSearchTask extends AsyncTask<String, Void, Void> {
        private int index;
        private boolean success = false;
        private ArrayList<Book> books = null;
        private HashMap<Integer, Bitmap> bookImages = null;
        public DoSearchTask(int index) {
            this.index = index;
        }

        /**
         * Run background thread to get search results
         */

        protected Void doInBackground(String... urls) {
            String search = urls[0];
            Log.d("Message", "Search = "+search);
            try {
                books = Networking.doSearch(index, search);
                success = true;

            } catch(Networking.NetworkTimedOutException ne) {
                ne.printStackTrace();
                UI.networkTimedOut(CatalogSearchActivity.this);
                isSearching = false;
            }
            bookImages = new HashMap<>();
            for(int i = 0; i < books.size(); i++) {
                try {
                    if(isSearching) {
                        Log.d("MessUrl", "id=" + i + " and url=" + books.get(i).getMediumImage());
                        String urldisplay = books.get(i).getMediumImage();
                        Bitmap mIcon11 = null;
                        try {
                            InputStream in = new java.net.URL(urldisplay).openStream();
                            mIcon11 = BitmapFactory.decodeStream(in);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }
                        bookImages.put(i, mIcon11);
                        Log.d("Message", (i+1) + "/" + books.size() + " complete");
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        /**
         * This method updates the CatalogSearchResult screen when background processing is complete
         */
        protected void onPostExecute(Void result) {
            if(isSearching) {
                isSearching = false;
                Intent resu = new Intent(CatalogSearchActivity.this, CatalogSearchResult.class);
                resu.putExtra("kolblibrary.kolblibrary.booklist", books);
                resu.putExtra("kolblibrary.kolblibrary.bookimages", bookImages);
                startActivity(resu);
            }
            UI.closeLoadingDialog();
        }
    }

}
