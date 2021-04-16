package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.UI;

/**
 * This class creates the share screen. Share screen allows for a user to share the app with others via twitter
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */


public class ShareActivity extends BaseActivity {

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Button test = (Button) findViewById(R.id.TwitterShareBtn);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TweetComposer.Builder builder = new TweetComposer.Builder(ShareActivity.this)
                        .text("Kolb Library always suits my needs!");
                builder.show();
            }
        });
    }


}
