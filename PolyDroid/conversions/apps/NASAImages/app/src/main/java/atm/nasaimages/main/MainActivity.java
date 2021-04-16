package atm.nasaimages.main;

import android.Manifest;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import atm.nasaimages.R;
import atm.nasaimages.api.NASAAPI;
import atm.nasaimages.api.NASAAPIImpl;
import atm.nasaimages.pojo.NASAItem;
import atm.nasaimages.pojo.NASAItemData;
import atm.nasaimages.utils.BitmapUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE_CODE = 112;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private ImageButton downloadButton;
    private ImageButton infoButton;
    private ImageButton shareButton;
    private ImageButton wallpaperButton;
    private PhotoView photoView;
    private NASAAPI api;
    private NASAItem currentItem;
    private String currentItemURL;
    private WallpaperManager wallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new NASAAPIImpl();
        wallpaperManager = WallpaperManager.getInstance(this);

        // Get UI objects
        photoView = (PhotoView) findViewById(R.id.photoView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        downloadButton = (ImageButton) findViewById(R.id.downloadButton);
        infoButton = (ImageButton) findViewById(R.id.infoButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        wallpaperButton = (ImageButton) findViewById(R.id.wallpaperButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Progress bar config
        progressBar.setVisibility(GONE);
        progressBar.setIndeterminate(true);

        // Disable wallpaperButton if the device is lower than Kitkat
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            wallpaperButton.setVisibility(GONE);
        }

        // OnClick Listener to all buttons of the activity
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fab:
                        loadRandomImage();
                        break;
                    case R.id.downloadButton:
                        if (currentItemURL != null && currentItem != null) {
                            if (hasWriteToStoragePermissions()) {
                                downloadImage(currentItemURL);
                            } else {
                                requestPerms();
                            }
                        }
                        break;
                    case R.id.infoButton:
                        if (currentItem != null) {
                            showInfoDialog();
                        }
                        break;
                    case R.id.shareButton:
                        if (currentItemURL != null) {
                            shareImage(currentItemURL);
                        }
                        break;
                    case R.id.wallpaperButton:
                        if (currentItemURL != null) {
                            setWallpaper(currentItemURL);
                        }
                    default:
                        break;
                }
            }
        };

        // Set the previous listener to all buttons of the activity
        downloadButton.setOnClickListener(clickListener);
        infoButton.setOnClickListener(clickListener);
        shareButton.setOnClickListener(clickListener);
        fab.setOnClickListener(clickListener);
        wallpaperButton.setOnClickListener(clickListener);

        // Load random image at the start of the activity
        loadRandomImage();
    }

    // IMAGE HANDLING

    // Execute a new thread that gathers a random asset's URL and loads it with Picasso
    // Also, stores the currentItem object
    private void loadRandomImage() {
        new RandomItemImageURLTask(this).execute(api);
    }

    private void shareImage(String url) {
        Picasso.with(this).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM,
                        BitmapUtils.getLocalBitmapURI(bitmap, MainActivity.this));
                // Image + Image title + Date created + App link
                String dateCreated = null;
                if (currentItem.getData().get(0).getDateCreated() != null) {
                    String[] split = currentItem.getData().get(0).getDateCreated().split("T");
                    dateCreated = split[0];
                }
                intent.putExtra(Intent.EXTRA_TEXT, currentItem.getData().get(0).getTitle()
                        + " (" + dateCreated + ")" + " | NASA Images " + "https://gitlab.com/atorresm/NASAImages");
                startActivity(Intent.createChooser(intent, "Share image"));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(MainActivity.this, R.string.error_share, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }

    private void downloadImage(String url) {
        Picasso.with(this).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Save the file to storage/Pictures/NASAImages/{NASA_ID}.jpg
                // Create the storage/Pictures/NASAImages directory
                File folderDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getPath() + "/NASAImages/");
                folderDir.mkdirs();
                File file = new File(folderDir, currentItem.getData().get(0).getNasaID() + ".jpg");
                if(!file.isFile()) {
                    try {
                        file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        Toast.makeText(MainActivity.this, R.string.success_download, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, R.string.error_downloading, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    // Tell the scanner about the new file so it is available instantly
                    MediaScannerConnection.scanFile(MainActivity.this,
                            new String[]{file.toString()}, null, null);
                } else {
                    Toast.makeText(MainActivity.this, R.string.image_already_downloaded, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(MainActivity.this, R.string.error_downloading, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    private void setWallpaper(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File appFolder = new File(this.getFilesDir(), "nasaimages");
            appFolder.mkdirs();
            final File image = new File(appFolder, currentItem.getData().get(0).getNasaID() + ".jpg");
            Picasso.with(this).load(url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                    if (!image.isFile()) {
                        try {
                            image.createNewFile();
                            FileOutputStream out = new FileOutputStream(image);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

            // once the image has been saved to the app-private storage, create the
            // wallpaper intent with its uri
            Uri imageContentUri = FileProvider.getUriForFile(this.getApplicationContext(), "atm.nasaimages.fileprovider", image);
            startActivity(wallpaperManager.getCropAndSetWallpaperIntent(imageContentUri));
        }
    }

    // DIALOGS

    private void showInfoDialog() {
        // Create the actual dialog
        NASAItemData currentData = currentItem.getData().get(0);
        MaterialDialog infoDialog =
                new MaterialDialog.Builder(this)
                        .title(currentData.getTitle())
                        .customView(R.layout.info_view, true)
                        .positiveText("OK")
                        .build();

        // Get TextView elements from the dialog layout
        TextView descriptionView = (TextView) infoDialog.getCustomView().findViewById(R.id.itemDescription);
        TextView dateCreatedView = (TextView) infoDialog.getCustomView().findViewById(R.id.itemDateCreated);
        TextView centerView = (TextView) infoDialog.getCustomView().findViewById(R.id.itemCenter);
        TextView nasaIDView = (TextView) infoDialog.getCustomView().findViewById(R.id.itemNASAID);

        // Check if there are null data and replace it with "-"
        if (currentData.getDescription() != null) {
            descriptionView.setText(currentData.getDescription());
        } else {
            descriptionView.setText("-");
        }
        if (currentData.getCenter() != null) {
            centerView.setText(currentData.getCenter());
        } else {
            centerView.setText("-");
        }
        if (currentData.getNasaID() != null) {
            nasaIDView.setText(currentData.getNasaID());
        } else {
            nasaIDView.setText("-");
        }
        if (currentData.getDateCreated() != null) {
            // The dates are given in the following format:
            // YYYY-MM-DDTHH:mm:ssZ
            // Return only YYYY-MM-DD
            String[] split = currentData.getDateCreated().split("T");
            dateCreatedView.setText(split[0]);
        }
        else {
            dateCreatedView.setText("-");
        }
        // show the dialog
        infoDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new MaterialDialog.Builder(this)
                        .title(R.string.about)
                        .content(R.string.about_body, true)
                        .positiveText("OK")
                        .build()
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // PERMISSIONS

    @NonNull
    private Boolean hasWriteToStoragePermissions() {
        int permissionGranted;
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for (String perm : permissions) {
            permissionGranted = checkCallingOrSelfPermission(perm);
            if (!(permissionGranted == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_WRITE_STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Boolean allowed = true;
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_CODE:
                for (int perm : grantResults) {
                    allowed = allowed && (perm == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // Permission was not granted
                allowed = false;
                break;
        }
        if (allowed) {
            downloadImage(currentItemURL);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Explain why permission is needed
                    new MaterialDialog.Builder(this)
                            .content(R.string.explanation_permission_storage)
                            .positiveText("OK")
                            .build()
                            .show();
                }
            }
        }
    }

    // ASYNCHRONOUS TASK
    // The purpose of the following inner class is to execute networking on secondary threads
    // (required by Android)
    private class RandomItemImageURLTask extends AsyncTask<NASAAPI, Integer, Void> {
        private Context context = null;
        Dialog errorDialog = new MaterialDialog.Builder(MainActivity.this)
                                .content(R.string.error_loading_image)
                                .positiveText("RETRY")
                                .negativeText("CLOSE APP")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        MainActivity.this.recreate();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        MainActivity.this.finish();
                                    }
                                })
                                .build();

        private RandomItemImageURLTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // show the progress bar and hide the previous image when a new one is being loaded
            progressBar.setVisibility(VISIBLE);
            photoView.setImageResource(android.R.color.transparent);
        }

        @Override
        protected Void doInBackground(NASAAPI... api) {
            currentItem = api[0].getRandomItem();
            if(currentItem == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorDialog.show();
                    }
                });
                return null;
            }
            String url = null;
            // Request an image's url with medium size
            // make sure the received URL is valid (not-null)
            while (url == null) {
                url = api[0].getAssetsFromItem(currentItem.getData().get(0).getNasaID())
                        .getCollection().getImage("medium");
            }
            currentItemURL = url;
            runPicassoOnUiThread(url, this.context);
            return null;
        }

        // Picasso needs to be called from the UI thread
        private void runPicassoOnUiThread(final String url, final Context context) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(context).load(url).into(photoView);
                }
            });
        }

        @Override
        protected void onPostExecute(Void res) {
            // Hide the progress bar when the image is loaded
            progressBar.setVisibility(GONE);
        }
    }
}