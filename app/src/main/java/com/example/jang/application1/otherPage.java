package com.example.jang.application1;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jang.application1.Token.gift_wallet;

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
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class otherPage extends AppCompatActivity {

    TextView otherPageID;
    ImageView otherImage;
    Button otherChat;
    String nick;
    String name;
    Handler handler =new Handler();
    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> list = new ArrayList<>();
    apapter apapter = new apapter();
    GridView profileGrid ;
    TextView profilPostNum;

    Intent intent;
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    String Server = "http://13.125.115.186/";

    //소켓 입출력객체
    DataInputStream in;
    DataOutputStream out;
    Thread t;
    String roomName ;
    int tmp;
    String rst[];
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    //private String ip = "13.125.115.186";
    //private int port = 9999;
    String html;

    Button giftBtn;

    String data;
    SocketChannel socketChannel;
    private static final String HOST = "13.125.115.186";
    private static final int PORT = 5001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otherprofilepage);



        Intent get = getIntent();
        nick = get.getStringExtra("nick");//other nick
        Log.d("nick",nick);
        name = get.getStringExtra("name");//myself nick
        Log.d("name",name);
        otherPageID=findViewById(R.id.otherPageID);
        otherPageID.setText(nick);
        otherImage=findViewById(R.id.otherImage);
        otherChat=findViewById(R.id.otherChat);

        GetData task = new GetData();
        task.execute("http://13.125.115.186/profileImage.php");

        GetData_body getData_body = new GetData_body();
        getData_body.execute();

        profileGrid = findViewById(R.id.otherprofileGrid);
        profileGrid.setAdapter(apapter);

        profilPostNum = findViewById(R.id.otherPostNum);
        profilPostNum.setText(String.valueOf(apapter.getCount()));


        otherChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetData_chatNo getData_chatNo = new GetData_chatNo();
                getData_chatNo.execute();

            }
        });


        giftBtn = findViewById(R.id.giftBtn);
        giftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),gift_wallet.class);
                intent.putExtra("nick",nick);
                startActivity(intent);
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
                Server = "http://13.125.115.186/";
                Server = Server + result;
                Log.d("serverImage",Server);
                Glide.with(getApplicationContext()).load(Server).into(otherImage);

            }
        }


        @Override
        protected String doInBackground(String... params) {

            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/profileImage.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("username", nick));



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

                httppost = new HttpPost("http://13.125.115.186/profileBody.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("name", nick));



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

                String name = item.getString("name");
                String no = item.getString("no");
                String feed_image = item.getString("feed_image");


                hashMap = new HashMap<>();

                hashMap.put("name"+i, name);
                hashMap.put("no"+i, no);
                hashMap.put("feed_image"+i, feed_image);

                list.add(hashMap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        apapter.notifyDataSetChanged();
                        profilPostNum = findViewById(R.id.otherPostNum);
                        profilPostNum.setText(String.valueOf(apapter.getCount()));
                    }
                });
            }
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

    //JSON파싱
    private class GetData_chatNo extends AsyncTask<String, Void, String> {

        String errorString = null;
        ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(otherPage.this, "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loading.dismiss();



            Log.d("TAG", "response  - " + result);

            if (result == null){


            }
            else {

                roomName = result;






                rst = result.split(":");
                setNetty();
                roomName = rst[0];
                Log.d("roomName","RoomName: "+roomName);
                Log.d("other",nick);
                Log.d("me",name);
                intent = new Intent(getApplicationContext(),chatroom.class);
                intent.putExtra("roomName",roomName);
                intent.putExtra("other",nick);
                intent.putExtra("my",name);

                startActivity(intent);
            }
        }


        @Override
        protected String doInBackground(String... params) {

            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/selectChatNo.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("my", name));
                Log.d("Tag","my"+name);
                nameValuePairs.add(new BasicNameValuePair("other", nick));
                Log.d("Tag","other"+nick);
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



    public class apapter extends BaseAdapter {
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
            LayoutInflater inflater = null;
            if (view == null) {
                final Context context = viewGroup.getContext();
                if (inflater == null) {
                    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = inflater.inflate(R.layout.profilepage_item, viewGroup, false);
            }
            ImageView profileBodyImage = view.findViewById(R.id.profilefBodyImage);
            TextView profileBodyno = view.findViewById(R.id.profileBodyno);
            Log.d("List",list.get(i).get("feed_image"+i));
            Glide.with(view).load("http://13.125.115.186/"+list.get(i).get("feed_image"+i)).into(profileBodyImage);
            profileBodyno.setText(list.get(i).get("no"+i));


            return view;
        }
    }



    public void sendMsg(String msg){//서버에게 메시지 보내기
        try {
            out.write((msg+"\n").getBytes()  );
        }catch (IOException e) {
            e.printStackTrace();
        }
    }//sendMsg


/*
    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            if(rst[1].equals("false")) {
                dos.writeUTF("100:" + name.toString() + ":" + roomName);
                dos.writeUTF("100:" + nick.toString() + ":" + roomName);
            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNetty(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    Log.d("asd", "asd success");
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                if(rst[1].equals("false")) {
                    new SendmsgTask().execute("100:" + name.toString() + ":" + roomName);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new SendmsgTask().execute("100:" + nick.toString() + ":" + roomName);
                }
            }
        }).start();
    }


    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("EUC-KR")); // 서버로

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //binding.sendMsgEditText.setText("");
                }
            });
        }
    }


}
