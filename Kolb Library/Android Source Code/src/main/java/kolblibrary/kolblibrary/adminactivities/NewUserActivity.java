package kolblibrary.kolblibrary.adminactivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.useractivities.UserHomeScreenActivity;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;
/**
 *
 * class NewUserActivity allows an administrator to add a user into the database
 * @author Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
 * @version 1.0
 */

public class NewUserActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // input fields for new users to enter information
        final EditText firstName = findViewById(R.id.firstNameEditText);
        final EditText lastName = findViewById(R.id.lastNameEditText);
        final EditText username = findViewById(R.id.userNameEditText);
        final EditText password = findViewById(R.id.passwordEditText);
        Button createNewUserBTN = findViewById(R.id.createNewUserBTN);
        final Spinner occupationSpinner = findViewById(R.id.occupationSpinner);

        //adds options to occupationSpinner
        final ArrayList<String> searchList = new ArrayList<>();
        searchList.add("Student");
        searchList.add("Teacher");
        final ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, searchList);
        occupationSpinner.setAdapter(searchAdapter);

        // called when New user button is clicked
        createNewUserBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Stores input field data into String variable
                String fname =  firstName.getText().toString();
                String lname = lastName.getText().toString();
                String usname = username.getText().toString();
                String pw = password.getText().toString();
                String occ = occupationSpinner.getSelectedItem().toString();
                UserInfo info = new UserInfo();
                info.firstName = fname;
                info.lastName = lname;
                info.userName = usname;
                info.userType = searchList.indexOf(occ);

                // sends new user data over the network
                char sep = (char) 0x00;

                String answer = Networking.createUser(fname, lname, occ, usname, pw);
                if(answer == null) {
                    AdminInfo.userslist.add(info);
                    UI.displayText(NewUserActivity.this, getString(R.string.create_user_success, usname));
                    Intent i = new Intent(NewUserActivity.this, AllUsersActivity.class);
                    Networking.update(NewUserActivity.this, i);
                    //startActivity(i);
                } else {
                    UI.displayText(NewUserActivity.this, answer);
                    Log.d("MessageKeagy", answer);
                }
            }
        });
    }
}
