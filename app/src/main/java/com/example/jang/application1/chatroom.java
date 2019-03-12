package com.example.jang.application1;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.jang.application1.WebRtc.RtcActivity;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;


public class chatroom extends AppCompatActivity {

    DataInputStream in;
    DataOutputStream out;

    private String html;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;



    String result;

    TextView title ;
    String name ;
    String selectedRoom ;

    List<String> text = new ArrayList<>();

    String upLoadServerUri = "http://13.125.115.186/chatImage.php";
    int serverResponseCode = 0;
    Button submint ;
    TextView chatText;
    ImageButton pick;

    Handler handler = new Handler();

    String my_name;
    String other_name;
    //String test;
    ListView list;
    listAdapter listAdapter = new listAdapter();

    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> arraylist = new ArrayList<>();
    Thread t;


    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HttpPost httppost;
    ImageButton videocall;




    //Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "13.125.115.186";
    private static final int PORT = 5001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlist);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        videocall = findViewById(R.id.videocall);
        list = findViewById(R.id.chatlistView);

        title = findViewById(R.id.title);
        Intent get = getIntent();
        selectedRoom = get.getStringExtra("roomName");
        Log.d("TAG","SelectRoom:"+selectedRoom);
        my_name = get.getStringExtra("my");
        other_name = get.getStringExtra("other");
        GetData_body getData_body = new GetData_body();
        getData_body.execute();

        //////////////네티 연결///////////
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
                checkUpdate.start();
            }
        }).start();

        //////////////////////////////////


        pick = findViewById(R.id.imagePick);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getApplicationContext())
                        .setImageProvider(new TedBottomPicker.ImageProvider() {
                            @Override
                            public void onProvideImage(ImageView imageView, Uri imageUri) {

                                Glide.with(getApplicationContext()).applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).load(imageUri).into(imageView);
                                Log.d("Log", "Uri Log : " + imageUri.toString());

                            }
                        })
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(final Uri uri) {
                                Log.d("uriTAG",uri.toString());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        uploadFile(uri.toString());
                                        /*try {
                                            //dos.writeUTF("300:"+selectedRoom+":"+my+":"+result);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/
                                    }
                                }).start();;



                            }
                        }).create();

                tedBottomPicker.show(getSupportFragmentManager());
            }
        });

        videocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RtcActivity.class);
                intent.putExtra("selectNum",selectedRoom);
                intent.putExtra("my",my_name);
                startActivity(intent);
            }
        });







        chatText= findViewById(R.id.chatText);
        submint= findViewById(R.id.submit);
        submint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(submint.getText().equals(""))
                {

                }
                else {
                    //dos.writeUTF("300:"+selectedRoom+":"+my+":"+chatText.getText().toString());
                    new SendmsgTask().execute("300:" + selectedRoom + ":" + my_name + ":" + chatText.getText().toString());
                    chatText.setText("");
                }
            }
        });

        title.setText(other_name);
        list.setAdapter(listAdapter);



    }

    @Override
    protected void onResume() {
        super.onResume();
        // Socket making thread
        /*t = new Thread(new Runnable() {
            public void run() {
                try {
                    setSocket(ip, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.w("ChattingStart", "Start Thread");

                while (true) {
                    try {
                        test = dis.readUTF();
                        String[] str = test.split(":");
                        Log.d("tag","text size : "+ str.length);
                        if(str[0].equals("300")&&str[1].equals(selectedRoom)) {
                            Log.d("size",String.valueOf(arraylist.size()));
                            int index = arraylist.size();



                            hashMap = new HashMap<>();

                            if(str.length>4)
                            {
                                hashMap.put("name" + index, str[2]);
                                Log.d("name",str[2]);
                                hashMap.put("no" + index, str[1]);
                                Log.d("no",str[1]);
                                String tmp = str[3]+str[4]+str[5];
                                hashMap.put("body"+index,tmp);
                                Log.d("body",tmp);
                            }
                            else {
                                hashMap.put("name" + index, str[2]);
                                Log.d("name",str[2]);
                                hashMap.put("no" + index, str[1]);
                                Log.d("no",str[1]);
                                hashMap.put("body" + index, str[3]);
                                Log.d("body",str[3]);
                            }


                            arraylist.add(hashMap);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listAdapter.notifyDataSetChanged();
                                    list.setSelection(listAdapter.getCount()-1);
                                }
                            });



                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
*/


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
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

                httppost = new HttpPost("http://13.125.115.186/getChatBody.php");

                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("roomNum", selectedRoom));



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
            arraylist = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String body = item.getString("body");
                String name = item.getString("user");
                String no = item.getString("no");



                hashMap = new HashMap<>();

                hashMap.put("name"+i, name);
                hashMap.put("no"+i, no);
                hashMap.put("body"+i,body);


                arraylist.add(hashMap);




                list.setAdapter(listAdapter);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(listAdapter);
                        list.setSelection(listAdapter.getCount()-1);
                    }
                });
            }
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }


    public int uploadFile(String sourceFileUri) {

        String[] fileName = sourceFileUri.split("://");

        Log.d("Filename",fileName[1]);
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName[1]);

        if (!sourceFile.isFile()) {
            Log.d("notFIle","notFILE");
            runOnUiThread(new Runnable() {
                public void run() {

                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                String name = my_name;
                String select = selectedRoom;

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName[1]);
                conn.setRequestProperty("name",name);
                conn.setRequestProperty("select",select);

                conn.connect();

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + name);
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"select\"\r\n\r\n" + select);


                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName[1] + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();


                String serverResponseMessage = conn.getResponseMessage();
                InputStream is = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                StringBuffer sb=new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                result = sb.toString();
                Log.d("test",result);
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                new SendmsgTask().execute("300:"+selectedRoom+":"+my_name+":"+result);



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        list.setAdapter(listAdapter);
                        list.setSelection(listAdapter.getCount()-1);

                    }
                });
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                           // Toast.makeText(getApplicationContext(), "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {


                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                       // Toast.makeText(getApplicationContext(), "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {


                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Got Exception ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("UploadfiletoserverExcep", "Exception : "
                        + e.getMessage(), e);
            }
            Log.d("ServerResponseCOde", String.valueOf(serverResponseCode));
            return serverResponseCode;

        } // End else block
    }


    public class listAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return arraylist.size();
        }

        @Override
        public Object getItem(int i) {
            return arraylist.get(i);
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
                view = inflater.inflate(R.layout.text_item, viewGroup, false);
            }

            TextView otherId = view.findViewById(R.id.otherID);
            TextView otherText= view.findViewById(R.id.otherText);
            ImageView otherImage= view.findViewById(R.id.otherImage);
            Button otherVideo = view.findViewById(R.id.otherVideo);
            LinearLayout other = view.findViewById(R.id.otherChat);
            ImageView otherChatImage = view.findViewById(R.id.otherChatImage);
            TextView chat_row = view.findViewById(R.id.Chat_row);

            TextView myText= view.findViewById(R.id.myText);
            ImageView myImage= view.findViewById(R.id.myImage);
            Button myVideo = view.findViewById(R.id.myVideo);
            LinearLayout myLinear = view.findViewById(R.id.myChat);


                Log.d("TEST1",arraylist.get(i).get("name" + i));
                Log.d("TEST2",arraylist.get(i).get("body" + i));
                Log.d("TEST3",arraylist.get(i).get("no" + i));
                Log.d("myID",my_name);
                Log.d("ListSize",String.valueOf(getCount()));

                otherVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),RtcActivity.class);
                        intent.putExtra("callerId",arraylist.get(i).get("body" + i).substring(1,arraylist.get(i).get("body" + i).length()));
                        startActivity(intent);
                    }
                });

                myVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), RtcActivity.class);
                        intent.putExtra("selectNum",selectedRoom);
                        intent.putExtra("my",my_name);
                        startActivity(intent);
                    }
                });

                if(arraylist.get(i).get("name" + i).equals(my_name)) {
                    other.setVisibility(View.GONE);
                    myLinear.setVisibility(View.VISIBLE);
                    String imageCheck = arraylist.get(i).get("body" + i);
                    if(imageCheck.length()>10) {
                        Log.d("image/", imageCheck.substring(0, 8));
                        if (imageCheck.substring(0, 8).equals("uploads/")) {
                            myText.setVisibility(View.GONE);
                            myImage.setVisibility(View.VISIBLE);
                            myVideo.setVisibility(View.GONE);
                            Glide.with(view).applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).load("http://13.125.115.186/" + arraylist.get(i).get("body" + i)).into(myImage);
                        } else {
                            Log.d("vhttp",imageCheck.substring(0,5));
                            if (imageCheck.substring(0, 5).equals("vhttp")) {
                                Log.d("split",imageCheck.substring(1,imageCheck.length()));
                                myVideo.setVisibility(View.VISIBLE);
                                myImage.setVisibility(View.GONE);
                                myText.setVisibility(View.GONE);
                            } else {
                                myVideo.setVisibility(View.GONE);
                                myImage.setVisibility(View.GONE);
                                myText.setVisibility(View.VISIBLE);
                                myText.setText(arraylist.get(i).get("body" + i));
                            }
                        }
                    }
                    else
                    {
                        myVideo.setVisibility(View.GONE);
                        myImage.setVisibility(View.GONE);
                        myText.setVisibility(View.VISIBLE);
                        myText.setText(arraylist.get(i).get("body"+i));
                    }
                } else {
                    int tmp = i + 1;


                        myLinear.setVisibility(View.GONE);
                        other.setVisibility(View.VISIBLE);
                        otherId.setText(arraylist.get(i).get("name" + i));
                        Glide.with(view).applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).load("http://13.125.115.186/uploads/" + other_name+".jpg").into(otherChatImage);
                        if (tmp!=arraylist.size()&&arraylist.get(i + 1).get("name" + tmp).equals(other_name)) {
                            Log.d("i+1 ",arraylist.get(i + 1).get("name" + tmp));
                            Log.d("i+1 ",String.valueOf(tmp));
                            otherId.setVisibility(View.GONE);
                            otherChatImage.setVisibility(View.GONE);
                            chat_row.setVisibility(View.VISIBLE);
                        }
                        String imageCheck = arraylist.get(i).get("body" + i);

                        if (imageCheck.length() > 10) {
                            Log.d("image/", imageCheck.substring(0, 8));
                            if (imageCheck.substring(0, 8).equals("uploads/")) {
                                otherVideo.setVisibility(View.GONE);
                                otherText.setVisibility(View.GONE);
                                otherImage.setVisibility(View.VISIBLE);
                                Glide.with(view).applyDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).load("http://13.125.115.186/" + arraylist.get(i).get("body" + i)).into(otherImage);
                            } else {
                                if (imageCheck.substring(0, 5).equals("vhttp")) {
                                    otherVideo.setVisibility(View.VISIBLE);
                                    otherImage.setVisibility(View.GONE);
                                    otherText.setVisibility(View.GONE);
                                } else {
                                    otherVideo.setVisibility(View.GONE);
                                    otherImage.setVisibility(View.GONE);
                                    otherText.setVisibility(View.VISIBLE);
                                    otherText.setText(arraylist.get(i).get("body" + i));
                                }
                            }
                        } else {
                            otherVideo.setVisibility(View.GONE);
                            otherImage.setVisibility(View.GONE);
                            otherText.setVisibility(View.VISIBLE);
                            otherText.setText(arraylist.get(i).get("body" + i));
                        }
                    }



            return view;
        }
    }








   /* public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }*/



    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                Log.d("checkUpdate","checkUpdate");
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private Runnable showUpdate = new Runnable() {

        public void run() {
            String receive = "Coming word : " + data;
            //binding.receiveMsgTv.setText(receive);

            list.setAdapter(listAdapter);
            list.setSelection(listAdapter.getCount()-1);

        }

    };

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();
                Log.d("receive", "msg :" + data);
                //////////나누기////
                String[] str = data.split(":");
                Log.d("tag","text size : "+ str.length);
                if(str[0].equals("300")&&str[1].equals(selectedRoom)) {
                    Log.d("size",String.valueOf(arraylist.size()));
                    int index = arraylist.size();



                    hashMap = new HashMap<>();

                    if(str.length>4)
                    {
                        hashMap.put("name" + index, str[2]);
                        Log.d("name",str[2]);
                        hashMap.put("no" + index, str[1]);
                        Log.d("no",str[1]);
                        String tmp = str[3]+str[4]+str[5];
                        hashMap.put("body"+index,tmp);
                        Log.d("body",tmp);
                    }
                    else {
                        hashMap.put("name" + index, str[2]);
                        Log.d("name",str[2]);
                        hashMap.put("no" + index, str[1]);
                        Log.d("no",str[1]);
                        hashMap.put("body" + index, str[3]);
                        Log.d("body",str[3]);
                    }


                    arraylist.add(hashMap);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.setAdapter(listAdapter);
                            list.setSelection(listAdapter.getCount()-1);
                        }
                    });



                }
                /////////////////////
                //text.add(data);
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
    }




    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
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
