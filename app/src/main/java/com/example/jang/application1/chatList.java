package com.example.jang.application1;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class chatList extends AppCompatActivity {



    String mJsonString;
    private static final String TAG_JSON="webnautes";
    View view;
    TextView nick ;
    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HttpPost httppost;


    String selectedRoom;

    List<String> roomInfo = new ArrayList<>();

    //Thread t;

    ListView room;


    Handler handler = new Handler();

    String nickName;

    ImageButton chatRoomSearch;


    roomInfoAdapter roomInfoAdapter = new roomInfoAdapter();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);
        Intent get = getIntent();
        nickName = get.getStringExtra("name");
        GetData_body getData_body = new GetData_body();
        getData_body.execute();




        chatRoomSearch= findViewById(R.id.chatRoomSearch);
        chatRoomSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Freinds_search.class);
                intent.putExtra("name",nickName);

                startActivity(intent);
            }
        });
        room = findViewById(R.id.chatLIstView);
        room.setAdapter(roomInfoAdapter);








    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    //JSON파싱
    private class GetData_body extends AsyncTask<String, Void, String> {

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

                httppost = new HttpPost("http://13.125.115.186/getRoomInfo.php");

                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("name", nickName));



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

                String name = item.getString("users");
                String no = item.getString("no");



                hashMap = new HashMap<>();

                hashMap.put("name"+i, name);
                hashMap.put("no"+i, no);


                list.add(hashMap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomInfoAdapter.notifyDataSetChanged();

                         }
                });
            }
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }



    /*public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }*/



    public class roomInfoAdapter extends BaseAdapter
    {
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
                view = inflater.inflate(R.layout.roominfo_item, viewGroup, false);
            }

            final TextView list_item = view.findViewById(R.id.roomInfo);
            LinearLayout chatRoom = view.findViewById(R.id.chatRoomLinear);
            ImageView roominfo_image = view.findViewById(R.id.roominfo_image);
            Glide.with(view).applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).load("http://13.125.115.186/uploads/"
                    + list.get(i).get("name"+i)+".jpg").into(roominfo_image);
            list_item.setText(list.get(i).get("name"+i));


            chatRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),chatroom.class);
                    selectedRoom = list.get(i).get("no"+i);
                    intent.putExtra("roomName",selectedRoom);
                    intent.putExtra("my",nickName);
                    intent.putExtra("other",list.get(i).get("name"+i));

                    startActivity(intent);
                }
            });

            return view;
        }
    }



}
