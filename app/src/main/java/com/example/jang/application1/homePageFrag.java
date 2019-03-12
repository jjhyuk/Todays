package com.example.jang.application1;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jang.application1.Horizontal.HorizontalListView;
import com.example.jang.application1.Streaming.StreamingView;
import com.example.jang.application1.Streaming.rtmpStreaming;

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

public class homePageFrag extends Fragment {
    View view;
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;

    private static final String TAG_ID = "name";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_FEEDIMAGE = "feed_image";
    private static final String TAG_TEXT = "text";
    private static final String TAG_NO = "no";

    String name ;

    String no=null;

    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    HashMap<String,ArrayList> hashLike = new HashMap<>();
    ArrayList<String> listLike = new ArrayList<>();

    ListView listview ;
    apapter apapter = new apapter();

    Handler handler = new Handler();

    TextView refersh;
    ImageButton homeChatBtn;

    ImageButton broadCastBtn;

    ImageButton testBtn;
    int index ;


    HorizontalListView horizontalListView ;
    HashMap<String,String> hashMap_Streaming = new HashMap<>();
    ArrayList<HashMap<String,String>> list_Streaming = new ArrayList<>();
    CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter();
    String mJsonString_Streaming;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.homepage,container,false);
        name = (((Home) getActivity())).nickTv;

        listview = view.findViewById(R.id.feedListview);
        horizontalListView = view.findViewById(R.id.LiveStreaming);
        horizontalListView.setVisibility(View.GONE);




        GetData_streaming getData_streaming = new GetData_streaming();
        getData_streaming.execute();
        GetData_likeDetail getData_likeDetail = new GetData_likeDetail();
        getData_likeDetail.execute();

        hashMap = new HashMap<>();
        GetData task = new GetData();
        task.execute("http://13.125.115.186/timeline.php");
        refersh = view.findViewById(R.id.refresh);
        refersh.setText((((Home) getActivity())).nickTv);
        refersh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        homeChatBtn = view.findViewById(R.id.homeChatBtn);
        homeChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),chatList.class);
                intent.putExtra("name",((Home) getActivity()).nickTv);
                startActivity(intent);
            }
        });

        /*testBtn = view.findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), rtmpStreaming.class);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });*/

        broadCastBtn= view.findViewById(R.id.broadCast);
        broadCastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), rtmpStreaming.class);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });


        listview.setAdapter(apapter);

        return view;
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

            String serverURL = params[0];


            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/timeline.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("username", name));



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
                String id = item.getString(TAG_ID);
                Log.d("id",id);
                String image = item.getString(TAG_IMAGE);
                String text = item.getString(TAG_TEXT);
                String no = item.getString(TAG_NO);
                String feed_image = item.getString(TAG_FEEDIMAGE);


                hashMap = new HashMap<>();

                hashMap.put(TAG_ID+i, id);
                hashMap.put(TAG_IMAGE+i, image);
                hashMap.put(TAG_TEXT+i, text);
                hashMap.put(TAG_NO+i, no);
                hashMap.put(TAG_FEEDIMAGE+i, feed_image);

                list.add(hashMap);

            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    apapter.notifyDataSetChanged();
                }
            });


        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

    public class apapter extends BaseAdapter{

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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = null;
            if (view == null) {
                final Context context = viewGroup.getContext();
                if (inflater == null) {
                    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = inflater.inflate(R.layout.homepage_listview, viewGroup, false);
            }

            ImageView userImage = view.findViewById(R.id.feedUserImage);
            TextView userName = view.findViewById(R.id.feedName);
            TextView userName2 = view.findViewById(R.id.feedName2);
            ImageView feedImage = view.findViewById(R.id.feedImage);
            TextView feedText = view.findViewById(R.id.feedText);
            final TextView likeNum = view.findViewById(R.id.likeNum_list);
            final ImageButton like = view.findViewById(R.id.likeBtn);


            TextView writeComment = view.findViewById(R.id.writeComment);

            userName.setText(list.get(i).get(TAG_ID+i));
            userName2.setText(list.get(i).get(TAG_ID+i));
            Glide.with(view).load("http://13.125.115.186/"+list.get(i).get(TAG_IMAGE+i)).into(feedImage);
            feedText.setText(list.get(i).get(TAG_TEXT+i));
            Glide.with(view).load("http://13.125.115.186/"+list.get(i).get(TAG_FEEDIMAGE+i)).into(userImage);

            no = list.get(i).get(TAG_NO+i);

            Log.d("nono",hashLike.toString() + " = " + no);
            final Boolean isCheck[] = {false};


            if(hashLike.containsKey(no))
            {
                likeNum.setText(String.valueOf(hashLike.get(no).size()));
                if(hashLike.get(no).contains(name)) {

                    like.setImageResource(R.mipmap.like);
                    isCheck[0] = true;
                    Log.d("noChange",no);

                }
                else
                {
                    like.setImageResource(R.mipmap.likeoff);
                    isCheck[0] = false;
                }
            }
            else
            {
                like.setImageResource(R.mipmap.likeoff);
                isCheck[0] = false;
            }


            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("!@#!#", String.valueOf(isCheck[0]));
                    if(isCheck[0] == false)
                    {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                like.setImageResource(R.mipmap.like);
                            }
                        });

                            int tmp = Integer.parseInt(likeNum.getText().toString());
                            likeNum.setText(String.valueOf(tmp+1));
                            Log.d("set","like=="+no);
                             isCheck[0] =true;
                            index = i;
                             no = list.get(index).get(TAG_NO+index);
                            GetData_like getData_like = new GetData_like();
                            getData_like.execute();




                    }
                    else {
                            Log.d("in","in3");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    like.setImageResource(R.mipmap.likeoff);
                                }
                            });
                            int tmp = Integer.parseInt(likeNum.getText().toString());
                            likeNum.setText(String.valueOf(tmp-1));
                            index = i;
                            no = list.get(index).get(TAG_NO+index);
                            GetData_delete getData_delete = new GetData_delete();
                            getData_delete.execute();
                            Log.d("set","likeoff=="+no);
                            isCheck[0] = false;
                        }
                }
            });
            Log.d("Resource", String.valueOf(like.getResources()));


            writeComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    index = i;
                    Intent intent = new Intent(getActivity(),comment.class);
                    intent.putExtra("no",list.get(index).get(TAG_NO+index));
                    Log.d("Tag NO index", "onClick: "+list.get(index).get(TAG_NO+index));
                    intent.putExtra("name",name);
                    Log.d("name ", "onClick: "+name);
                    startActivity(intent);
                }
            });


            return view;
        }
    }

    //JSON파싱
    private class GetData_delete extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);



            Log.d("TAG", "response  - " + result);


        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/deleteLike.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("username", refersh.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("no", no));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d("responseHandler",responseHandler.toString());




                return String.valueOf(response);


            } catch (Exception e) {

                Log.d("TAG", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showResult_like(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            listLike = new ArrayList<>();
            hashLike= new HashMap<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);
                String no = item.getString("no");

                String id = item.getString("user_id");


                listLike = new ArrayList<>();

                if(hashLike.containsKey(no))
                {
                    listLike.addAll(hashLike.get(no));
                }

                listLike.add(id);
                hashLike.put(no,listLike);
                Log.d("test!!",no + "="+hashLike.get(no).get(0).toString());

            }
            listview.setAdapter(apapter);
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }


    //JSON파싱
    private class GetData_likeDetail extends AsyncTask<String, Void, String> {

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
                showResult_like();
            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/getLike.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);





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

    //JSON파싱
    private class GetData_like extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);



            Log.d("TAG", "response  - " + result);


        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/postLike.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("username", refersh.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("no", no));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d("responseHandler",responseHandler.toString());





                return String.valueOf(response);


            } catch (Exception e) {

                Log.d("TAG", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void refresh(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                apapter.notifyDataSetChanged();
                customArrayAdapter.notifyDataSetChanged();
            }
        });

        listview.setAdapter(apapter);
        horizontalListView.setAdapter(customArrayAdapter);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();


    }

    @Override
    public void onPause() {
        super.onPause();

    }


    //JSON파싱
    private class GetData_streaming extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
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

                mJsonString_Streaming = result;
                showResult_streaming();
            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/checkLive.php");
                httppost.addHeader("Cache-Control", "no-cache");
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
    private void showResult_streaming(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString_Streaming);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            list_Streaming = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String name = item.getString("name");
                String image = item.getString("image");


                hashMap_Streaming = new HashMap<>();

                hashMap_Streaming.put("name"+i, name);

                hashMap_Streaming.put("image"+i, image);

                list_Streaming.add(hashMap_Streaming);
            }



            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    horizontalListView.setAdapter(customArrayAdapter);
                    customArrayAdapter.notifyDataSetChanged();
                    if(list_Streaming.isEmpty())
                    {
                        horizontalListView.setVisibility(View.GONE);
                    }
                    else {
                        horizontalListView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

    public class CustomArrayAdapter extends BaseAdapter {
        LayoutInflater inflater;

        @Override
        public int getCount() {
            return list_Streaming.size();
        }

        @Override
        public Object getItem(int i) {
            return list_Streaming.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                final Context context = viewGroup.getContext();
                if (inflater == null) {
                    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = inflater.inflate(R.layout.home_live_streaming, viewGroup, false);
            }
            TextView name_text = view.findViewById(R.id.home_streaming_name);
            ImageView image = view.findViewById(R.id.home_streaming_image);
            name_text.setText(list_Streaming.get(i).get("name" + i));
            Glide.with(view).load("http://13.125.115.186/" + list_Streaming.get(i).get("image" + i)).into(image);

            LinearLayout home_live_select = view.findViewById(R.id.home_live_select);
            home_live_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), StreamingView.class);
                    intent.putExtra("name", name);
                    intent.putExtra("room", list_Streaming.get(i).get("name" + i));
                    startActivity(intent);
                }
            });

            return view;
        }
    }


}
