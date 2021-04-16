
package kolblibrary.kolblibrary.adminactivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.BaseActivity;
import kolblibrary.kolblibrary.internal.Networking;


/**
 *
 * class NewBook allows for a new book to be added to the database
 * @author Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
 * @version 1.0
 */

public class NewBook extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        // input fields for information on new books
        final EditText titleEditText = (EditText) findViewById(R.id.titleEditText);
        final EditText authorEditText = (EditText) findViewById(R.id.authorEditText);
        final EditText subjectEditText = (EditText) findViewById(R.id.subjectEditText);
        final EditText isbnEditText = (EditText) findViewById(R.id.isbnEditText);
        Button addBookBTN = (Button) findViewById(R.id.addBookBTN);

        //Event Handler
        addBookBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stores input field data into String variable
                String ttle =  titleEditText.getText().toString();
                String athr = authorEditText.getText().toString();
                String sbjct = subjectEditText.getText().toString();
                String ISBN = isbnEditText.getText().toString();

                //sends new book data over the network
                char sep = (char) 0x00;
                String combined = "addBook" + sep + ttle + sep + athr + sep + sbjct + sep + ISBN;
                Networking.sendMessage(combined.getBytes());
            }
        });


    }
}
