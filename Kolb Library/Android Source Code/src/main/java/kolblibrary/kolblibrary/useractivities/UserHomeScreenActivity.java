package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.SelfUserInfo;

/**
 * This class creates the user home page. Allows the user to view some of their own books,
 * or gives them access to other things via naviagtion drawer.  If the user is an admin,
 * they can view other functions via the navigation drawer
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class UserHomeScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_screen);


        final TextView welcomeUserTextView = findViewById(R.id.welcomeUserTextView);
        if(SelfUserInfo.isNewUser) {
            welcomeUserTextView.setText("Welcome,\n" + SelfUserInfo.firstName + " " + SelfUserInfo.lastName + "!");
        } else {
            welcomeUserTextView.setText("Welcome Back,\n" + SelfUserInfo.firstName + " " + SelfUserInfo.lastName + "!");
        }


        Button searchCatalogButton = findViewById(R.id.searchCatalogButton);

        searchCatalogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent catalogSearch = new Intent(UserHomeScreenActivity.this,CatalogSearchActivity.class);
                startActivity(catalogSearch);
            }
        });

        Button myBooksButton = findViewById(R.id.ChkdOutBooksBtn);
        myBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mybooksview = new Intent(UserHomeScreenActivity.this, MyCheckoutsActivity.class);
               Networking.update(UserHomeScreenActivity.this, mybooksview);
            }
        });

        Button reserved = (Button) findViewById(R.id.ReservedBtn);
        reserved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserHomeScreenActivity.this, MyReservedActivity.class);
                Networking.update(UserHomeScreenActivity.this,i);
            }
        });

        Button LogoutBtn = (Button) findViewById(R.id.LogoutBtn);
        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Networking.logout(view.getContext());
            }
        });


    }
}
