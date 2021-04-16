package com.haringeymobile.ukweather;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.haringeymobile.ukweather.database.CityTable;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

/**
 * An adapter to map the cities stored in the database to the city list.
 */
public abstract class BaseCityCursorAdapter extends SimpleDragSortCursorAdapter {

    /**
     * The resource ID for the view corresponding to an even cursor position.
     */
    static final int BACKGROUND_RESOURCE_EVEN = R.drawable.clickable_dark;
    /**
     * The resource ID for the view corresponding to an odd cursor position. It depends on the app
     * theme, and is resolved at runtime.
     */
    static int BACKGROUND_RESOURCE_ODD;
    /**
     * Columns in the database that will be displayed in a list row.
     */
    protected static final String[] COLUMNS_TO_DISPLAY = new String[]{CityTable.COLUMN_NAME};
    /**
     * Resource IDs of views that will display the data mapped from the
     * database.
     */
    protected static final int[] TO = new int[]{R.id.city_name_in_list_row_text_view};
    /**
     * Loader ID.
     */
    private static final int LOADER_ALL_CITY_RECORDS = 0;

    protected Activity parentActivity;
    /**
     * A listener for button clicks.
     */
    OnClickListener onClickListener;

    BaseCityCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags,
                          OnClickListener onClickListener) {
        super(context, layout, c, from, to, flags);
        this.onClickListener = onClickListener;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.themed_clickable, outValue, true);
        BACKGROUND_RESOURCE_ODD = outValue.resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position % 2 == 1) {
            view.setBackgroundResource(BACKGROUND_RESOURCE_ODD);
        } else {
            view.setBackgroundResource(BACKGROUND_RESOURCE_EVEN);
        }
        return view;
    }

    /**
     * Obtains the Open Weather Map city ID for the specified list position.
     *
     * @param position the city list position
     * @return Open Weather Map city ID, or -1 if the city list does not contain the
     * specified position
     */
    int getCityId(int position) {
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            return cursor.getInt(cursor.getColumnIndex(CityTable.COLUMN_CITY_ID));
        }
        return CityTable.CITY_ID_DOES_NOT_EXIST;
    }

    /**
     * Obtains the city name stored in the database for the specified list position.
     *
     * @param position city list position
     * @return city name, or null if city list does not contain the specified position
     */
    String getCityName(int position) {
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            return cursor.getString(cursor.getColumnIndex(CityTable.COLUMN_NAME));
        }
        return null;
    }

}