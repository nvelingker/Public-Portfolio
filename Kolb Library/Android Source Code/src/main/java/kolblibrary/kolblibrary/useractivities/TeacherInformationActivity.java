package kolblibrary.kolblibrary.useractivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.objects.Book;

/**
 * This class allows for a teacher to input the amount of time they want to reserve/checkout a book
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class TeacherInformationActivity extends AppCompatActivity {

    /**
     * OnCreate method of the class.  Called when the activity is first created. This is where you initialize the activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_information);

        Bundle extras = getIntent().getExtras();
        final Book about = (Book) extras.get("kolblibrary.kolblibrary.confirmandbook");
        final int type = (int) extras.get("kolblibrary.kolblibrary.transactiontype");

        Button continu = findViewById(R.id.AdminContBtn);
        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText day = findViewById(R.id.teacherInpTxt);
                int i = Integer.parseInt(day.getText().toString());
                if(i<14 || i>365){
                    Toast warn = new Toast(getApplicationContext());
                    warn.setDuration(Toast.LENGTH_LONG);
                    warn.setText("Please enter a number from 14 to 365");
                    warn.show();
                    return;
                }
                else{
                    Intent j = new Intent(TeacherInformationActivity.this, TransactionConfirmationActivity.class);
                    j.putExtra("kolblibrary.kolblibrary.bookchecked", about);
                    j.putExtra("kolblibrary.kolblibrary.type", type);
                    j.putExtra("kolblibrary.kolblibrary.duration", i);
                    startActivity(j);
                }
            }
        });
    }
}
