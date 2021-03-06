package org.wikipedia.readinglist;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Constants;
import org.wikipedia.R;
import org.wikipedia.WikipediaApp;
import org.wikipedia.analytics.ReadingListsFunnel;
import org.wikipedia.auth.AccountUtil;
import org.wikipedia.concurrency.CallbackTask;
import org.wikipedia.events.ArticleSavedOrDeletedEvent;
import org.wikipedia.feed.FeedFragment;
import org.wikipedia.history.SearchActionModeCallback;
import org.wikipedia.main.MainActivity;
import org.wikipedia.main.MainFragment;
import org.wikipedia.onboarding.OnboardingView;
import org.wikipedia.page.ExclusiveBottomSheetPresenter;
import org.wikipedia.readinglist.database.ReadingList;
import org.wikipedia.readinglist.database.ReadingListDbHelper;
import org.wikipedia.readinglist.database.ReadingListPage;
import org.wikipedia.readinglist.sync.ReadingListSyncAdapter;
import org.wikipedia.readinglist.sync.ReadingListSyncEvent;
import org.wikipedia.settings.Prefs;
import org.wikipedia.util.DeviceUtil;
import org.wikipedia.util.FeedbackUtil;
import org.wikipedia.util.StringUtil;
import org.wikipedia.util.log.L;
import org.wikipedia.views.DrawableItemDecoration;
import org.wikipedia.views.SearchEmptyView;
import org.wikipedia.views.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.wikipedia.readinglist.database.ReadingList.SORT_BY_NAME_ASC;
import static org.wikipedia.readinglist.database.ReadingList.SORT_BY_NAME_DESC;
import static org.wikipedia.readinglist.database.ReadingList.SORT_BY_RECENT_ASC;
import static org.wikipedia.readinglist.database.ReadingList.SORT_BY_RECENT_DESC;
import static org.wikipedia.util.ResourceUtil.getThemedAttributeId;

public class ReadingListsFragment extends Fragment implements SortReadingListsDialog.Callback {
    private Unbinder unbinder;
    @BindView(R.id.reading_list_content_container) ViewGroup contentContainer;
    @BindView(R.id.reading_list_list) RecyclerView readingListView;
    @BindView(R.id.empty_container) ViewGroup emptyContainer;
    @BindView(R.id.empty_title) TextView emptyTitle;
    @BindView(R.id.empty_image) ImageView emptyImage;
    @BindView(R.id.empty_message) TextView emptyMessage;
    @BindView(R.id.search_empty_view) SearchEmptyView searchEmptyView;
    @BindView(R.id.reading_list_onboarding_container) ViewGroup onboardingContainer;
    @BindView(R.id.reading_list_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;

    private List<ReadingList> readingLists = new ArrayList<>();

    private ReadingListsFunnel funnel = new ReadingListsFunnel();
    private EventBusMethods eventBusMethods = new EventBusMethods();

    private ReadingListAdapter adapter = new ReadingListAdapter();
    private ReadingListItemCallback listItemCallback = new ReadingListItemCallback();
    private ReadingListsSearchCallback searchActionModeCallback = new ReadingListsSearchCallback();
    private LinearLayoutManager linearLayoutManager;
    @Nullable private ActionMode actionMode;
    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();
    private static final int SAVE_COUNT_LIMIT = 3;


    @NonNull public static ReadingListsFragment newInstance() {
        return new ReadingListsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WikipediaApp.getInstance().getRefWatcher().watch(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading_lists, container, false);
        unbinder = ButterKnife.bind(this, view);
        linearLayoutManager = new LinearLayoutManager(getContext());

        searchEmptyView.setEmptyText(R.string.search_reading_lists_no_results);
        readingListView.setLayoutManager(linearLayoutManager);
        readingListView.setAdapter(adapter);
        readingListView.addItemDecoration(new DrawableItemDecoration(requireContext(), R.attr.list_separator_drawable));

        WikipediaApp.getInstance().getBus().register(eventBusMethods);

        contentContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        emptyContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup)emptyContainer.getChildAt(0)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        swipeRefreshLayout.setColorSchemeResources(getThemedAttributeId(requireContext(), R.attr.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> refreshSync(ReadingListsFragment.this, swipeRefreshLayout));
        if (ReadingListSyncAdapter.isDisabledByRemoteConfig()) {
            swipeRefreshLayout.setEnabled(false);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        WikipediaApp.getInstance().getBus().unregister(eventBusMethods);
        readingListView.setAdapter(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLists();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_reading_lists, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search_lists:
                ((AppCompatActivity) requireActivity())
                        .startSupportActionMode(searchActionModeCallback);
                return true;
            case R.id.menu_sort:
                bottomSheetPresenter.show(getChildFragmentManager(),
                        SortReadingListsDialog.newInstance(Prefs.getReadingListSortMode(SORT_BY_NAME_ASC)));
                return true;
            case R.id.create_list:
                String title = getString(R.string.reading_list_name_sample);
                List<String> existingTitles = new ArrayList<>();
                for (ReadingList tempList : readingLists) {
                    existingTitles.add(tempList.title());
                }
                ReadingListTitleDialog.readingListTitleDialog(requireContext(), title, "",
                        existingTitles, (text, description) -> {
                            ReadingListDbHelper.instance().createList(text, description);
                            updateLists();
                        }).show();
                return true;
            case R.id.refresh:
                swipeRefreshLayout.setRefreshing(true);
                refreshSync(this, swipeRefreshLayout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortListsBy(int option) {
        switch (option) {
            case SORT_BY_NAME_DESC:
                Prefs.setReadingListSortMode(SORT_BY_NAME_DESC);
                break;
            case SORT_BY_RECENT_DESC:
                Prefs.setReadingListSortMode(SORT_BY_RECENT_DESC);
                break;
            case SORT_BY_RECENT_ASC:
                Prefs.setReadingListSortMode(SORT_BY_RECENT_ASC);
                break;
            case SORT_BY_NAME_ASC:
            default:
                Prefs.setReadingListSortMode(SORT_BY_NAME_ASC);
                break;
        }
        updateLists();
    }

    public static void refreshSync(@NonNull Fragment fragment, @NonNull SwipeRefreshLayout swipeRefreshLayout) {
        if (!AccountUtil.isLoggedIn()) {
            ReadingListSyncBehaviorDialogs.promptLogInToSyncDialog(fragment.requireActivity());
            swipeRefreshLayout.setRefreshing(false);
        } else {
            Prefs.setReadingListSyncEnabled(true);
            ReadingListSyncAdapter.manualSyncWithRefresh();
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!isAdded()) {
            return;
        }
        if (visible) {
            updateLists();
            maybeShowOnboarding();
        } else if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void updateLists() {
        updateLists(null);
    }

    private void updateLists(@Nullable final String searchQuery) {
        maybeShowOnboarding();
        CallbackTask.execute(() -> {
            List<ReadingList> lists = ReadingListDbHelper.instance().getAllLists();
            lists = applySearchQuery(searchQuery, lists);
            ReadingList.sort(lists, Prefs.getReadingListSortMode(SORT_BY_NAME_ASC));
            return lists;
        }, new CallbackTask.DefaultCallback<List<ReadingList>>() {
            @Override
            public void success(List<ReadingList> lists) {
                if (getActivity() == null) {
                    return;
                }
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override public int getOldListSize() {
                        return readingLists.size();
                    }
                    @Override public int getNewListSize() {
                        return lists.size();
                    }

                    @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        if (readingLists.size() <= oldItemPosition || lists.size() <= newItemPosition) {
                            return false;
                        }
                        return readingLists.get(oldItemPosition).id() == lists.get(newItemPosition).id();
                    }

                    @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        if (readingLists.size() <= oldItemPosition || lists.size() <= newItemPosition) {
                            return false;
                        }
                        return readingLists.get(oldItemPosition).id() == lists.get(newItemPosition).id()
                                && readingLists.get(oldItemPosition).pages().size() == lists.get(newItemPosition).pages().size()
                                && readingLists.get(oldItemPosition).numPagesOffline() == lists.get(newItemPosition).numPagesOffline();
                    }
                });
                readingLists = lists;
                result.dispatchUpdatesTo(adapter);

                swipeRefreshLayout.setRefreshing(false);
                maybeShowListLimitMessage();
                updateEmptyState(searchQuery);
                maybeDeleteListFromIntent();
            }
        });
    }

    @NonNull
    private List<ReadingList> applySearchQuery(@Nullable String searchQuery, @NonNull List<ReadingList> lists) {
        List<ReadingList> newList = new ArrayList<>();
        if (TextUtils.isEmpty(searchQuery)) {
            newList.addAll(lists);
            return newList;
        }
        String normalizedQuery = StringUtils.stripAccents(searchQuery).toLowerCase();
        for (ReadingList list : lists) {
            if (StringUtils.stripAccents(list.title()).toLowerCase().contains(normalizedQuery)) {
                newList.add(list);
            }
        }
        return newList;
    }

    private void maybeShowListLimitMessage() {
        if (getUserVisibleHint() && readingLists.size() >= Constants.MAX_READING_LISTS_LIMIT) {
            String message = getString(R.string.reading_lists_limit_message);
            FeedbackUtil.makeSnackbar(getActivity(), message, FeedbackUtil.LENGTH_DEFAULT).show();
        }
    }

    private void updateEmptyState(@Nullable String searchQuery) {
        if (TextUtils.isEmpty(searchQuery)) {
            searchEmptyView.setVisibility(View.GONE);
            if (readingLists.size() == 1) {
                setEmptyContainerVisibility(true);
                setUpEmptyContainer();
            }
            setEmptyContainerVisibility(readingLists.size() == 1);
        } else {
            searchEmptyView.setVisibility(readingLists.isEmpty() ? View.VISIBLE : View.GONE);
            setEmptyContainerVisibility(false);
        }
        contentContainer.setVisibility(readingLists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setEmptyContainerVisibility(boolean visible) {
        if (visible) {
            emptyContainer.setVisibility(View.VISIBLE);
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            emptyContainer.setVisibility(View.GONE);
            DeviceUtil.setWindowSoftInputModeResizable(requireActivity());
        }
    }

    private void setUpEmptyContainer() {
        if (!readingLists.get(0).pages().isEmpty()) {
            emptyTitle.setText(getString(R.string.no_user_lists_title));
            emptyMessage.setText(getString(R.string.no_user_lists_msg));
            emptyImage.setImageDrawable(ContextCompat.getDrawable(emptyImage.getContext(), R.drawable.ic_no_user_lists));
        } else {
            emptyTitle.setText(getString(R.string.saved_list_empty_title));
            emptyMessage.setText(getString(R.string.reading_lists_empty_message));
            emptyImage.setImageDrawable(ContextCompat.getDrawable(emptyImage.getContext(), R.drawable.no_lists));
        }
    }

    @Override
    public void onSortOptionClick(int position) {
        sortListsBy(position);
    }

    private class ReadingListItemHolder extends RecyclerView.ViewHolder {
        private ReadingListItemView itemView;

        ReadingListItemHolder(ReadingListItemView itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        void bindItem(ReadingList readingList) {
            itemView.setReadingList(readingList, ReadingListItemView.Description.SUMMARY);
        }

        public ReadingListItemView getView() {
            return itemView;
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View view) {
            super(view);
        }
    }

    private final class ReadingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_ITEM = 0;
        private static final int VIEW_TYPE_FOOTER = 1;

        private boolean isFooterEnabled() {
            boolean isLastItemBeenCovered = false;
            if (linearLayoutManager != null && adapter != null) {
                isLastItemBeenCovered = linearLayoutManager.findLastCompletelyVisibleItemPosition() < readingLists.size();
            }
            return ((MainActivity) requireActivity()).isFloatingQueueEnabled() && isLastItemBeenCovered;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1
                    && isFooterEnabled()) {
                return VIEW_TYPE_FOOTER;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return readingLists.size() + (isFooterEnabled() ? 1 : 0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int pos) {
            if (pos == VIEW_TYPE_FOOTER) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.view_empty_footer, parent, false);
                return new FooterViewHolder(view);
            } else {
                ReadingListItemView view = new ReadingListItemView(getContext());
                return new ReadingListItemHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            if (holder instanceof ReadingListItemHolder) {
                ((ReadingListItemHolder) holder).bindItem(readingLists.get(pos));
            }
        }

        @Override public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (holder instanceof ReadingListItemHolder) {
                ((ReadingListItemHolder) holder).getView().setCallback(listItemCallback);
            }
        }

        @Override public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            if (holder instanceof ReadingListItemHolder) {
                ((ReadingListItemHolder) holder).getView().setCallback(null);
            }
            super.onViewDetachedFromWindow(holder);
        }
    }

    private class ReadingListItemCallback implements ReadingListItemView.Callback {
        @Override
        public void onClick(@NonNull ReadingList readingList) {
            if (actionMode != null) {
                actionMode.finish();
            }
            startActivity(ReadingListActivity.newIntent(requireContext(), readingList));
        }

        @Override
        public void onRename(@NonNull ReadingList readingList) {
            if (readingList.isDefault()) {
                L.w("Attempted to rename default list.");
                return;
            }
            List<String> existingTitles = new ArrayList<>();
            for (ReadingList list : readingLists) {
                existingTitles.add(list.title());
            }
            existingTitles.remove(readingList.title());
            ReadingListTitleDialog.readingListTitleDialog(requireContext(), readingList.title(),
                    readingList.description(), existingTitles, (text, description) -> {
                        readingList.title(text);
                        readingList.description(description);
                        readingList.dirty(true);
                        ReadingListDbHelper.instance().updateList(readingList, true);
                        ReadingListSyncAdapter.manualSync();

                        updateLists();
                        funnel.logModifyList(readingList, readingLists.size());
                    }).show();
        }

        @Override
        public void onDelete(@NonNull ReadingList readingList) {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());
            alert.setMessage(getString(R.string.reading_list_delete_confirm, readingList.title()));
            alert.setPositiveButton(android.R.string.yes, (dialog, id) -> deleteList(readingList));
            alert.setNegativeButton(android.R.string.no, null);
            alert.create().show();
        }

        @Override
        public void onSaveAllOffline(@NonNull ReadingList readingList) {
            if (Prefs.isDownloadOnlyOverWiFiEnabled() && !DeviceUtil.isOnWiFi()) {
                showMobileDataWarningDialog(requireActivity(), (dialog, which)
                        -> saveAllOffline(readingList, true));
            } else {
                saveAllOffline(readingList, false);
            }
        }

        @Override
        public void onRemoveAllOffline(@NonNull ReadingList readingList) {
            ReadingListDbHelper.instance().markPagesForOffline(readingList.pages(), false, false);
            updateLists();
            showMultiSelectOfflineStateChangeSnackbar(readingList.pages(), false);
        }
    }

    public static void showMobileDataWarningDialog(@NonNull Activity activity, @NonNull DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_download_only_over_wifi)
                .setMessage(R.string.dialog_text_download_only_over_wifi)
                .setPositiveButton(R.string.dialog_title_download_only_over_wifi_allow, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void saveAllOffline(@NonNull ReadingList readingList, boolean forcedSave) {
        ReadingListDbHelper.instance().markPagesForOffline(readingList.pages(), true, forcedSave);
        updateLists();
        showMultiSelectOfflineStateChangeSnackbar(readingList.pages(), true);
    }

    private void showMultiSelectOfflineStateChangeSnackbar(List<ReadingListPage> pages, boolean offline) {
        String message = offline
                ? getResources().getQuantityString(R.plurals.reading_list_article_offline_message, pages.size())
                : getResources().getQuantityString(R.plurals.reading_list_article_not_offline_message, pages.size());
        FeedbackUtil.showMessage(getActivity(), message);
    }

    private void maybeDeleteListFromIntent() {
        if (requireActivity().getIntent().hasExtra(Constants.INTENT_EXTRA_DELETE_READING_LIST)) {
            String titleToDelete = requireActivity().getIntent()
                    .getStringExtra(Constants.INTENT_EXTRA_DELETE_READING_LIST);
            requireActivity().getIntent().removeExtra(Constants.INTENT_EXTRA_DELETE_READING_LIST);
            for (ReadingList list : readingLists) {
                if (list.title().equals(titleToDelete)) {
                    deleteList(list);
                }
            }
        }
    }

    private void deleteList(@Nullable ReadingList readingList) {
        if (readingList != null) {
            if (readingList.isDefault()) {
                L.w("Attempted to delete default list.");
                return;
            }
            showDeleteListUndoSnackbar(readingList);

            ReadingListDbHelper.instance().deleteList(readingList);
            ReadingListDbHelper.instance().markPagesForDeletion(readingList, readingList.pages(), false);

            funnel.logDeleteList(readingList, readingLists.size());
            updateLists();
        }
    }

    private void showDeleteListUndoSnackbar(final ReadingList readingList) {
        Snackbar snackbar = FeedbackUtil.makeSnackbar(getActivity(),
                String.format(getString(R.string.reading_list_deleted), readingList.title()),
                FeedbackUtil.LENGTH_DEFAULT);
        snackbar.setAction(R.string.reading_list_item_delete_undo, v -> {
            ReadingList newList = ReadingListDbHelper.instance().createList(readingList.title(), readingList.description());
            List<ReadingListPage> newPages = new ArrayList<>();
            for (ReadingListPage page : readingList.pages()) {
                newPages.add(new ReadingListPage(ReadingListPage.toPageTitle(page)));
            }
            ReadingListDbHelper.instance().addPagesToList(newList, newPages, true);
            updateLists();
        });
        snackbar.show();
    }

    private class ReadingListsSearchCallback extends SearchActionModeCallback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;

            ViewUtil.finishActionModeWhenTappingOnView(getView(), actionMode);
            ViewUtil.finishActionModeWhenTappingOnView(emptyContainer, actionMode);
            ((MainFragment) getParentFragment()).getFloatingQueueView().setVisibility(View.GONE);

            return super.onCreateActionMode(mode, menu);
        }

        @Override
        protected void onQueryChange(String s) {
            String searchString = s.trim();
            ((MainFragment) getParentFragment())
                    .setBottomNavVisible(searchString.length() == 0);
            updateLists(searchString);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            super.onDestroyActionMode(mode);
            actionMode = null;
            ((MainFragment) getParentFragment()).getFloatingQueueView().setVisibility(View.VISIBLE);
            updateLists();
        }

        @Override
        protected String getSearchHintString() {
            return requireContext().getResources().getString(R.string.search_hint_search_my_lists);
        }

        @Override
        protected boolean finishActionModeIfKeyboardHiding() {
            return true;
        }
    }

    private class EventBusMethods {
        @Subscribe public void on(ReadingListSyncEvent event) {
            readingListView.post(() -> {
                if (isAdded()) {
                    updateLists();
                }
            });
        }

        @Subscribe
        public void on(ArticleSavedOrDeletedEvent event) {
            if (event.isAdded()) {
                if (Prefs.getReadingListsPageSaveCount() < SAVE_COUNT_LIMIT) {
                    showReadingListsSyncDialog();
                    Prefs.setReadingListsPageSaveCount(Prefs.getReadingListsPageSaveCount() + 1);
                }
            }
        }
    }

    private void showReadingListsSyncDialog() {
        if (!Prefs.isReadingListSyncEnabled()) {
            if (AccountUtil.isLoggedIn()) {
                ReadingListSyncBehaviorDialogs.promptEnableSyncDialog(requireActivity());
            } else {
                ReadingListSyncBehaviorDialogs.promptLogInToSyncDialog(requireActivity());
            }
        }
    }

    private void maybeShowOnboarding() {
        onboardingContainer.removeAllViews();

        if (AccountUtil.isLoggedIn() && !Prefs.isReadingListSyncEnabled()
                && Prefs.isReadingListSyncReminderEnabled()
                && !ReadingListSyncAdapter.isDisabledByRemoteConfig()) {
            OnboardingView onboardingView = new OnboardingView(requireContext());
            onboardingView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.base20));
            onboardingView.setTitle(R.string.reading_lists_sync_reminder_title);
            onboardingView.setText(StringUtil.fromHtml(getString(R.string.reading_lists_sync_reminder_text)));
            onboardingView.setPositiveAction(R.string.reading_lists_sync_reminder_action);
            onboardingContainer.addView(onboardingView);
            onboardingView.setCallback(new SyncReminderOnboardingCallback());

        } else if (!AccountUtil.isLoggedIn() && Prefs.isReadingListLoginReminderEnabled()
                && !ReadingListSyncAdapter.isDisabledByRemoteConfig()) {
            OnboardingView onboardingView = new OnboardingView(requireContext());
            onboardingView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.base20));
            onboardingView.setTitle(R.string.reading_list_login_reminder_title);
            onboardingView.setText(R.string.reading_lists_login_reminder_text);
            onboardingView.setNegativeAction(R.string.reading_lists_onboarding_got_it);
            onboardingView.setPositiveAction(R.string.menu_login);
            onboardingContainer.addView(onboardingView);
            onboardingView.setCallback(new LoginReminderOnboardingCallback());
        }
    }

    private class SyncReminderOnboardingCallback implements OnboardingView.Callback {
        @Override
        public void onPositiveAction() {
            Prefs.shouldShowReadingListSyncMergePrompt(true);
            ReadingListSyncAdapter.setSyncEnabledWithSetup();
            maybeShowOnboarding();
        }

        @Override
        public void onNegativeAction() {
            Prefs.setReadingListSyncReminderEnabled(false);
            maybeShowOnboarding();
        }
    }

    private class LoginReminderOnboardingCallback implements OnboardingView.Callback {
        @Override
        public void onPositiveAction() {
            if (getParentFragment() instanceof FeedFragment.Callback) {
                ((FeedFragment.Callback) getParentFragment()).onLoginRequested();
            }
        }

        @Override
        public void onNegativeAction() {
            Prefs.setReadingListLoginReminderEnabled(false);
            maybeShowOnboarding();
        }
    }
}
