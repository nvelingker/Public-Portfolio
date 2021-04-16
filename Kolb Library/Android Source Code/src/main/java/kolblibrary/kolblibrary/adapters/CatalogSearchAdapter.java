/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/

package kolblibrary.kolblibrary.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.objects.Book;



public class CatalogSearchAdapter extends ArrayAdapter<Book> {

    LayoutInflater mInflater;
    ArrayList<Book> booklist;
    HashMap<Integer, Bitmap> bookImages;
    private boolean isActive = true;
    public static CatalogSearchAdapter instance;
    public CatalogSearchAdapter(Context c, ArrayList<Book> b, HashMap<Integer, Bitmap> bookimages) {
        super(c, 0, b);
        instance = this;
        booklist = b;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bookImages = bookimages;
    }
    public CatalogSearchAdapter(Context c, HashMap<UserInfo, Book> a , HashMap<Integer, Bitmap> images) {
        super(c, 0, new ArrayList<Book>());
        ArrayList<Book> b = new ArrayList<Book>();
        for(Map.Entry<UserInfo, Book> entry : a.entrySet()) {
	    // Populate list of Books to display even if not directly given
            b.add(entry.getValue());
        }
        instance = this;
        booklist = b;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bookImages = images;
    }
    public void deactivate() {
        isActive = false;
    }
    ArrayList<ImageView> imageViews;
    @Override
    public int getCount() {
        if(booklist == null){
            Log.d("message", "the booklist is empty");
            return 0;
        }
        return booklist.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
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
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.catalog_searchview_detail, null, false);
        }
	// Find necessary views to display
        TextView Title = v.findViewById(R.id.Title);
        TextView Author = v.findViewById(R.id.Author);
        TextView Availability = v.findViewById(R.id.Availability);
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
        String availability = book.getStatus();
        if(availability.isEmpty()){
            availability = "unavailable";
        }
        String uriStr = book.getMediumImage();
        if(uriStr.isEmpty()) {
	    // set URI String to blank image so that the app doesn't attempt to parse blank string
            uriStr = "http://ericasadun.com/wp-content/uploads/2013/04/f.png";
        }



        Title.setText("Title: " + title);
        Availability.setText("Availability: " + availability);
        Author.setText("Author: " + author);
        if(bookImages.containsKey(i)) {
            Image.setImageBitmap(bookImages.get(i));
        }
        return v;
    }

    // Main task to wait until an image is downloaded, then set the given ImageView's display to the downloaded image
    public class WaitForImageTask extends AsyncTask<HashMap<Integer, Bitmap>, Void, Bitmap> {
        private ImageView bmImage;
        private int i;
        public WaitForImageTask(ImageView bmImage, int i) {
            this.bmImage = bmImage;
            this.i = i;
        }
        protected Bitmap doInBackground(HashMap<Integer, Bitmap>... lists) {
            HashMap<Integer, Bitmap> images = lists[0];
            while(!images.containsKey(i) || images.get(i) == null);
            return images.get(i);
        }
        protected void onPostExecute(Bitmap bmp) {
            bmImage.setImageBitmap(bmp);
        }
    }
}
