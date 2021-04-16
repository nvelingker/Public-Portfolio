package atm.nasaimages.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    public static Uri getLocalBitmapURI(Bitmap bitmap, AppCompatActivity context) {
        Uri res = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_nasa_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            res = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
