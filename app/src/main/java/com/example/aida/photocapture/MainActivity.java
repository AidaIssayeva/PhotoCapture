package com.example.aida.photocapture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private Uri fileUri; //file uri to store image/video
    private ImageView imgPreview;
    private Button btnCapturePicture;
    private ScaleGestureDetector sGD;
    private Matrix matrix=new Matrix();
    float saveScale=1.0f;
    static String DEBUG_TAG="Custom view info";
    float x=0,y=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1.we'll store pics in a file.we set the file name here
        String outputFilePath=Environment.getExternalStorageDirectory().getAbsolutePath()+ "/PhotoCapture.jpg";
        fileUri=Uri.fromFile(new File (outputFilePath));

        //2.set the user interface elements
        imgPreview=(ImageView) findViewById(R.id.imgPreview);



        btnCapturePicture=(Button) findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //3.this method is called to start camera
                captureImage();
            }
        });
        sGD=new ScaleGestureDetector(this,new ScaleListener());
    }


    //Capturing camera Image will launch camera app request image capture
    private void captureImage(){
        //4.first check if device has a camera
        boolean deviceHasCamera=getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        //5.phone has camera.let's start the native camera
        if(deviceHasCamera){

            //6.create intent to take a pic
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //7.tell the intent that we need the camera to store the photo in our
            //file defined earlier
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

            //8.start the activity with the intent created above.
            //when this activity finishes,the method onActivityResult(..) is called

            startActivityForResult(intent,1);
        } else{
            Log.i("Camera_APP","No camera found");
        }
    }
    //9.receiving activity result method will be called after closing the camera

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            /*10.we've successfully captured the image
            now we'll show it in the image view created earlier
             */
            try{
                /*11.now we need to ensure our photo isn't
                 unnecessarily rotated
                 */
                Matrix matrix=new Matrix();
                ExifInterface ei=new ExifInterface(fileUri.getPath());

                //12.get orientation of the photo
                int orientation=ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);

                //13.in case img is rotated,we rotate it back
                switch(orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);

                }
                //14.now we get bitmap from photo and apply rotation matrix above
                BitmapFactory.Options options=new BitmapFactory.Options();
                //down-sizing img as it can throw OutofMemory Exception for larger images
                options.inSampleSize=2;
                Bitmap bitmap=BitmapFactory.decodeFile(fileUri.getPath(),options);
                Bitmap rotateBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                //15.now display the img on the ui
                imgPreview.setImageBitmap(rotateBitmap);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public boolean onTouchEvent(MotionEvent event){
        sGD.onTouchEvent(event);


        return true;
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            saveScale*=detector.getScaleFactor();
            saveScale=Math.max(0.1f,Math.min(saveScale,5.0f));
            matrix.setScale(saveScale,saveScale);
            imgPreview.setImageMatrix(matrix);


            return true;
        }
    }
}
