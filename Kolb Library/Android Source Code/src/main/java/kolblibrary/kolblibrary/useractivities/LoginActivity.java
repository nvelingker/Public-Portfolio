package kolblibrary.kolblibrary.useractivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import kolblibrary.kolblibrary.internal.AdminInfo;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.RegistrationService;
import kolblibrary.kolblibrary.internal.SelfUserInfo;
import kolblibrary.kolblibrary.internal.UI;

/**
 * class LoginActivity allows for a User to login to the app.  If they choose to save their password,
 * it will be saved in preferences until they sign out
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */


public class LoginActivity extends AppCompatActivity {
    final String PREF_NAME = "kolblibrary.kolblibrary.loginpreferences";
    SharedPreferences prefs;
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    public static SharedPreferences loginPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
        instance = this;
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);


        //************Google SignIn*******************
        //Configure sign-in to request the user's ID, email address, and basic
        //profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        //Snackbar.make(view, "Hello" + findViewById(R.id.helloText).toString() , Snackbar.LENGTH_LONG)
                        //        .setAction("Action", null).show();
                        break;

                }
            }
        });
        //************Google SignIn*******************

        // Starts the registration service to obtain the necessary tokens for push notifications
        try {
            Intent i = new Intent(this, RegistrationService.class);
            startService(i);

            Networking.connect(3000);

            final SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            loginPrefs = prefs;
            String preferencesusername = prefs.getString("username", null);
            String preferencespassword = prefs.getString("password", null);
            if (!(preferencesusername == null) && !(preferencespassword == null)) {

                Networking.waitForConnect(3000);
                String response = Networking.sendCredentials(preferencesusername, preferencespassword);
                if (response == null) {
                    try {
                        Networking.setPassword(preferencespassword);
                        SelfUserInfo.firstName = new String(Networking.waitForMessage(1000));
                        SelfUserInfo.lastName = new String(Networking.waitForMessage(1000));
                        String temptype = new String(Networking.waitForMessage(1000));
                        if (temptype.equals("Administrator")) {
                            SelfUserInfo.userType = 2;
                            // ayy we an admin!
                        }
                        if (temptype.equals("Teacher")) {
                            SelfUserInfo.userType = 1;
                        }
                        if (temptype.equals("Student")) {
                            SelfUserInfo.userType = 0;
                        }
                        Log.d("message", "usertype:" + SelfUserInfo.userType);
                        SelfUserInfo.isNewUser = false;
                        SelfUserInfo.reservedBooks = Networking.waitForBooks(2000);
                        SelfUserInfo.checkedBooks = Networking.waitForBooks(2000);
                        if (SelfUserInfo.userType == 2) {
                            AdminInfo.reserverequests = Networking.waitForRequests(2000);
                            AdminInfo.checkoutrequests = Networking.waitForRequests(2000);
                            AdminInfo.allcheckouts = Networking.waitForAllCheckouts(2000);
                            AdminInfo.userslist = Networking.waitForUsers(2000);
                        }
                        Intent userHome = new Intent(LoginActivity.this, UserHomeScreenActivity.class);
                        startActivity(userHome);
                    } catch (Networking.NetworkTimedOutException ne) {
                        ne.printStackTrace();
                    }
                }
            }

            final EditText usernameinp = findViewById(R.id.usernameInpTxt);
            final EditText passwordinp = findViewById(R.id.passwordInpTxt);
            final TextView errorTextView = findViewById(R.id.errorTextView);
            Button loginBtn = findViewById(R.id.loginBtn);

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = usernameinp.getText().toString();
                    String password = passwordinp.getText().toString();
                    String response = Networking.sendCredentials(username, password);
                    //String response = null;
                    if (response == null) {
                        Log.d("message", "attempting to set pass");
                        Networking.setPassword(password);
                        CheckBox save = findViewById(R.id.saveLoginCheckBox);
                        if (save.isChecked()) {
                            SharedPreferences.Editor e = prefs.edit();
                            e.putString("username", username);
                            e.putString("password", password);
                            e.commit();
                        }
                        // move on to next page
                        try {
                            SelfUserInfo.firstName = new String(Networking.waitForMessage(1000));
                            SelfUserInfo.lastName = new String(Networking.waitForMessage(1000));
                            String temptype = new String(Networking.waitForMessage(1000));
                            if (temptype.equals("Administrator")) {
                                SelfUserInfo.userType = 2;
                            }
                            if (temptype.equals("Teacher")) {
                                SelfUserInfo.userType = 1;
                            }
                            if (temptype.equals("Student")) {
                                SelfUserInfo.userType = 0;
                            }
                            Log.d("message", "usertype:" + SelfUserInfo.userType);
                            SelfUserInfo.isNewUser = false;
                            SelfUserInfo.reservedBooks = Networking.waitForBooks(2000);
                            SelfUserInfo.checkedBooks = Networking.waitForBooks(2000);
                            if (SelfUserInfo.userType == 2) {
                                AdminInfo.reserverequests = Networking.waitForRequests(2000);
                                AdminInfo.checkoutrequests = Networking.waitForRequests(2000);
                                AdminInfo.allcheckouts = Networking.waitForAllCheckouts(2000);
                                AdminInfo.userslist = Networking.waitForUsers(2000);
                            }
                            Intent userHome = new Intent(LoginActivity.this, UserHomeScreenActivity.class);
                            startActivity(userHome);
                        } catch (Networking.NetworkTimedOutException ne) {
                            UI.networkTimedOut(LoginActivity.this);
                            ne.printStackTrace();
                        }

                    } else {
                        errorTextView.setText(response);
                    }
                }
            });
        }catch(Exception e){e.printStackTrace();}





    }

    private static LoginActivity instance;
    public static LoginActivity getInstance(){
        return instance;
    }

    //************Google SignIn*******************

    /**
     *    @Override
    protected void onStart() {
    super.onStart();
    // Check for existing Google Sign In account, if the user is already signed in
    // the GoogleSignInAccount will be non-null.
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    //updateUI(account);
    }
     **/

    /**
     * Sign In if the google sing in button is clicked
     */
    private void signIn() {
        Log.d("beebwoop", "Aleo Blacc signed in");
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * method to sign out from the google account
     */
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // ...
                    }
                });
    }

    /**
     * Check the reqeust code of the google sign in and calls handler method
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("beebwoop", "Aleo Blacc went to onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Handle results of Google signin. Checks credentials against the database.
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d("beebwoop", "Aleo Blacc went to handleSigninResult");
        try {
            GoogleSignInAccount account = null;
                account = completedTask.getResult(ApiException.class);
            String fName = account.getDisplayName().split(" (?!.* )")[0];
            String lName = account.getDisplayName().split(" (?!.* )")[1];
            // Signed in successfully, show authenticated UI.
            Log.d("message","Email: " + account.getEmail());
            Log.d("message","Id: " + account.getId());
            Log.d("message","IdToken: " + account.getIdToken());
            Log.d("message","DisplayName: " + account.getDisplayName());
            Log.d("message","First Name: " + fName);
            Log.d("message","Last Name: " + lName);



            String  username = account.getEmail();
            String password = account.getId();

            String response = Networking.sendCredentials(username, password);
            //String response = null;
            if(response == null) {
                Log.d("message" , "attempting to set pass");
                Networking.setPassword(password);
                CheckBox save = findViewById(R.id.saveLoginCheckBox);
                if(save.isChecked()){
                    SharedPreferences.Editor e = prefs.edit();
                    e.putString("username", username);
                    e.putString("password", password);
                    e.commit();
                }
                // move on to next page
                try {
                    SelfUserInfo.firstName = new String(Networking.waitForMessage(1000));
                    SelfUserInfo.lastName = new String(Networking.waitForMessage(1000));
                    String temptype = new String(Networking.waitForMessage(1000));
                    if(temptype.equals("Administrator")){
                        SelfUserInfo.userType = 2;
                    }
                    if(temptype.equals("Teacher")){
                        SelfUserInfo.userType = 1;
                    }
                    if(temptype.equals("Student")){
                        SelfUserInfo.userType = 0;
                    }
                    Log.d("message", "usertype:" + SelfUserInfo.userType);
                    SelfUserInfo.isNewUser = false;
                    SelfUserInfo.reservedBooks = Networking.waitForBooks(2000);
                    SelfUserInfo.checkedBooks = Networking.waitForBooks(2000);
                    if(SelfUserInfo.userType == 2) {
                        AdminInfo.reserverequests = Networking.waitForRequests(2000);
                        AdminInfo.checkoutrequests = Networking.waitForRequests(2000);
                        AdminInfo.allcheckouts = Networking.waitForAllCheckouts(2000);
                        AdminInfo.userslist = Networking.waitForUsers(2000);
                    }
                    Networking.setGoogleClient(mGoogleSignInClient);
                    Intent userHome = new Intent(LoginActivity.this, UserHomeScreenActivity.class);
                    startActivity(userHome);
                } catch(Networking.NetworkTimedOutException ne) {
                    UI.networkTimedOut(LoginActivity.this);
                    ne.printStackTrace();
                }

            } else {
                if(response.equals(LoginActivity.getInstance().getString(R.string.credentials_denied))){
                    Intent i = new Intent(LoginActivity.this, GoogleConfirmInformationActivity.class);
                    i.putExtra("kolblibrary.kolblibrary.account", account);
                    startActivity(i);
                }
                else{
                final TextView errorTextView = findViewById(R.id.errorTextView);
                errorTextView.setText(response);
                }
            }


        } catch (Exception e) {
            Log.d("beebwoop", "Aleo Blacc died");
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
           // Log.d("beebwoop","signInResult:failed code=" + e.getStatusCode());
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bOut));
            Log.d("beebwoop",new String(bOut.toByteArray()));
            //updateUI(null);
        }
    }
    //************Google SignIn*******************
}
