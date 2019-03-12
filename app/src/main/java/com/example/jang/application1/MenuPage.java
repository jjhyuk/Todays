package com.example.jang.application1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jang.application1.login.login;
import com.facebook.login.LoginManager;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuPage extends Activity {


    EditText inputID, inputPW;
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;

    TextView idTv;
    TextView nickTv;

    private static final String TAG_ID = "u_id";
    private static final String TAG_NICK = "u_nick";




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =getIntent();

        setContentView(R.layout.menu);


        idTv = findViewById(R.id.idText);
        idTv.setText(intent.getStringExtra("id"));
        nickTv = findViewById(R.id.nickText);



        GetData task = new GetData();
        task.execute("http://13.125.115.186/nickPwCheck.php");




    }

    //JSON파싱
    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MenuPage.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            idTv.setText(result);
            Log.d("TAG", "response  - " + result);

            if (result == null){

                idTv.setText(errorString);
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

                nameValuePairs.add(new BasicNameValuePair("username", idTv.getText().toString()));



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

                idTv.setText(hashMap.get(TAG_ID));
                nickTv.setText(hashMap.get(TAG_NICK));

            }



        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }



 //로그아웃버튼 원클릭메소드
    public void logout(View view)
    {
        Intent intent = new Intent(this, login.class);
        LoginManager.getInstance().logOut();
        finish();



        startActivityForResult(intent,1);
    }

}
