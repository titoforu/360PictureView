package dhiman.a2017.tito.a360vrview.com.a360pictureview;
/*Sample Code for VR*///
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class vrViewActivity extends AppCompatActivity {

    private static final String TAG = "vrViewActivity";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int FILE_SELECT_CODE = 15;
    private Button openFile;
    private TextView fileName;
    private VrPanoramaView vrView;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    private static void openPermissionSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_view);
        openFile = (Button) findViewById(R.id.openFile);
        fileName = (TextView) findViewById(R.id.fileName);
        vrView = (VrPanoramaView) findViewById(R.id.vrView);
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(vrViewActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    showFileChooser();

                }
                else
                {
                    requestFileWrite();
                }
            }
        });


        fileName.setText("R0010120.JPG");
        panoOptions.inputType = getIntent().getIntExtra("inputType", VrPanoramaView.Options.TYPE_MONO);
        ImageLoaderTaskDefault backgroundImageLoaderTask = new ImageLoaderTaskDefault();
        backgroundImageLoaderTask.execute(panoOptions);

    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        vrView.resumeRendering();
    }

    @Override
    protected void onPause(){
        vrView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        vrView.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showFileChooser();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    openPermissionSettings(vrViewActivity.this);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void requestFileWrite(){
        Log.d(TAG, "requestFileWrite: ");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(vrViewActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(vrViewActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                openPermissionSettings(vrViewActivity.this);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(vrViewActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = null;
                    try {
                        if (resultData != null) {
                            uri = resultData.getData();
                            Log.i(TAG, "Uri: " + uri.toString());
                            String path = ImageFilePath.getPath(vrViewActivity.this,uri);
                            Log.i(TAG, "path: " + path);
                            uri = Uri.fromFile(new File(path));
                            fileName.setText(path.substring(path.lastIndexOf("/")+1));
                            panoOptions.inputType = getIntent().getIntExtra("inputType", VrPanoramaView.Options.TYPE_MONO);
                            ImageLoaderTask backgroundImageLoaderTask = new ImageLoaderTask();
                            backgroundImageLoaderTask.execute(Pair.create(uri, panoOptions));
                        }

                    }
                    catch (Exception ex) {
                        Log.e(TAG, "onActivityResult: ",ex);
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }




    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, VrPanoramaView.Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, VrPanoramaView.Options>... fileInformation) {
            VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open("andes.jpg");
                    panoOptions = new VrPanoramaView.Options();
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode default bitmap: " + e);
                    return false;
                }
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e(TAG, "Could not load file: " + e);
                    return false;
                }
            }

            vrView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTaskDefault extends AsyncTask<VrPanoramaView.Options, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(VrPanoramaView.Options... fileInformation) {
            VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open("R0010120.JPG");
                    panoOptions = new VrPanoramaView.Options();
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode default bitmap: " + e);
                    return false;
                }

            vrView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }
}
