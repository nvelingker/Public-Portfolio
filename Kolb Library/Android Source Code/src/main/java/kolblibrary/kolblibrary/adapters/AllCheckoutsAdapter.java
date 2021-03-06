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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;



public class AllCheckoutsAdapter extends CatalogSearchAdapter {

    private LinkedHashMap<UserInfo, Book> info;
    public AllCheckoutsAdapter(Context C, LinkedHashMap<UserInfo, Book> a, HashMap<Integer, Bitmap> images){
        super(C, a, images);
        info = a;
    }
    /**
    * @return The UserInfo object associated with a book that has been checked out by some user.
    */
    private UserInfo getInfoFrom(Book book) {
        for(Map.Entry<UserInfo, Book> entry : info.entrySet()) {
            if(entry.getValue().equals(book)) {
                return entry.getKey();
            }
        }
        return null;
    }
    /**
     * @param position
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
            v = LayoutInflater.from(getContext()).inflate(R.layout.catalog_all_checkout_detail, null, false);
        }
	// Find necessary views to display
        TextView Title = v.findViewById(R.id.Title);
        TextView Author = v.findViewById(R.id.Author);
        TextView CheckoutNameView = v.findViewById(R.id.CheckoutNameView);
        ImageView Image =  v.findViewById(R.id.bookImg);
        Book book = booklist.get(i);
        UserInfo uinfo = getInfoFrom(book);
	
	// Populate the views with information
        String title = book.getTitle();
        if(title.isEmpty()){
            title = "unavailable";
        }
        String author = book.getAuthor();
        if(author.isEmpty()){
            author = "unavailable";
        }
        String Name = "";
        try {
            Name = uinfo.firstName + " " + uinfo.lastName;
        }catch(Exception e){}



        Title.setText("Title: " + title);
        Title.setTextColor(viewGroup.getResources().getColor(R.color.colorPrimaryDark));
        CheckoutNameView.setText("Checked Out By: " + Name);
        CheckoutNameView.setTextColor(viewGroup.getResources().getColor(R.color.colorPrimaryDark));
        Author.setText("Author: " + author);
        Author.setTextColor(viewGroup.getResources().getColor(R.color.colorPrimaryDark));
        if(!imageViews.contains(Image)) {
		// Start task to attach image to view when ready
            new WaitForImageTask(Image, i)
                    .execute(bookImages);
            imageViews.add(Image);
        }
        return v;
    }


}
