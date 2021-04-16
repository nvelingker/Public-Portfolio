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
import java.util.LinkedHashMap;
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.objects.Book;



public class ReserveRequestsAdapter extends CatalogSearchAdapter {

    private LinkedHashMap<UserInfo, Book> info;
    public ReserveRequestsAdapter(Context C, LinkedHashMap<UserInfo, Book> a, HashMap<Integer, Bitmap> images){
        super(C, a, images);
        info = a;
    }
    private UserInfo getInfoFrom(Book book) {
        for(Map.Entry<UserInfo, Book> entry : info.entrySet()) {
            if(entry.getValue().equals(book)) {
                return entry.getKey();
            }
        }
        return null;
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
            v = LayoutInflater.from(getContext()).inflate(R.layout.catalog_request_detail, null, false);
        }
	// Find necessary views to display
        TextView Title = v.findViewById(R.id.Title);
        TextView Author = v.findViewById(R.id.Author);
        TextView ReqNameView = v.findViewById(R.id.RequestNameView);
        TextView DateReqView = v.findViewById(R.id.DateRequestedView);
        ImageView Image =  v.findViewById(R.id.bookImg);

     	// Populate views with information
        Book book = booklist.get(i);
        UserInfo uinfo = getInfoFrom(book);


        String title = book.getTitle();
        if(title.isEmpty()){
            title = "unavailable";
        }
        String author = book.getAuthor();
        if(author.isEmpty()){
            author = "unavailable";
        }
        String ReqName = "";
        try {
            ReqName = uinfo.firstName + " " + uinfo.lastName;
        }catch(Exception e){}
        String DateReq = "";
        try {
            DateReq = new SimpleDateFormat("MM-dd-yyyy").format(uinfo.dateRequested);
        } catch(Exception e) {}
        if(DateReq.isEmpty()){
            DateReq = "unavailable";
        }

        Title.setText("Title: " + title);
        DateReqView.setText("Date Requested: " + DateReq);
        Author.setText("Author: " + author);
        ReqNameView.setText("Requested by: " + ReqName);
        if(!imageViews.contains(Image)) {
            new WaitForImageTask(Image, i)
                    .execute(bookImages);
            imageViews.add(Image);
        }
        return v;
    }

}
