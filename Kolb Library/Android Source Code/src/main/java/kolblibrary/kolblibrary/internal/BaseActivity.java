/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import kolblibrary.kolblibrary.useractivities.BugActivity;
import kolblibrary.kolblibrary.adminactivities.AllCheckoutsActivity;
import kolblibrary.kolblibrary.adminactivities.AllUsersActivity;
import kolblibrary.kolblibrary.adminactivities.CheckoutRequestActivity;
import kolblibrary.kolblibrary.adminactivities.ReserveRequestActivity;
import kolblibrary.kolblibrary.useractivities.MyCheckoutsActivity;
import kolblibrary.kolblibrary.useractivities.MyReservedActivity;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.useractivities.ShareActivity;
import kolblibrary.kolblibrary.useractivities.UserHomeScreenActivity;
import kolblibrary.kolblibrary.useractivities.CatalogSearchActivity;
import kolblibrary.kolblibrary.useractivities.LibraryMapActivity;

/**
 * This is an abstract class that extends AppCompatActivity.
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */


public abstract class BaseActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {
    private FrameLayout view_stub; //This is the framelayout to keep your content view
    private NavigationView navigation_view; // The new navigation view from Android Design Library. Can inflate menu resources. Easy
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Menu drawerMenu;
    protected ProgressBar progressBar;

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_home);// The base layout that contains your navigation drawer.
        currentActivity = BaseActivity.this;
        view_stub = (FrameLayout) findViewById(R.id.app_bar_home);
        navigation_view = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerMenu = navigation_view.getMenu();
        for(int i = 0; i < drawerMenu.size(); i++) {
            drawerMenu.getItem(i).setOnMenuItemClickListener(this);
        }
        if(SelfUserInfo.userType != 2){
            navigation_view.getMenu().findItem(R.id.nav_edit_users).setVisible(false);
            navigation_view.getMenu().findItem(R.id.nav_view_checkout_requests).setVisible(false);
            navigation_view.getMenu().findItem(R.id.nav_view_reserve_requests).setVisible(false);
            navigation_view.getMenu().findItem(R.id.nav_view_all_checkouts).setVisible(false);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Override all setContentView methods to put the content view to the FrameLayout view_stub
     * so that, we can make other activity implementations looks like normal activity subclasses.
     */
    @Override
    public void setContentView(int layoutResID) {
        if (view_stub != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            View stubView = inflater.inflate(layoutResID, view_stub, false);
            view_stub.addView(stubView, lp);
        }
    }

    @Override
    public void setContentView(View view) {
        if (view_stub != null) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            view_stub.addView(view, lp);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (view_stub != null) {
            view_stub.addView(view, params);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    Activity currentActivity = null;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Intent h= new Intent(this,UserHomeScreenActivity.class);
                //Networking.update(currentActivity, h);
                startActivity(h);
                break;
            case R.id.nav_map:
                Intent i= new Intent(this,LibraryMapActivity.class);
                //Networking.update(currentActivity, i);
                startActivity(i);
                break;
            case R.id.nav_reserved:
                Intent g= new Intent(this,MyReservedActivity.class);
                Networking.update(currentActivity, g);
                //startActivity(h);
                break;
            case R.id.nav_checkedout:
                Intent s= new Intent(this,MyCheckoutsActivity.class);
                Networking.update(currentActivity, s);
                //startActivity(h);
                break;
            case R.id.nav_search:
                Intent t= new Intent(this,CatalogSearchActivity.class);
                //Networking.update(currentActivity, t);
                startActivity(t);
                break;
            case R.id.nav_edit_users:
                Intent u= new Intent(this,AllUsersActivity.class);
                Networking.update(currentActivity, u);
                //startActivity(h);
                break;
            case R.id.nav_view_checkout_requests:
                Intent v= new Intent(this,CheckoutRequestActivity.class);
                Networking.update(currentActivity, v);
                //startActivity(h);
                break;
            case R.id.nav_view_reserve_requests:
                Intent w= new Intent(this,ReserveRequestActivity.class);
                Networking.update(currentActivity, w);
                //startActivity(h);
                break;
            case R.id.nav_view_all_checkouts:
                Intent ww= new Intent(this,AllCheckoutsActivity.class);
                Networking.update(currentActivity, ww);
                //startActivity(h);
                break;
            case R.id.nav_report_bug:
                Intent x= new Intent(this,BugActivity.class);
                //Networking.update(currentActivity, x);
                startActivity(x);
                break;
            case R.id.nav_share:
                Intent j = new Intent(this, ShareActivity.class);
                //Networking.update(currentActivity, d);
                startActivity(j);
                break;
            case R.id.nav_logout:
                Networking.logout(view_stub.getContext());
                break;
        }
        return false;
    }
}