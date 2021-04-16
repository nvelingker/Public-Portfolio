/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.useractivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;

import kolblibrary.kolblibrary.R;


/**
 * This class creates the splash screen
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */
public class SplashActivity extends Activity {


    private static int SPLASH_TIME_OUT = 6000;    // Splash screen timer.  Changes this to adjust splash timer

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        imgLogo.startAnimation(animation);



        TextView tv=(TextView)findViewById(R.id.welcome);
        tv.setSelected(true);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}