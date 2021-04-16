package org.wikipedia.readinglist;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.wikipedia.R;
import org.wikipedia.readinglist.database.ReadingList;
import org.wikipedia.readinglist.database.ReadingListPage;
import org.wikipedia.util.DimenUtil;
import org.wikipedia.util.ResourceUtil;
import org.wikipedia.views.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadingListItemView extends ConstraintLayout {
    public interface Callback {
        void onClick(@NonNull ReadingList readingList);
        void onRename(@NonNull ReadingList readingList);
        void onDelete(@NonNull ReadingList readingList);
        void onSaveAllOffline(@NonNull ReadingList readingList);
        void onRemoveAllOffline(@NonNull ReadingList readingList);
    }

    public enum Description { DETAIL, SUMMARY }

    @BindView(R.id.item_title) TextView titleView;
    @BindView(R.id.item_reading_list_statistical_description) TextView statisticalDescriptionView;
    @BindView(R.id.item_description) TextView descriptionView;
    @BindView(R.id.item_overflow_menu)View overflowButton;

    @Nullable private Callback callback;
    @Nullable private ReadingList readingList;

    public ReadingListItemView(Context context) {
        super(context);
        init();
    }

    public ReadingListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReadingListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setReadingList(@NonNull ReadingList readingList, @NonNull Description description) {
        this.readingList = readingList;

        boolean isDetailView = description == Description.DETAIL;
        descriptionView.setMaxLines(isDetailView
                ? Integer.MAX_VALUE
                : getResources().getInteger(R.integer.reading_list_description_summary_view_max_lines));
        CharSequence text = isDetailView
                ? buildStatisticalDetailText(readingList)
                : buildStatisticalSummaryText(readingList);
        statisticalDescriptionView.setText(text);

        updateDetails();
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    public void setOverflowButtonVisible(boolean visible) {
        overflowButton.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setTitleTextAppearance(@StyleRes int id) {
        TextViewCompat.setTextAppearance(titleView, id);
    }

    @OnClick void onClick(View view) {
        if (callback != null && readingList != null) {
            callback.onClick(readingList);
        }
    }

    @OnClick(R.id.item_overflow_menu) void showOverflowMenu(View anchorView) {
        PopupMenu menu = new PopupMenu(getContext(), anchorView, Gravity.END);
        menu.getMenuInflater().inflate(R.menu.menu_reading_list_item, menu.getMenu());

        if (readingList.isDefault()) {
            menu.getMenu().findItem(R.id.menu_reading_list_rename).setVisible(false);
            menu.getMenu().findItem(R.id.menu_reading_list_delete).setVisible(false);
        }
        menu.setOnMenuItemClickListener(new OverflowMenuClickListener(readingList));
        menu.show();
    }

    private void init() {
        inflate(getContext(), R.layout.item_reading_list, this);
        ButterKnife.bind(this);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final int topBottomPadding = 16;
        setPadding(0, DimenUtil.roundedDpToPx(topBottomPadding), 0, DimenUtil.roundedDpToPx(topBottomPadding));
        setBackgroundColor(ResourceUtil.getThemedColor(getContext(), R.attr.paper_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setForeground(ContextCompat.getDrawable(getContext(), ResourceUtil.getThemedAttributeId(getContext(), R.attr.selectableItemBackground)));
        }
        setClickable(true);
    }

    private void updateDetails() {
        if (readingList == null) {
            return;
        }

        titleView.setText(readingList.title());
        if (readingList.isDefault()) {
            descriptionView.setText(getContext().getString(R.string.default_reading_list_description));
            descriptionView.setVisibility(VISIBLE);
        } else {
            descriptionView.setText(readingList.description());
            descriptionView.setVisibility(TextUtils.isEmpty(readingList.description()) ? GONE : VISIBLE);
        }
    }



    @NonNull private String buildStatisticalSummaryText(@NonNull ReadingList readingList) {
        float listSize = statsTextListSize(readingList);
        return readingList.pages().size() == 1
                ? getString(R.string.format_reading_list_statistical_summary_singular,
                    listSize)
                : getString(R.string.format_reading_list_statistical_summary_plural,
                    readingList.pages().size(), listSize);
    }

    @NonNull private String buildStatisticalDetailText(@NonNull ReadingList readingList) {
        float listSize = statsTextListSize(readingList);
        return readingList.pages().size() == 1
                ? getString(R.string.format_reading_list_statistical_detail_singular,
                    readingList.numPagesOffline(), listSize)
                : getString(R.string.format_reading_list_statistical_detail_plural,
                    readingList.numPagesOffline(), readingList.pages().size(), listSize);
    }

    private float statsTextListSize(@NonNull ReadingList readingList) {
        int unitSize = Math.max(1, getResources().getInteger(R.integer.reading_list_item_size_bytes_per_unit));
        return readingList.sizeBytes() / (float) unitSize;
    }

    @NonNull private String getString(@StringRes int id, @Nullable Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    private class OverflowMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        @Nullable private ReadingList list;

        OverflowMenuClickListener(@Nullable ReadingList list) {
            this.list = list;
        }

        @Override public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_reading_list_rename:
                    if (callback != null && list != null) {
                        callback.onRename(list);
                        return true;
                    }
                    break;
                case R.id.menu_reading_list_delete:
                    if (callback != null && list != null) {
                        callback.onDelete(list);
                        return true;
                    }
                    break;
                case R.id.menu_reading_list_save_all_offline:
                    if (callback != null && list != null) {
                        callback.onSaveAllOffline(list);
                        return true;
                    }
                    break;
                case R.id.menu_reading_list_remove_all_offline:
                    if (callback != null && list != null) {
                        callback.onRemoveAllOffline(list);
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
