package kolblibrary.kolblibrary.useractivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;

public class GoogleConfirmInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_user);
        final String PREF_NAME = "kolblibrary.kolblibrary.loginpreferences";
        final EditText first = (EditText) findViewById(R.id.FirstNameEditText);
        final EditText last = (EditText) findViewById(R.id.LastNameEditText);
        Button create = (Button) findViewById(R.id.GoogleCreateUserBtn);
        Bundle extras = getIntent().getExtras();
        final GoogleSignInAccount account = (GoogleSignInAccount) extras.get("kolblibrary.kolblibrary.account");

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String firstname = first.getText().toString();
                    if (firstname == null || firstname.isEmpty()) {

                        String[] temp = account.getDisplayName().split(" ");
                        firstname = temp[0];
                    }
                    String lastname = last.getText().toString();
                    if (lastname == null || lastname.isEmpty()) {

                        String[] temp = account.getDisplayName().split(" ");
                        lastname = temp[temp.length - 1];
                    }
                    String response = Networking.createUser(firstname, lastname, "Student", account.getEmail(), account.getId());
                    if (response != null) {
                        UI.displayText(GoogleConfirmInformationActivity.this, "Google Sign In could not be completed.  Please try again or contact am administrator");

                        Intent i = new Intent(GoogleConfirmInformationActivity.this, LoginActivity.class);
                        startActivity(i);
                    } else if (response == null) {
							Networking.setPassword(account.getId());
							SelfUserInfo.firstName = firstname;
							SelfUserInfo.lastName = lastname;
							SelfUserInfo.isNewUser = true;
							SelfUserInfo.reservedBooks = new ArrayList<>();
							SelfUserInfo.checkedBooks = new ArrayList<>();
							SelfUserInfo.userType = 0;
                            Intent i = new Intent(GoogleConfirmInformationActivity.this, UserHomeScreenActivity.class);
                            startActivity(i);
                    }
                } catch(Exception e){e.printStackTrace();}
            }
        });
    }
}