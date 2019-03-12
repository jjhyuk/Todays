package com.example.jang.application1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

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


public class Freinds_search extends Activity {
    View view;
    EditText search ;
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    String filterText;
    ListView freindsSearchList;
    searchListAdapter searchListAdapter = new searchListAdapter();
    boolean adapterOK = false;


    Button followBtn;
    LinearLayout searchLinear;

    GetData task = new GetData();

    String name;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_search);

        freindsSearchList = findViewById(R.id.freindsSearchList);
        freindsSearchList.setAdapter(searchListAdapter);

        Intent get = getIntent();
        name = get.getStringExtra("name");







        search = findViewById(R.id.freindsSearchText);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                list = new ArrayList<>();

                filterText = editable.toString() ;
                Log.d("Test", filterText);
                if(filterText.length()>0) {
                    if (task.getStatus() == AsyncTask.Status.RUNNING)
                    {
                        task.cancel(true);
                    }

                    task = new GetData();
                    task.execute();
                }
            }
        });




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
                httppost = new HttpPost("http://13.125.115.186/freindsearch.php");
                    httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair("search", filterText));
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
            list = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String nick = item.getString("u_nick");
                String profile_image = item.getString("image");


                hashMap = new HashMap<>();

                hashMap.put("u_nick"+i, nick);

                hashMap.put("image"+i, profile_image);

                list.add(hashMap);


            }
            searchListAdapter.notifyDataSetChanged();
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }



    public class searchListAdapter extends BaseAdapter{
        LayoutInflater inflater;
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    if (view == null) {
                        final Context context = viewGroup.getContext();
                        if (inflater == null) {
                            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        }
                view = inflater.inflate(R.layout.freinds_list_item, viewGroup, false);
            }


            final TextView userID = view.findViewById(R.id.searchString);
            userID.setText(list.get(i).get("u_nick"+i));
            ImageView searchImage = view.findViewById(R.id.searchImage);
            Glide.with(view).load("http://13.125.115.186/"+list.get(i).get("image"+i)).into(searchImage);

            searchLinear = view.findViewById(R.id.searchLinear);
            searchLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent visit = new Intent(getApplicationContext(),otherPage.class);
                    visit.putExtra("nick",userID.getText().toString());
                    visit.putExtra("name",name);
                    startActivity(visit);
                    finish();
                }
            });

            return view;
        }
        }
    }




