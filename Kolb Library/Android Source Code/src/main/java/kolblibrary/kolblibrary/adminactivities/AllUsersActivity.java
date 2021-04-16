/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.adminactivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.AllUsersAdapter;
import kolblibrary.kolblibrary.adapters.CatalogSearchAdapter;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.useractivities.BookViewActivity;

import kolblibrary.kolblibrary.internal.BaseActivity;

/**
 * class AllUsersActivity allows for an admin to view all other users in the database, also allows
 * for them to open an activity to create a new user
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class AllUsersActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        ListView u = findViewById(R.id.AllUsersView);
        final AllUsersAdapter csa = new AllUsersAdapter(this, AdminInfo.userslist);
        u.setAdapter(csa);

        Button newb = (Button) findViewById(R.id.NewUserBtn);
        newb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tempo = new Intent(AllUsersActivity.this, NewUserActivity.class);
                startActivity(tempo);
            }
        });


    }
}
