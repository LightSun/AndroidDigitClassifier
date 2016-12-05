package com.sdsmdg.harjot.digitrecognition;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity {

    ImageView selectedImage;
    Button sendBtn, selectBtn;
    EditText urlText;

    String imagePath = null;
    File imageFile = null;

    HttpClient httpclient;
    HttpPost httppost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlText = (EditText) findViewById(R.id.url_text);
        selectedImage = (ImageView) findViewById(R.id.selected_image);
        selectBtn = (Button) findViewById(R.id.image_select_btn);
        sendBtn = (Button) findViewById(R.id.send_btn);

        httpclient = new DefaultHttpClient();

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, 1);

                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                httppost = new HttpPost(urlText.getText().toString());
                new Post().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                if (imageFile != null) {
//                    try {
//                        Log.d("HTTPUPLOAD",imagePath);
////                        params.put("file", new File(imagePath));
//                        params.put("file", imageFile);
//                        client.post(urlText.getText().toString(), params, new AsyncHttpResponseHandler() {
//                            @Override
//                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                                Log.d("HTTPUPLOAD",":Success");
//                                Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                                Log.d("HTTPUPLOAD",":Failure");
//                                Toast.makeText(MainActivity.this, "Failure!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        imagePath = getPath(imageUri);
                        imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Toast.makeText(MainActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "NO", Toast.LENGTH_SHORT).show();
                        }
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap returnedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage.setImageBitmap(returnedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            case 2: {
                if (resultCode == RESULT_OK) {
                    byte[] byteData = data.getByteArrayExtra("data");
                    Bitmap bmp = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
                    selectedImage.setImageBitmap(bmp);
                }
            }
        }
    }

    public class Post extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (imageFile != null) {

//                    Toast.makeText(MainActivity.this, imagePath, Toast.LENGTH_SHORT).show();

//                        File f = new File(imagePath);
                    FileBody fb = new FileBody(imageFile);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("file", fb);

//                        Toast.makeText(MainActivity.this, f.toString() + ":" + fb.toString() + ":" + builder.toString(), Toast.LENGTH_SHORT).show();

                    HttpEntity entity = builder.build();

                    httppost.setEntity(entity);

                    Log.d("TIME", "starting");

                    HttpResponse response = httpclient.execute(httppost);


                    Log.d("TIME", "ending : " + response);
                    if (response != null) {
                        Log.d("TIME", "ending : " + response.toString());
//                        Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    }

                    response.getEntity().consumeContent();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
