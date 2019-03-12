package com.example.jang.application1;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.jang.application1.Horizontal.HorizontalListView;
import com.example.jang.application1.Posting.EditImageActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Home extends Activity{
    homePageFrag homePageFrag = new homePageFrag();
    ProfilPageFrag profilPageFrag = new ProfilPageFrag();
    checkVodFrag checkVodFrag = new checkVodFrag();

    //하단 버튼 목록
    ImageButton profileBtn ;
    ImageButton homeBtn;
    ImageButton addPostBtn;
    ImageButton homeSearchBtn;
    ImageButton vodBtn;





    //서버와의 연동
    HttpPost httppost;
    String mJsonString;
    String mJsonString_wallet;

    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;



    String idTv ;
    String nickTv;

    private static final String TAG_ID = "u_id";
    private static final String TAG_NICK = "u_nick";


    HorizontalListView horizontalListView ;
    HashMap<String,String> hashMap_Streaming = new HashMap<>();
    ArrayList<HashMap<String,String>> list_Streaming = new ArrayList<>();

    String mJsonString_Streaming;


    String walletName ;
    String walletAddr;
    String Master_Name = "UTC--2018-08-18T14-58-29.585--85105b3d8c89d819b730e9c884f1875c22f9e6bb.json";

    String File_Name = "확장자를 포함한 파일명";


    String fileURL = "http://13.125.115.186/wallet"; // URL
    String Save_Path;
    String Save_folder = "/key";

    DownloadThread dThread;
    Boolean downCheck = false; //false => nomal true => Master

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        Intent get = getIntent();
        idTv = get.getStringExtra("id");
        horizontalListView = findViewById(R.id.LiveStreaming);
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            Save_Path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + Save_folder;
        }








        GetData task = new GetData();
        task.execute("http://13.125.115.186/test.php");


        TedPermission.with(this).setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        }).setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WAKE_LOCK)
                .check();

        profileBtn = findViewById(R.id.myProfile);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                off();
                profileBtn.setImageResource(R.mipmap.profileon);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.homeFrame,profilPageFrag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                off();
                homeBtn.setImageResource(R.drawable.homeon_3);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.homeFrame,homePageFrag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        addPostBtn=findViewById(R.id.addPost);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),EditImageActivity.class);
                intent.putExtra("id",nickTv);
                startActivity(intent);
            }
        });

        homeSearchBtn = findViewById(R.id.homeSearchBtn);
        homeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Freinds_search.class);
                intent.putExtra("name",nickTv);
                startActivity(intent);
            }
        });


        vodBtn = findViewById(R.id.vodBtn);
        {
            vodBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    off();
                    vodBtn.setImageResource(R.drawable.vod_on);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.homeFrame,checkVodFrag);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }



    }

    //JSON파싱
    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            idTv = result;
            Log.d("TAG", "response  - " + result);

            if (result == null){

                idTv=errorString;
            }
            else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/nickPwCheck.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("username", idTv.toString()));



                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d("responseHandler",responseHandler.toString());
                final String response = httpclient.execute(httppost, responseHandler);
                Log.d("response",response);
                System.out.println("Response : " + response);




                return response;


            } catch (Exception e) {

                Log.d("TAG", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    //파싱한 결과값 출력
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);
                String nick = item.getString(TAG_NICK);


                HashMap<String,String> hashMap = new HashMap<>();

                hashMap.put(TAG_ID, id);
                hashMap.put(TAG_NICK, nick);

                idTv=hashMap.get(TAG_ID);
                nickTv=hashMap.get(TAG_NICK);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                homeBtn = findViewById(R.id.homeBtn);
                homeBtn.setImageResource(R.drawable.homeon_3);
                transaction.replace(R.id.homeFrame,homePageFrag);
                transaction.commit();
            }

            GetData_wallet getData_wallet = new GetData_wallet();
            getData_wallet.execute();
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

    //버튼 색상변경
    private void off()
    {
        profileBtn = findViewById(R.id.myProfile);
        profileBtn.setImageResource(R.drawable.profileoff_3);
        homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setImageResource(R.drawable.homeoff_3);
        vodBtn = findViewById(R.id.vodBtn);
        vodBtn.setImageResource(R.drawable.vod_off);
    }




    //JSON파싱
    private class GetData_wallet extends AsyncTask<String, Void, String> {

        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("TAG", "response  - " + result);

            if (result == null){

            }
            else {
                mJsonString_wallet = result;
                showResult_wallet();

            }
        }


        @Override
        protected String doInBackground(String... params) {


            try {
                httpclient = new DefaultHttpClient();
                httppost = new HttpPost("http://13.125.115.186/tokenKeycheck.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);
                Log.d("nickTv",nickTv);
                nameValuePairs.add(new BasicNameValuePair("name", nickTv));
                Log.d("idTV",idTv);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                response = httpclient.execute(httppost);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d("responseHandler",responseHandler.toString());
                final String response = httpclient.execute(httppost, responseHandler);
                Log.d("response",response);
                System.out.println("Response : " + response);

                return response;
            } catch (Exception e) {

                Log.d("TAG", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showResult_wallet(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString_wallet);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);


                walletName = item.getString("wallet");
                walletAddr = item.getString("walletAddr");



            }
            String[] tmp = walletName.split("/");
            walletName = tmp[1];


            File_Name = walletName;


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),walletName+"",Toast.LENGTH_SHORT).show();
                    Log.d("walletName",walletName);
                }
            });


            File dir = new File(Save_Path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            // 다운로드 폴더에 동일한 파일명이 존재하는지 확인해서
            // 없으면 다운받고 있으면 해당 파일 실행시킴.
            if (new File(Save_Path + "/" + File_Name).exists() == false) {

                dThread = new DownloadThread(fileURL + "/" + File_Name,
                        Save_Path + "/" + File_Name);
                dThread.start();
            } else {

                SharedPreferences sharedPreferences = getSharedPreferences("key",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("key",walletName);
                editor.commit();
            }

            if (new File(Save_Path + "/" + Master_Name).exists() == false) {
                DownloadThread masterKey = new DownloadThread(fileURL + "/" + Master_Name,
                        Save_Path + "/" + Master_Name);
                masterKey.start();
            } else {

                SharedPreferences sharedPreferences = getSharedPreferences("key",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("walletAddr",walletAddr);
                editor.commit();
            }
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }


    // 다운로드 쓰레드로 돌림..
    class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;

        DownloadThread(String serverPath, String localPath) {
            ServerUrl = serverPath;
            LocalPath = localPath;
        }

        @Override
        public void run() {
            URL imgurl;
            int Read;
            try {
                imgurl = new URL(ServerUrl);
                HttpURLConnection conn = (HttpURLConnection) imgurl
                        .openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(LocalPath);
                FileOutputStream fos = new FileOutputStream(file);
                for (;;) {
                    Read = is.read(tmpByte);
                    if (Read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, Read);
                }
                is.close();
                fos.close();
                conn.disconnect();

                if(downCheck == false) {
                    SharedPreferences sharedPreferences = getSharedPreferences("key", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("key", walletName);
                    editor.commit();
                }
                else
                {
                    downCheck = false;
                    SharedPreferences sharedPreferences = getSharedPreferences("key",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("walletAddr",walletAddr);
                    editor.commit();

                }

            } catch (MalformedURLException e) {
                Log.e("ERROR1", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR2", e.getMessage());
                e.printStackTrace();
            }
            mAfterDown.sendEmptyMessage(0);
        }
    }

    Handler mAfterDown = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            // 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
            //showDownloadFile();
        }

    };


}
