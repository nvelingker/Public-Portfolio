package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.CatalogSearchAdapter;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.useractivities.BookViewActivity;


/**
 * class allows for users to see the results of their search query
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class CatalogSearchResult extends BaseActivity {

    ListView searchView;
    ArrayList<Book> booklist;
    HashMap<Integer, Bitmap> bookimages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_search_result);
        searchView = findViewById(R.id.searchView);
        booklist = (ArrayList<Book>) getIntent().getExtras().get("kolblibrary.kolblibrary.booklist");
        bookimages = (HashMap<Integer, Bitmap>) getIntent().getExtras().get("kolblibrary.kolblibrary.bookimages");
        final CatalogSearchAdapter csa = new CatalogSearchAdapter(this, booklist, bookimages);
        searchView.setAdapter(csa);

        // Called when an item is clicked
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CatalogSearchAdapter.instance.deactivate();
                Intent showBook = new Intent(getApplicationContext(), BookViewActivity.class);
                showBook.putExtra("kolblibrary.kolblibrary.bookclicked", booklist.get(i));
                startActivity(showBook);
            }

        });
    }
}
