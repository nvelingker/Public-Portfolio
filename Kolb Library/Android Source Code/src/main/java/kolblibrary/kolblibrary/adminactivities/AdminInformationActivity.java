/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adminactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;








import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.useractivities.HomeActivity;
import kolblibrary.kolblibrary.useractivities.TransactionConfirmationActivity;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.useractivities.UserHomeScreenActivity;
/**
 * class AdminInformationActivity allows an admin to be able to reserve a book for however long
 * they please, also giving them the option to completely remove a book from circulation
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class AdminInformationActivity extends AppCompatActivity {
    int inc = 0;
    @Override

    /**
     * OnCreate method of the class.  Called when the activity is first created.
     * This is where you initialize the activity
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_information);

        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.confirmandbook");
        final int type = (int) extras.get("kolblibrary.kolblibrary.transactiontype");

        final Button contuni = findViewById(R.id.AdminContBtn);
        contuni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.AdminInpTxt);
                int i = Integer.parseInt(input.getText().toString());
                if(i<0){
                    //set toast
                    return;
                }
                else if(i == 0) {
                    if (inc == 0) {
                        TextView warning = findViewById(R.id.warn1);
                        warning.setVisibility(View.VISIBLE);
                        contuni.setText("confirm");
                        inc++;
                    } else if (inc == 1) {
                        //needs implement
                        UI.displayText(AdminInformationActivity.this , "\"" + about.getTitle() + "\" has been removed from circulation.");
                        Intent j = new Intent(getApplicationContext(), UserHomeScreenActivity.class);
                        startActivity(j);
                    }
                }
                else if(i>0){
                    Intent j = new Intent(getApplicationContext(), TransactionConfirmationActivity.class);
                    j.putExtra("kolblibrary.kolblibrary.bookchecked", about);
                    j.putExtra("kolblibrary.kolblibrary.type", type);
                    j.putExtra("kolblibrary.kolblibrary.duration", i);
                    startActivity(j);
                }



            }
        });
    }
}
