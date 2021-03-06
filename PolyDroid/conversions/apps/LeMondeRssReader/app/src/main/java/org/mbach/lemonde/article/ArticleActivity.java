package org.mbach.lemonde.article;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.Chart;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mbach.lemonde.Constants;
import org.mbach.lemonde.LeMondeDB;
import org.mbach.lemonde.R;
import org.mbach.lemonde.ThemeUtils;
import org.mbach.lemonde.account.LoginActivity;
import org.mbach.lemonde.home.MainActivity;
import org.mbach.lemonde.home.RssItem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ArticleActivity class fetch an article from an URI, and parse the HTML response.
 *
 * @author Matthieu BACHELIER
 * @since 2017-05
 */
public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = "ArticleActivity";
    private static final String STATE_RECYCLER_VIEW_POS = "STATE_RECYCLER_VIEW_POS";
    private static final String STATE_RECYCLER_VIEW = "STATE_RECYCLER_VIEW";
    private static final String STATE_ADAPTER_ITEM = "STATE_ADAPTER_ITEM";
    private static final String ATTR_HEADLINE = "Headline";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_AUTHOR = "author";
    private static final String TAG_TRUE = "vrai";
    private static final String TAG_FAKE = "faux";
    private static final String TAG_MOSTLY_TRUE = "plutot_vrai";
    private static final String TAG_FORGOTTEN = "oubli";

    @Nullable
    static RequestQueue REQUEST_QUEUE = null;

    private final ArticleAdapter articleAdapter = new ArticleAdapter();
    private final AlphaAnimation animationFadeIn = new AlphaAnimation(0, 1);
    private RecyclerView recyclerView;
    private ProgressBar autoLoader;
    private MenuItem shareItem;
    private MenuItem toggleFavItem;
    @Nullable
    private String shareLink;
    private String commentsURI;
    private String shareSubject;
    private String shareText;
    private boolean isRestricted = false;
    private int articleId = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            return super.dispatchTouchEvent(motionEvent);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(getBaseContext(), getTheme());
        setContentView(R.layout.activity_article);
        setTitle("");

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.articleActivityRecyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(articleAdapter);
        Toolbar toolbar = findViewById(R.id.toolbarArticle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.articleAppBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(@NonNull AppBarLayout appBarLayout, int verticalOffset) {
                if (shareItem == null) {
                    return;
                }
                // XXX: convert to independent unit
                //Log.d(TAG, "verticalOffset = " + verticalOffset);
                View share = findViewById(R.id.action_share);

                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                    shareItem.setVisible(true);
                    if (share != null) {
                        share.startAnimation(animationFadeIn);
                    }
                } else {
                    shareItem.setVisible(false);
                }
            }
        });

        if (REQUEST_QUEUE == null) {
            REQUEST_QUEUE = Volley.newRequestQueue(this);
        }

        autoLoader = findViewById(R.id.autoLoader);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.getLayoutManager() != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getLayoutManager().getItemCount() - 1) {
                    if (Constants.BASE_URL2.equals(commentsURI)) {
                        autoLoader.setVisibility(View.INVISIBLE);
                        Snackbar.make(findViewById(R.id.coordinatorArticle), getString(R.string.no_more_comments_to_load), Snackbar.LENGTH_LONG).show();
                    } else if (commentsURI != null) {
                        autoLoader.setVisibility(View.VISIBLE);
                        REQUEST_QUEUE.add(new StringRequest(Request.Method.GET, commentsURI, commentsReceived, errorResponse));
                    }
                } else {
                    autoLoader.setVisibility(View.INVISIBLE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        // If user is opening a link from another App, like a mail client for instance
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            shareLink = intent.getDataString();
        } else if (intent.getExtras() != null) {
            collapsingToolbar.setTitle(intent.getExtras().getString(Constants.EXTRA_NEWS_CATEGORY));
            Picasso.with(this)
                    // V5(100) V6(50) COMMENT IN
                    .load(intent.getExtras().getString(Constants.EXTRA_RSS_IMAGE)).resize(100, 100)
                    // V5 V6 COMMENT OUT
//                    .load(intent.getExtras().getString(Constants.EXTRA_RSS_IMAGE))
                    // V2 COMMENT OUT
                    .into((ImageView) findViewById(R.id.imageArticle));

            shareLink = intent.getExtras().getString(Constants.EXTRA_RSS_LINK);
            int articleId = intent.getExtras().getInt(Constants.EXTRA_RSS_ARTICLE_ID);
            if (articleId > 0) {
                this.articleId = articleId;
            }
        }

        // Start async job
        int lastFirstVisiblePosition = getIntent().getIntExtra(STATE_RECYCLER_VIEW_POS, -1);
        // If we have stored the position, it means we also have stored state and items from the recycler view
        if (lastFirstVisiblePosition >= 0) {
            Parcelable parcelable = getIntent().getParcelableExtra(STATE_RECYCLER_VIEW);
            if (recyclerView.getLayoutManager() != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
            }
            ArrayList<Model> items = getIntent().getParcelableArrayListExtra(STATE_ADAPTER_ITEM);
            articleAdapter.addItems(items);
            recyclerView.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
        } else {
            REQUEST_QUEUE.add(new StringRequest(Request.Method.GET, shareLink, articleReceived, errorResponse));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Do not display the loader once again after resuming this activity
        if (articleAdapter.getItemCount() > 0) {
            findViewById(R.id.articleLoader).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save position, state, and items before rotating the device
        if (recyclerView.getLayoutManager() != null) {
            int lastFirstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            Parcelable parcelable = recyclerView.getLayoutManager().onSaveInstanceState();
            getIntent().putExtra(STATE_RECYCLER_VIEW, parcelable);
            getIntent().putExtra(STATE_RECYCLER_VIEW_POS, lastFirstVisiblePosition);
            getIntent().putParcelableArrayListExtra(STATE_ADAPTER_ITEM, articleAdapter.getItems());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else {
                onBackPressed();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shareContent = sharedPreferences.getBoolean("shareOnSocialNetworks", true);
        if (shareContent) {
            getMenuInflater().inflate(R.menu.articleactivity_right_menu, menu);
            shareItem = menu.findItem(R.id.action_share);
            /// FIXME
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animationFadeIn.setDuration(1000);
                    shareItem.setVisible(true);
                }
            }, 1);
            shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText + " " + shareLink);
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_article)));
                    return false;
                }
            });
            toggleFavItem = menu.findItem(R.id.action_toggle_fav);
            toggleFavItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    final LeMondeDB leMondeDB = new LeMondeDB(ArticleActivity.this);
                    boolean hasArticle = leMondeDB.hasArticle(articleId);
                    if (hasArticle && leMondeDB.deleteArticle(articleId)) {
                        hasArticle = false;
                        Snackbar.make(findViewById(R.id.coordinatorArticle), getString(R.string.favorites_article_removed), Snackbar.LENGTH_SHORT).show();
                    } else {
                        RssItem favorite = new RssItem(RssItem.ARTICLE_TYPE);
                        favorite.setArticleId(articleId);
                        if (getIntent().getExtras() != null) {
                            favorite.setTitle(getIntent().getExtras().getString(Constants.EXTRA_RSS_TITLE));
                            favorite.setPubDate(getIntent().getExtras().getLong(Constants.EXTRA_RSS_DATE));
                            favorite.setEnclosure(getIntent().getExtras().getString(Constants.EXTRA_RSS_IMAGE));
                        }
                        favorite.setLink(shareLink);
                        favorite.setCategory(getTitle().toString());
                        if (leMondeDB.saveArticle(favorite)) {
                            hasArticle = true;
                            Snackbar.make(findViewById(R.id.coordinatorArticle), getString(R.string.favorites_article_added), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    toggleFavIcon(hasArticle);
                    return false;
                }
            });
        }
        return true;
    }

    public void openTweet(@NonNull View view) {
        Button button = view.findViewById(R.id.tweet_button);
        String link = button.getContentDescription().toString();
        Uri uri;
        if (link.isEmpty()) {
            uri = Uri.parse("https://www.twitter.com");
        } else {
            uri = Uri.parse(link);
        }
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public void goToAccount(@SuppressWarnings("unused") View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void register(@SuppressWarnings("unused") View view) {
        Uri uri = Uri.parse("https://abo.lemonde.fr");
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    /**
     * This listener is a callback which can parse and extract the HTML page that has been received after
     * an asynchronous call to the web. Jsoup library is used to parse the response and not to make the call.
     * Otherwise, a NetworkOnMainThreadException will be fired by the system.
     */
    @Nullable
    private final Response.Listener<String> articleReceived = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            // Hide icon
            findViewById(R.id.noNetwork).setVisibility(View.INVISIBLE);

            Document doc = Jsoup.parse(response);

            // If article was loaded from an external App, no image was passed from MainActivity,
            // so it must be fetched in the Collapsing Toolbar
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                Elements image = doc.select("meta[property=og:image]");
                if (atLeastOneChild(image)) {
                    Picasso.with(ArticleActivity.this)
                            .load(image.first().attr("content"));
                    //V2 COMMENT OUT
//                            .into((ImageView) findViewById(R.id.imageArticle));
                }
            }

            // Article is from a hosted blog
            ArrayList<Model> items;
            Element content = doc.getElementById("content");
            if (content != null) {
                items = extractBlogArticle(content);
                setTagInHeader(R.string.blog_article, R.color.accent_complementary, Color.WHITE);
            } else {
                Elements category = doc.select("div.tt_rubrique_ombrelle");
                if (atLeastOneChild(category)) {
                    setTitle(category.text());
                }
                Elements articles = doc.getElementsByTag("article");
                Element largeFormat = doc.getElementById("hors_format");
                if (largeFormat != null) {
                    items = new ArrayList<>();
                    setTagInHeader(R.string.large_article, R.color.primary_dark, Color.WHITE);
                } else if (articles.isEmpty()) {
                    Elements liveContainer = doc.getElementsByClass("live2-container");
                    if (liveContainer.isEmpty()) {
                        // Video
                        items = extractVideo(doc);
                        setTagInHeader(R.string.video_article, R.color.accent_complementary, Color.WHITE);
                    } else {
                        LiveFeedParser liveFeedParser = new LiveFeedParser(getApplicationContext(), articleAdapter, doc);
                        items = liveFeedParser.extractLiveFacts();
                        liveFeedParser.fetchPosts(liveContainer.select("script").html());
                        setTagInHeader(R.string.live_article, R.color.accent_live, Color.WHITE);
                    }
                } else {
                    // Standard article
                    items = extractStandardArticle(articles);
                    LeMondeDB leMondeDB = new LeMondeDB(ArticleActivity.this);
                    // Full article is restricted to paid members
                    isRestricted = doc.getElementById("teaser_article") != null;
                    Log.d(TAG, "articleId " + articleId);
                    boolean hasArticle = leMondeDB.hasArticle(articleId);
                    toggleFavIcon(hasArticle);
                    if (isRestricted) {
                        if (shareItem != null) {
                            shareItem.setIcon(getResources().getDrawable(R.drawable.ic_share_black));
                        }
                        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
                        collapsingToolbar.setContentScrimResource(R.color.accent);
                        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.primary_dark));
                        setTagInHeader(R.string.paid_article, R.color.accent, Color.BLACK);

                        if (getSupportActionBar() != null) {
                            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
                            getSupportActionBar().setHomeAsUpIndicator(upArrow);
                        }

                        // Add a button before comments where the user can connect
                        CardView connectButton = new CardView(ArticleActivity.this);
                        items.add(new Model(Model.BUTTON_TYPE, connectButton));
                    }
                    // After parsing the article, start a new request for comments
                    Element react = doc.getElementById("liste_reactions");
                    if (react != null) {
                        Elements dataAjURI = react.select("[^data-aj-uri]");
                        if (atLeastOneChild(dataAjURI)) {
                            String commentPreviewURI = Constants.BASE_URL2 + dataAjURI.first().attr("data-aj-uri");
                            REQUEST_QUEUE.add(new StringRequest(Request.Method.GET, commentPreviewURI, commentsReceived, errorResponse));
                        }
                    }
                }
            }
            articleAdapter.addItems(items);
            findViewById(R.id.articleLoader).setVisibility(View.GONE);
        }
    };

    private void toggleFavIcon(boolean hasArticle) {
        if (isRestricted && toggleFavItem != null) {
            if (hasArticle) {
                toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_full_black));
            } else {
                toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_border_black));
            }
        } else if (toggleFavItem != null) {
            if (hasArticle) {
                if (ThemeUtils.isDarkTheme(ArticleActivity.this)) {
                    toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_full_light));
                } else {
                    toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_full_black));
                }
            } else {
                if (ThemeUtils.isDarkTheme(ArticleActivity.this)) {
                    toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_border_light));
                } else {
                    toggleFavItem.setIcon(getResources().getDrawable(R.drawable.star_border_black));
                }
            }
        } else {
            Log.d(TAG, "toggleFavItem is null :(");
        }
    }

    /**
     * This helper method is used to customize the header (in the AppBar) to display a tag or a bubble when the current
     * article comes from an hosted blog, or is restricted to paid members.
     *
     * @param stringId        string to display in the AppBar
     * @param backgroundColor color of the background
     * @param textColor       color of the text to display
     */
    private void setTagInHeader(int stringId, int backgroundColor, int textColor) {
        TextView tagArticle = findViewById(R.id.tagArticle);
        tagArticle.setText(getString(stringId));
        tagArticle.setBackgroundColor(ContextCompat.getColor(getBaseContext(), backgroundColor));
        tagArticle.setTextColor(textColor);
        tagArticle.setVisibility(View.VISIBLE);
    }

    /**
     * See @articleReceived field.
     */
    private final Response.Listener<String> commentsReceived = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Document commentDoc = Jsoup.parse(response);

            ArrayList<Model> items = new ArrayList<>();
            // Extract header
            Elements header = commentDoc.select("[itemprop='InteractionCount']");
            if (atLeastOneChild(header)) {
                TextView commentHeader = new TextView(ArticleActivity.this);
                commentHeader.setText(String.format("Commentaires %s", header.text()));
                commentHeader.setTypeface(null, Typeface.BOLD);
                commentHeader.setPadding(0, 0, 0, Constants.PADDING_COMMENT_ANSWER);
                items.add(new Model(commentHeader, 0));
            }

            // Extract comments
            Elements comments = commentDoc.select("[itemprop='commentText']");
            for (Element comment : comments) {
                Elements refs = comment.select("p.references");
                if (atLeastOneChild(refs)) {
                    // Clear date
                    refs.select("span").remove();
                    TextView author = new TextView(ArticleActivity.this);
                    author.setTypeface(null, Typeface.BOLD);
                    author.setText(refs.text());

                    Elements commentComment = refs.next();
                    if (atLeastOneChild(commentComment)) {
                        TextView content = new TextView(ArticleActivity.this);
                        content.setText(commentComment.first().text());
                        if (comment.hasClass("reponse")) {
                            author.setPadding(Constants.PADDING_COMMENT_ANSWER, 0, 0, 12);
                            content.setPadding(Constants.PADDING_COMMENT_ANSWER, 0, 0, 16);
                        } else {
                            author.setPadding(0, 0, 0, 12);
                            content.setPadding(0, 0, 0, 16);
                        }
                        int commentId = Integer.valueOf(comment.attr("data-reaction_id"));
                        items.add(new Model(author, commentId));
                        items.add(new Model(content, commentId));
                    }
                }
            }
            // Extract full comments page URI
            Elements div = commentDoc.select("div.reactions");

            if (atLeastOneChild(div)) {
                Element fullComments = div.first().nextElementSibling();
                Elements next = fullComments.select("a");
                if (atLeastOneChild(next)) {
                    commentsURI = Constants.BASE_URL2 + next.first().attr("href");
                }
            }
            articleAdapter.addItems(items);
        }
    };

    /**
     * See @articleReceived field.
     */
    @Nullable
    private final Response.ErrorListener errorResponse = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            findViewById(R.id.articleLoader).setVisibility(View.GONE);

            ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
                    // Display icon
                    findViewById(R.id.noNetwork).setVisibility(View.VISIBLE);
                    // Display permanent message
                    Snackbar.make(findViewById(R.id.coordinatorArticle), getString(R.string.error_no_connection), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.error_no_connection_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    REQUEST_QUEUE.add(new StringRequest(Request.Method.GET, shareLink, articleReceived, errorResponse));
                                }
                            }).show();
                }
            }
        }
    };

    /**
     * Check if elements has at least one child.
     * This helper is useful because Elements.select() returns a collection of nodes.
     *
     * @param elements nodes to check
     * @return true if elements can be safely called with first()
     */
    public static boolean atLeastOneChild(@Nullable Elements elements) {
        return elements != null && !elements.isEmpty();
    }

    /**
     * Extract and parse a standard article. A standard article is published on the main page and by definition,
     * is not: from a hosted blog, nor a video, nor a special multimedia content. It has some standardized fields like
     * one (or multiple) author(s), a date, a description, an headline, a description and a list of paragraphs.
     * Each paragraph is a block of text (comments included) or an image or an embedded tweet.
     *
     * @param articles article to analyze
     * @return a list of formatted content that can be nicely displayed in the recycler view.
     */
    @NonNull
    private ArrayList<Model> extractStandardArticle(@NonNull Elements articles) {
        TextView headLine = new TextView(this);
        TextView authors = new TextView(this);
        TextView dates = new TextView(this);
        TextView description = new TextView(this);

        headLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_headline));
        authors.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_authors));
        dates.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_authors));
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_description));

        Element article = articles.first();
        authors.setText(extractAttr(article, ATTR_AUTHOR));
        dates.setText(extractDates(article));
        shareSubject = extractAttr(article, ATTR_HEADLINE);
        shareText = extractAttr(article, ATTR_DESCRIPTION);
        description.setText(shareText);
        headLine.setText(shareSubject);

        ArrayList<Model> views = new ArrayList<>();
        views.add(new Model(headLine));
        views.add(new Model(authors));
        views.add(new Model(dates));
        views.add(new Model(description));
        views.addAll(extractParagraphs(article));
        return views;
    }

    /**
     * This method is not functionnal at this moment.
     *
     * @param doc the document to analyze
     * @return a list of formatted content that can be nicely displayed in the recycler view.
     */
    @NonNull
    private ArrayList<Model> extractVideo(@NonNull Document doc) {
        Elements elements = doc.select("section.video");
        if (elements.isEmpty()) {
            return new ArrayList<>();
        }

        TextView headLine = new TextView(this);
        TextView authors = new TextView(this);
        TextView dates = new TextView(this);
        TextView content = new TextView(this);

        headLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_headline));
        authors.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_authors));
        dates.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_authors));
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_body));

        Element video = elements.first();
        headLine.setText(extractAttr(video, ATTR_HEADLINE));
        authors.setText(extractAttr(video, ATTR_AUTHOR));
        dates.setText(extractDates(video));
        Elements els = video.select("div.grid_12.alpha");
        if (atLeastOneChild(els)) {
            els.select("h2").remove();
            els.select("span.txt_gris_soutenu").remove();
            els.select("#recos_videos_outbrain").remove();
            fromHtml(content, els.html());
        }
        ArrayList<Model> views = new ArrayList<>();
        views.add(new Model(headLine));
        views.add(new Model(authors));
        views.add(new Model(dates));
        views.add(new Model(content));
        Elements scripts = doc.getElementsByTag("script");
        for (Element script : scripts) {
            if (script.html().contains("lmd/module/video/player")) {
                Pattern p = Pattern.compile("url: '//(.+)',", Pattern.DOTALL);
                Matcher m = p.matcher(script.html());
                if (m.find()) {
                    String iFrame = m.group(1);
                    REQUEST_QUEUE.add(new StringRequest(Request.Method.GET, "https://" + iFrame, videoFrameReceived, errorResponse));
                }
            }
        }
        return views;
    }

    /**
     * Extract Video URI.
     */
    private final Response.Listener<String> videoFrameReceived = new Response.Listener<String>() {
        @Override
        public void onResponse(@NonNull String response) {
            Pattern p = Pattern.compile("\"mp4_720\":\"(https:.*\\.mp4)\\?mdtk=.*\",\"mp4_480\":.*", Pattern.DOTALL);
            Matcher m = p.matcher(response);
            if (m.find()) {
                String videoURI = m.group(1);
                videoURI = videoURI.replace("\\", "");
                Model model = new Model(Model.VIDEO_TYPE, Uri.parse(videoURI));
                articleAdapter.addItem(model);
                Log.d(TAG, "videoURI = " + videoURI);
            } else {
                Log.d(TAG, "not found >> " + response);
            }
        }
    };

    /**
     * Static fromHtml to deal with older SDK.
     *
     * @param textView the textView to fill
     * @param html     raw string
     */
    static void fromHtml(@NonNull TextView textView, String html) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html));
        } else {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    /**
     * See {@link ArticleActivity extractStandardArticle}.
     *
     * @param content the document to analyze
     * @return a list of formatted content that can be nicely displayed in the recycler view.
     */
    @NonNull
    private ArrayList<Model> extractBlogArticle(@NonNull Element content) {
        TextView headLine = new TextView(this);
        TextView dates = new TextView(this);

        headLine.setTextSize(getResources().getDimension(R.dimen.article_headline));
        dates.setTextSize(getResources().getDimension(R.dimen.article_authors));

        Elements elements = content.select(".entry-title");
        if (atLeastOneChild(elements)) {
            headLine.setText(elements.first().text());
        }

        elements = content.select(".entry-date");
        if (atLeastOneChild(elements)) {
            dates.setText(elements.first().text());
        }
        ArrayList<Model> views = new ArrayList<>();
        views.add(new Model(headLine));
        views.add(new Model(dates));

        elements = content.select(".entry-content");
        if (atLeastOneChild(elements)) {
            for (Element child : elements.first().children()) {
                TextView textView = new TextView(this);
                textView.setText(child.text());
                textView.setPadding(0, 0, 0, Constants.PADDING_BOTTOM);
                textView.setTextSize(getResources().getDimension(R.dimen.article_body));
                views.add(new Model(textView));
            }
        }
        return views;
    }

    @NonNull
    private String extractAttr(@NonNull Element article, String attribute) {
        Elements elements = article.select("[itemprop='" + attribute + "']");
        if (elements.isEmpty()) {
            return "";
        } else {
            return elements.first().text();
        }
    }

    @NonNull
    private String extractDates(@NonNull Element article) {
        StringBuilder builder = new StringBuilder();
        Elements datePublished = article.select("[itemprop='datePublished']");
        if (!datePublished.isEmpty()) {
            builder.append("Publi?? le ")
                    .append(datePublished.first().text());
        }
        Elements dateModified = article.select("[itemprop='dateModified']");
        if (!dateModified.isEmpty()) {
            builder.append(", modifi?? le ")
                    .append(dateModified.first().text());
        }
        return builder.toString();
    }

    @NonNull
    private List<Model> extractParagraphs(@NonNull Element article) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean displayTweets = sharedPreferences.getBoolean("displayTweets", false);

        List<Model> p = new ArrayList<>();
        Elements articleBody = article.select("[itemprop='articleBody']");
        if (articleBody.isEmpty()) {
            return p;
        }
        Element body = articleBody.first();
        Elements elements = body.children();
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            // Ignore "?? lire" ("Read also") parts which don't add much information on mobile phones
            if (element.hasClass("lire")) {
                continue;
            }

            Elements figures = element.getElementsByTag("figure");
            // Text or figure ?
            if (figures.isEmpty()) {

                if (element.is("div.snippet.multimedia-embed")) {
                    // Parse graphs if possible, otherwise just ignore this multimedia snippet
                    boolean hasGraph = !element.select("div.graphe").isEmpty();
                    boolean hasScript = !element.select("script").isEmpty();
                    if (hasGraph && hasScript) {
                        GraphExtractor graphExtractor = new GraphExtractor(this, element.select("script").html());
                        Elements hasLegend = element.select("h2.ca-heading-sub.titre");
                        if (!hasLegend.isEmpty()) {
                            graphExtractor.setLegend(hasLegend.first().html());
                        }
                        Chart graph = graphExtractor.generate();
                        if (graph != null) {
                            p.add(new Model(GraphExtractor.getModelType(graph), graph));
                        }
                    }
                    continue;
                }

                if (element.is("blockquote.twitter-tweet")) {
                    if (displayTweets) {
                        TextView content = new TextView(this);
                        fromHtml(content, element.html());
                        Button link = new Button(this);
                        Elements links = element.select("a[href]");
                        if (atLeastOneChild(links)) {
                            link.setContentDescription("https:" + links.first().attr("href"));
                        }
                        CardView cardView = new CardView(this);
                        cardView.addView(content);
                        cardView.addView(link);
                        p.add(new Model(Model.TWEET_TYPE, cardView));
                    }
                    continue;
                }

                // Cleanup hyperlink and keep only the value
                element.select("a[href]").unwrap();

                // Cleanup style markup and script which should be placed on top
                if (element.is("style")) {
                    element.remove();
                    continue;
                }
                if (element.is("script")) {
                    element.remove();
                    continue;
                }

                TextView t = new TextView(this);
                fromHtml(t, element.html());

                boolean hasIntertitre = element.is("h2.intertitre");
                if (!hasIntertitre) {
                    hasIntertitre = !element.select("h2.intertitre").isEmpty();
                }

                if (hasIntertitre) {
                    t.setTypeface(Typeface.SERIF);
                    t.setPadding(0, Constants.PADDING_TOP_SUBTITLE, 0, Constants.PADDING_BOTTOM_SUBTITLE);
                    t.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_description));
                } else {
                    t.setPadding(0, 0, 0, Constants.PADDING_BOTTOM);
                    t.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.article_body));
                }

                if (element.is("p.question")) {
                    t.setTypeface(null, Typeface.BOLD);
                }

                if (element.is("h2.tag") && element.children().size() > 0) {
                    String cssClass = element.child(0).attr("class");
                    t.setAllCaps(true);
                    RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 24, 0, 24);
                    t.setLayoutParams(lp);
                    t.setPadding(Constants.PADDING_LEFT_RIGHT_TAG, Constants.PADDING_BOTTOM, Constants.PADDING_LEFT_RIGHT_TAG, Constants.PADDING_BOTTOM);
                    switch (cssClass) {
                        case TAG_FAKE:
                            t.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tag_red));
                            break;
                        case TAG_TRUE:
                            t.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tag_green));
                            break;
                        case TAG_MOSTLY_TRUE:
                            t.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tag_yellow));
                            break;
                        case TAG_FORGOTTEN:
                            t.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tag_grey));
                            break;
                        default:
                            break;
                    }
                }
                p.add(new Model(t));

            } else {
                // If image is on first position in the DOM, it's useless to display once more: it's already displayed in the toolbar
                if (i > 0) {
                    Element figure = figures.first();
                    Elements images = figure.getElementsByTag("img");
                    if (!images.isEmpty()) {
                        p.add(new Model(Model.IMAGE_TYPE, images.first().attr("src")));
                    }
                }
            }
        }
        return p;
    }
}
