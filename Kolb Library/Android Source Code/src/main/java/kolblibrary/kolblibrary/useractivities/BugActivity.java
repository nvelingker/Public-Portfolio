/*
 * @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
 */
package kolblibrary.kolblibrary.useractivities;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

        import kolblibrary.kolblibrary.R;
        import kolblibrary.kolblibrary.internal.BaseActivity;
        import kolblibrary.kolblibrary.internal.Networking;
        import kolblibrary.kolblibrary.internal.UI;

/**
 * This is an class that creates the screen for Bug Report
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */
public class BugActivity extends BaseActivity {
    /**
     * class BugActivity allows a user to send a bug report should they find an issue with the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug);

        Button BTN_submitBug = findViewById(R.id.BTN_submitBug);
        final TextView bugMessage = findViewById(R.id.bugMessage);

        BTN_submitBug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = bugMessage.getText().toString();
                if(Networking.sendBugReport(message)) {
                    Intent i = new Intent(BugActivity.this, UserHomeScreenActivity.class);
                    startActivity(i);
                    UI.displayText(BugActivity.this, "Succesfully sent bug report.");
                } else {
                    UI.displayText(BugActivity.this, "Unable to send bug report.");
                }
            }
        });

    }
}
