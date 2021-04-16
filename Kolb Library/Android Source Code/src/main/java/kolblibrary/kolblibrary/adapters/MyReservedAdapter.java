/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.CatalogSearchAdapter;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.objects.Book;



public class MyReservedAdapter extends CatalogSearchAdapter {
    public MyReservedAdapter(Context c, ArrayList<Book> b, HashMap<Integer, Bitmap> images) {
        super(c, b, images);
    }

    /**
     *
     * @param i
     * @param v
     * @param viewGroup
     * @return The View associated with the given index from this Adapter fitting a ListView.
     */
    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        if(imageViews == null) {
            imageViews = new ArrayList<>();
        }
        Log.d("Message", "thing" + i);
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.catalog_myreservedview_detail, null, false);
        }
	// Find necessary views to display
        TextView Title = v.findViewById(R.id.Title);
        TextView Author = v.findViewById(R.id.Author);
        TextView DateChkdOut = v.findViewById(R.id.DateReservedView);
        TextView DaysLeft = v.findViewById(R.id.IsAvailableView);
        ImageView Image =  v.findViewById(R.id.bookImg);

     	// Populate views with information
        Book book = booklist.get(i);


        String title = book.getTitle();
        if(title.isEmpty()){
            title = "unavailable";
        }
        String author = book.getAuthor();
        if(author.isEmpty()){
            author = "unavailable";
        }
        String DateReserved = "";
        try {
            DateReserved = new SimpleDateFormat("MM-dd-yyyy").format(SelfUserInfo.reservedBookInfo.get(book.getISBN()).datechecked);
        } catch(Exception e) {}
        if(DateReserved.isEmpty()){
            DateReserved = "unavailable";
        }
        String availablity = SelfUserInfo.reservedBookInfo.get(book.getISBN()).availability;



        Title.setText("Title: " + title);
        DateChkdOut.setText("Date Reserved: " + DateReserved);
        Author.setText("Author: " + author);
        DaysLeft.setText("Availability: " + availablity);
        if(!imageViews.contains(Image)) {
            new WaitForImageTask(Image, i)
                    .execute(bookImages);
            imageViews.add(Image);
        }
        return v;
    }


}
