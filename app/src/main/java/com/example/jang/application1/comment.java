package com.example.jang.application1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class comment extends AppCompatActivity{
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;

    String name;
    String no;

    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> commentlist = new ArrayList<>();

    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    String selectName ;
    String selectText;

    TextView commentText;
    ImageView commentImage;

    EditText commentEdit ;
    Button commentBtn;

    ListView commentListView ;
    adapter adapter = new adapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);

        Intent get = getIntent();
        name = get.getStringExtra("name");
        Log.d("name", "onCreate: "+name);
        no = get.getStringExtra("no");
        Log.d("no", "onCreate: "+no);
        commentText = findViewById(R.id.commentText);
        commentImage = findViewById(R.id.commentImage);
        commentEdit = findViewById(R.id.commentEdit);
        commentBtn = findViewById(R.id.commentSubmit);
        commentListView = findViewById(R.id.commentListview);







    }

    @Override
    protected void onResume() {
        super.onResume();
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetData_comment getData_comment = new GetData_comment();
                getData_comment.execute();
                Toast.makeText(getApplicationContext(),"댓글이 작성되었습니다",Toast.LENGTH_SHORT).show();
                commentEdit.setText("");
                onResume();

            }
        });



        GetData getData = new GetData();
        getData.execute();
        GetData_commentDB getData_commentDB = new GetData_commentDB();
        getData_commentDB.execute();
    }

    private class adapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return commentlist.size();
        }

        @Override
        public Object getItem(int i) {
            return commentlist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = null;
            if (view == null) {
                final Context context = viewGroup.getContext();
                if (inflater == null) {
                    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = inflater.inflate(R.layout.comment_item, viewGroup, false);
            }

            ImageView commentBodyImage = view.findViewById(R.id.commentBodyImage);
            TextView commentBodyName = view.findViewById(R.id.commentBodyName);
            TextView commentBodyText = view.findViewById(R.id.commentBodyText);

            Glide.with(view).load("http://13.125.115.186/"+commentlist.get(i).get("image"+i)).into(commentBodyImage);
            commentBodyName.setText(commentlist.get(i).get("user"+i));
            commentBodyText.setText(commentlist.get(i).get("text"+i));


            return view;
        }
    }




    //JSON파싱
    private class GetData_commentDB extends AsyncTask<String, Void, String> {

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

                mJsonString = result;
                showResult_commentDB();
            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/getcommentBody.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("no", no));



                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

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

    private void showResult_commentDB(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            commentlist = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);
                String image = item.getString("image");
                String id = item.getString("name");
                String text = item.getString("text");

                hashMap.put("image"+i,image);
                hashMap.put("user"+i,id);
                hashMap.put("text"+i,text);



                commentlist.add(hashMap);
            }

            commentListView.setAdapter(adapter);
            commentListView.setSelection(commentListView.getCount()-1);
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }


    //JSON파싱
    private class GetData_comment extends AsyncTask<String, Void, String> {

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

                mJsonString = result;

            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/commentUpload.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("no", no));

                nameValuePairs.add(new BasicNameValuePair("comment",  commentEdit.getText().toString()));
                Log.d("encoding", "doInBackground: "+commentEdit.getText().toString());
                nameValuePairs.add(new BasicNameValuePair("name", name));



                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"utf-8"));

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



    //JSON파싱
    private class GetData extends AsyncTask<String, Void, String> {

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

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/getcomment.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("no", no));



                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

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
            list = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);
                String id = item.getString("name");
                String text = item.getString("text");


                Log.d("encoding 2", "showResult: "+text);



                selectName = id;
                Log.d("selectName",selectName);
                selectText = text;

            }
            commentText.setText(selectText);
            GetData_Profile getData_profile = new GetData_Profile();
            getData_profile.execute();


        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }


    //JSON파싱
    private class GetData_Profile extends AsyncTask<String, Void, String> {

        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);



            Log.d("TAG", "ProfileResponse " + result);

            if (result == null){


            }
            else {

                mJsonString = result;
                Glide.with(getApplicationContext()).load("http://13.125.115.186/"+result).into(commentImage);
            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/profileImage.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("username", selectName));



                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

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

}
