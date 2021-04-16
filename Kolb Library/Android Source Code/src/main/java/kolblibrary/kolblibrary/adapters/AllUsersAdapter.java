/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/

package kolblibrary.kolblibrary.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.hsqldb.rights.User;
/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;



public class AllUsersAdapter extends BaseAdapter{

    LayoutInflater mInflater;
    ArrayList<UserInfo> userlist;
    public static AllUsersAdapter instance;
    Context current;


    public AllUsersAdapter(Context c, ArrayList<UserInfo> b) {
        current = c;
        instance = this;
        userlist = b;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        if(userlist == null || userlist.size() == 0){
            return 0;
        }
        else{
            return userlist.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return userlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     *
     * @param position
     * @param v
     * @param viewGroup
     * @return The View associated with the given index from this Adapter fitting a ListView.
     */
    @Override
    public View getView(int position, View v, ViewGroup viewGroup) {
        if(v == null) {
            v = LayoutInflater.from(current).inflate(R.layout.catalog_all_users_detail, null, false);
        }
            TextView name = (TextView) v.findViewById(R.id.AllUsersNameView);
            String namey = userlist.get(position).firstName + " " + userlist.get(position).lastName;
            if(namey != null && !namey.isEmpty()){
                name.setText(namey);
            }
	    // Find necessary views to display
            TextView type = (TextView) v.findViewById(R.id.AllUsersRoleView);
            int usertype = userlist.get(position).userType;

	    // Populate views with information
            String set = "";
            if(usertype == 0){
                set = "Student";
            }
            else if(usertype == 1){
                set = "Teacher";
            }
            else if(usertype == 2){
                set = "Administrator";
            }
            type.setText("Type of user: " + set);



        return v;
    }
}
