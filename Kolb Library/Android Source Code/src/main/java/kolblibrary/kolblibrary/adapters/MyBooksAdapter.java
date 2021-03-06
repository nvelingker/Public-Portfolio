/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.objects.Book;



public class MyBooksAdapter extends CatalogSearchAdapter {

    public MyBooksAdapter(Context c, ArrayList<Book> b, HashMap<Integer, Bitmap> images) {
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
                v = LayoutInflater.from(getContext()).inflate(R.layout.catalog_mybookview_detail, null, false);
            }
	    // Find necessary views to display
            TextView Title = v.findViewById(R.id.Title);
            TextView Author = v.findViewById(R.id.Author);
            TextView DateChkdOut = v.findViewById(R.id.DateChkdOutView);
            TextView DaysLeft = v.findViewById(R.id.DaysLeftView);
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
            String DateChkedOut = "";
            UserInfoCDetails details = SelfUserInfo.checkedBooksInfo.get(book.getISBN());
            try {
                DateChkedOut = new SimpleDateFormat("MM-dd-yyyy").format(details.datechecked);
            }catch(Exception e){e.printStackTrace();}
            if(DateChkedOut.isEmpty()){
                DateChkedOut = "unavailable";
            }
            Log.d("MessageISBN", book.getISBN());
            int days = details.daysdue;
            String DayLeft = "Days until return: " + days;
            if(days < 0){
                days = days * (-1);
                DayLeft = "Overdue by " + days + " days";
                DaysLeft.setTextColor(Color.RED);
            }
            if(days >= 0){
                DaysLeft.setTextColor(Color.BLACK);
            }
            if(DayLeft.isEmpty()){
                DayLeft = "unavailable";
            }


            Title.setText("Title: " + title);
            DateChkdOut.setText("Date Checked Out: " + DateChkedOut);
            Author.setText("Author: " + author);
            DaysLeft.setText(DayLeft);
            if(!imageViews.contains(Image)) {
                new WaitForImageTask(Image, i)
                        .execute(bookImages);
                imageViews.add(Image);
            }
            return v;
        }


    }

