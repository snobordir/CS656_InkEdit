package com.bobby.cs656_inkedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private DrawingView drawView;
    private Button saveButton, newButton, loadButton;
    EditText fileNameEdit;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Give us access to the drawing view
        drawView = (DrawingView) findViewById(R.id.drawing);

        // Create listeners for our buttons
        saveButton = (Button) findViewById(R.id.save_btn);
        newButton = (Button)  findViewById(R.id.new_btn);
        loadButton = (Button) findViewById(R.id.load_btn);

        saveButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        loadButton.setOnClickListener(this);

        fileNameEdit = (EditText) findViewById(R.id.filename_Edit);

        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ink_"); // get path

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.save_btn){
            // Save drawing to system memory

            //String fileName = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String fileName = fileNameEdit.getText().toString();
            // generate the image path
            //String imagePath = Environment.getExternalStorageDirectory().toString() + File.separator + "Pictures" + File.separator + fileName + ".png";


            String imagePath =  file.getPath() + fileName + ".png";


            drawView.setDrawingCacheEnabled(true); // Allow caching of our ink


            // This comment is solely to test github
            int pointless = 0;

            try {
                // save the image as png
                FileOutputStream out = new FileOutputStream(imagePath);

                String[] strings = new String[1];
                //strings[0] = file.getPath();
                strings[0] = imagePath;
                // compress the image to png and pass it to the output stream
                drawView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);

                // save the image
                out.flush();
                out.close();

                // Invoke media scanner to recognize new picture...... PLEASE WORK.
               MediaScannerConnection.scanFile(getApplicationContext(),strings,null,null);


                Toast success = Toast.makeText(getApplicationContext(), "Saved as \"" + fileNameEdit.getText()
                        + ".png\" in 'Pictures' folder!", Toast.LENGTH_SHORT);
                success.show();

            } catch (Exception error) {
                //Log.e("Error saving image", error.getMessage());
                Toast fail = Toast.makeText(getApplicationContext(), "FileOutputStream failed.", Toast.LENGTH_SHORT);
                fail.show();
            }

            drawView.destroyDrawingCache(); // Delete cached ink

            /*
            drawView.setDrawingCacheEnabled(true); // Allow caching of our ink
            String savedInk = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                    drawView.getDrawingCache(), fileNameEdit.getText() + ".png", "myDrawing");
            // Verify to user
            if(savedInk == null) {
                Toast fail = Toast.makeText(getApplicationContext(), "Failed to save.", Toast.LENGTH_SHORT);
                fail.show();

            }
            else {
                Toast success = Toast.makeText(getApplicationContext(), "Saved as \"" + fileNameEdit.getText()
                        + ".png\" in gallery!", Toast.LENGTH_SHORT);
                success.show();

            }
            drawView.destroyDrawingCache(); // Delete cached ink
            */
        }
        else if(v.getId()==R.id.new_btn){
            // New drawing. Just clear the screen.
            drawView.newDrawing();
        }
        else if(v.getId()==R.id.load_btn){
            // Load a drawing. Create intent to get bitmap
            Intent imageIntent = new Intent();
            imageIntent.setType("image/*");
            imageIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(imageIntent,"Select ink"), 1);
        }
    }

    // Use bitmap from load button press to put on canvas
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri loadedImageUri = data.getData();
                Bitmap loadedImage = null;  //
                try {
                    loadedImage  = MediaStore.Images.Media.getBitmap(this.getContentResolver(), loadedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("YOU SUCK...at getting a bitmap.");
                }
                if(loadedImage == null){
                    Toast fail = Toast.makeText(getApplicationContext(), "Bitmap was null.", Toast.LENGTH_SHORT);
                    fail.show();
                }
                else {
                    Toast success = Toast.makeText(getApplicationContext(), "Loading image!", Toast.LENGTH_SHORT);
                    success.show();
                    drawView.loadBitmap(loadedImage);
                }


            }
        }
    }
}
