package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import kolblibrary.kolblibrary.adminactivities.AdminInformationActivity;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.internal.Networking;
import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.SelfUserInfo;

/**
 * This class GetPswdActivity requires the user to confirm his/her password before checking out or reserving a book
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class GetPswdActivity extends AppCompatActivity {

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_pswd);

        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.bookchecked");
        final int transactiontype = (int) extras.get("kolblibrary.kolblibrary.transactiontype");

        Button j = findViewById(R.id.ContinueButton);
        j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText e = findViewById(R.id.PswdInpTxt);
                String temp = e.getText().toString();
                if(Networking.checkPassword(temp)){
                    Log.d("message", "hallelujah");
                    // Regular user.  Call TransactionConfirmationActivity
                    if(SelfUserInfo.userType == 0){
                        Intent temp0 = new Intent(GetPswdActivity.this, TransactionConfirmationActivity.class);
                        temp0.putExtra("kolblibrary.kolblibrary.bookchecked", about);
                        temp0.putExtra("kolblibrary.kolblibrary.type", transactiontype);
                        temp0.putExtra("kolblibrary.kolblibrary.duration", 14);
                    }
                    // This user is a teacher.  Call TeacherInformationActivity
                    if(SelfUserInfo.userType == 1){
                        Intent temp1 = new Intent(GetPswdActivity.this, TeacherInformationActivity.class);
                        temp1.putExtra("kolblibrary.kolblibrary.confirmandbook", about);
                        temp1.putExtra("kolblibrary.kolblibrary.transactiontype", transactiontype);
                        startActivity(temp1);
                    }
                    // This user is an Administrator.  TeacherInformationActivity
                    if(SelfUserInfo.userType == 2){
                        Intent temp1 = new Intent(GetPswdActivity.this, AdminInformationActivity.class);
                        temp1.putExtra("kolblibrary.kolblibrary.transactiontype", transactiontype);
                        temp1.putExtra("kolblibrary.kolblibrary.confirmandbook", about);
                        startActivity(temp1);
                    }
                }
                else{
                    TextView reject = findViewById(R.id.RejectView);
                    reject.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
