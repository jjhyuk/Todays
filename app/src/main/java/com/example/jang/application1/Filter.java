/*
package com.example.jang.application1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Filter extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    ImageView imageVIewInput;
    ImageView imageVIewOuput;
    private Mat img_input;
    private Mat img_output;

    static final int IMAGE_REQUEST_CODE = 101;
    String uri;

    String intentID;
    private static final String TAG = "opencv";
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }



    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

@Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else
                        {
                            read_image_file(uri);
                            imageprocess_and_showResult();
                        }
                    }
                }
                break;
        }
    }


    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  Filter.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        Intent get =getIntent();
        uri = get.getStringExtra("path");
        intentID = get.getStringExtra("id");


        //이미지 데이터를 비트맵으로 받아온다.

        ImageView image = (ImageView)findViewById(R.id.imageViewInput);

        //배치해놓은 ImageView에 set
        Glide.with(getApplicationContext()).load("file://"+uri).into(image);

        read_image_file(uri);

        imageVIewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageVIewOuput = (ImageView)findViewById(R.id.imageViewOutput);

        Button gray = findViewById(R.id.cvGray);
        Button nagative = findViewById(R.id.cvNagative);
        Button original = findViewById(R.id.cvOriginal);
        Button  capture= findViewById(R.id.cvSelect);

        original.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext()).load("file://"+uri).into(imageVIewOuput);
            }
        });

        nagative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageprocessing(img_input.getNativeObjAddr(),img_output.getNativeObjAddr());

                Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_output, bitmapOutput);

                Glide.with(getApplicationContext()).load(bitmapOutput).into(imageVIewOuput);

            }
        });

        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConverRGBtoGray(img_input.getNativeObjAddr(),img_output.getNativeObjAddr());


                Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_output, bitmapOutput);

                Glide.with(getApplicationContext()).load(bitmapOutput).into(imageVIewOuput);
            }
        });



        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageVIewOuput.buildDrawingCache();

                Bitmap captureView = ((BitmapDrawable)imageVIewOuput.getDrawable()).getBitmap();

                FileOutputStream fos;
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfnow = new SimpleDateFormat("yyyyMMddHHmmss");
                String strnow = sdfnow.format(date);
                try {

                    fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/"+strnow+".jpeg");
                    Log.d("tag", String.valueOf(Environment.getExternalStorageDirectory().toString()));

                    captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                } catch (FileNotFoundException e) {

                    e.printStackTrace();

                }


                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ Environment.getExternalStorageDirectory()+"/"+strnow+".jpeg")));
                Intent intent = new Intent(getApplicationContext(),Post.class);
                intent.putExtra("path", Environment.getExternalStorageDirectory()+"/"+strnow+".jpeg");
                intent.putExtra("id",intentID);
                startActivity(intent);
                finish();

            }
        });

if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        } else {
            //이미 사용자에게 퍼미션 허가를 받음.

        }

    }

    private void imageprocess_and_showResult() {

        imageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr());
        Log.d("getNa", String.valueOf(img_input.getNativeObjAddr()));

        Bitmap bitmapInput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_input, bitmapInput);

        Glide.with(getApplicationContext()).load(bitmapInput).into(imageVIewInput);

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
    }

    private void read_image_file(String test) {


        img_input = new Mat();
        img_output = new Mat();

        loadImage(test, img_input.getNativeObjAddr());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK)
        {


            try {
                //Uri에서 이미지 이름을 얻어온다.
                Log.d("notPath", String.valueOf(data.getData()));
                uri = (getPath(data.getData()));
                Log.d("getPath",uri);

                //이미지 데이터를 비트맵으로 받아온다.
                Bitmap image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ImageView image = (ImageView)findViewById(R.id.imageViewInput);

                //배치해놓은 ImageView에 set
                Glide.with(getApplicationContext()).load(image_bitmap).into(image);

                read_image_file(uri);



                //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();


            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }




*
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.


    public native void loadImage(String imageFileName, long img);
    public native void imageprocessing(long inputImage, long outputImage);
    public native void ConverRGBtoGray(long inputImage, long outputImage);
}
*/
