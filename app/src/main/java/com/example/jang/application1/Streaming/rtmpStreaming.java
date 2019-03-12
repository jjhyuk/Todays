package com.example.jang.application1.Streaming;



import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.pedro.encoder.input.video.CameraOpenException;
import com.example.jang.application1.R;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class rtmpStreaming extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

    private RtmpCamera1 rtmpCamera1;
    private Button button;

    String etUrl ="rtmp://13.125.115.186/myapp/";
    String upLoadServerUri = "http://13.125.115.186/uploadVod.php";
    String name ;
    int serverResponseCode = 0;


    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HttpPost httppost;


    private String currentDateAndTime = "";
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/vod");


    String data;
    SocketChannel socketChannel;
    private static final String HOST = "13.125.115.186";
    private static final int PORT = 5001;
    Handler handler = new Handler();
    Handler handler_time = new Handler();
    Button send ;
    EditText chatText;



    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> arraylist = new ArrayList<>();

    adapter listAdapter = new adapter();
    ListView list ;

    TextView timer ;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;

    int Seconds, Minutes, MilliSeconds ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_example);


        timer = (TextView)findViewById(R.id.time_out);


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

        Intent get =getIntent();

        name = get.getStringExtra("name");
        etUrl = etUrl+name;
        Log.d("etUrl",etUrl);


        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);


        Button switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);


        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        surfaceView.getHolder().addCallback(this);

        list = findViewById(R.id.streamingList);
        list.setAdapter(listAdapter);

        chatText = findViewById(R.id.streamingChat);
        send = findViewById(R.id.streamingSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new SendmsgTask().execute("400:"+name+":"+name+":"+chatText.getText().toString());
                    chatText.setText("");

            }
        });
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Connection failed. " + reason, Toast.LENGTH_SHORT)
                        .show();
                rtmpCamera1.stopStream();
                button.setText(R.string.start_button);
            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_start_stop:
                if (!rtmpCamera1.isStreaming()) {
                    if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo() && !rtmpCamera1.isRecording()) {
                        button.setText(R.string.stop_button);
                        rtmpCamera1.startStream(etUrl);
                        GetData_body getData_body = new GetData_body();
                        getData_body.execute();

                        /////////Recording start///////////
                        try {
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                            currentDateAndTime = sdf.format(new Date());
                            if (!rtmpCamera1.isStreaming()) {
                                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                    handler_time.removeCallbacks(runnable);
                                    rtmpCamera1.startRecord(
                                            folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");

                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                rtmpCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                new SendmsgTask().execute("410:"+name+":"+currentDateAndTime);



                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            rtmpCamera1.stopRecord();

                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        ///////////////////////////////////


                    }
                    else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    button.setText(R.string.start_button);
                    rtmpCamera1.stopStream();
                    rtmpCamera1.stopRecord();

                    /////방송종료/////////////
                    GetData_body_off getData_body_off = new GetData_body_off();
                    getData_body_off.execute();


                    new SendmsgTask().execute("401:"+name);





                    AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this
                    // 여기서 부터는 알림창의 속성 설정
                    builder.setTitle("VOD 저장 확인")        // 제목 설정
                            .setMessage("방송 영상을 저장하시겠습니까?")        // 메세지 설정
                            .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                            .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton){
                                    Toast.makeText(getApplicationContext(),"file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                    Log.d("VOD", "onClick: "+Environment.getExternalStorageDirectory().getAbsolutePath()
                                            + "/vod/"+currentDateAndTime+".mp4");
                                    uploadFile(Environment.getExternalStorageDirectory().getAbsolutePath()
                                            + "/vod/"+currentDateAndTime+".mp4");

                                    Log.d("VOD2", "onClick: "+Environment.getExternalStorageDirectory().getAbsolutePath()
                                            + "/vod/"+currentDateAndTime+".mp4");

                                    currentDateAndTime = "";
                                    dialog.cancel();

                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                                // 취소 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton){
                                    File delete = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                            + "/vod/"+currentDateAndTime+".mp4");
                                    Log.d("delete",delete.getPath());
                                    delete.delete();
                                    new SendmsgTask().execute("402:"+currentDateAndTime);
                                    currentDateAndTime = "";
                                    dialog.cancel();
                                }
                            });

                    AlertDialog dialog = builder.create();    // 알림창 객체 생성
                    dialog.show();    // 알림창 띄우기

                     /*   Toast.makeText(this,
                        "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();


                    currentDateAndTime = "";*/
                }
                break;
            case R.id.switch_camera:
                try {
                    rtmpCamera1.switchCamera();
                } catch (CameraOpenException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
          /*  case R.id.b_record:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (!rtmpCamera1.isRecording()) {
                        try {
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                            currentDateAndTime = sdf.format(new Date());
                            if (!rtmpCamera1.isStreaming()) {
                                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                    rtmpCamera1.startRecord(
                                            folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                    bRecord.setText(R.string.stop_record);
                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                rtmpCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                bRecord.setText(R.string.stop_record);
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            rtmpCamera1.stopRecord();
                            bRecord.setText(R.string.start_record);
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rtmpCamera1.stopRecord();
                        bRecord.setText(R.string.start_record);
                        Toast.makeText(this,
                                "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        currentDateAndTime = "";
                    }
                } else {
                    Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                            Toast.LENGTH_SHORT).show();
                }
                break;*/
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();

            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
            button.setText(getResources().getString(R.string.start_button));
        }
        rtmpCamera1.stopPreview();
    }


    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            return 0;
        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);


                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                conn.setRequestProperty("name",name);

                conn.connect();

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + name);


                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

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
                Log.d("ResponseCode",String.valueOf(serverResponseCode));

                String serverResponseMessage = conn.getResponseMessage();
                InputStream is = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                StringBuffer sb=new StringBuffer();
                String result;
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                result = sb.toString();
                Log.d("test",result);
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                            Toast.makeText(getApplicationContext(), "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();

                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {


                ex.printStackTrace();


                        Toast.makeText(getApplicationContext(), "MalformedURLException",
                                Toast.LENGTH_SHORT).show();


                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {


                e.printStackTrace();


                        Toast.makeText(getApplicationContext(), "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();

                Log.e("UploadfiletoserverExcep", "Exception : "
                        + e.getMessage(), e);
            }

            return serverResponseCode;

        } // End else block
    }



    /////////////////////////////////방송 시작 종료
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


            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/streamingList.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("name", name));



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

    //JSON파싱
    private class GetData_body_off extends AsyncTask<String, Void, String> {

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


            }
        }


        @Override
        protected String doInBackground(String... params) {




            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/streamingoff.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("name", name));



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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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

            listAdapter.notifyDataSetChanged();
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
                if(str[0].equals("400")&&str[1].equals(name)) {
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



    public class adapter extends BaseAdapter
    {

        LayoutInflater inflater;

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
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                final Context context = viewGroup.getContext();
                if (inflater == null) {
                    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = inflater.inflate(R.layout.streaming_chat_item, viewGroup, false);
            }

            TextView user = view.findViewById(R.id.streamingUser);
            TextView chat = view.findViewById(R.id.streamingBody);

            Log.d("tag",arraylist.get(i).get("name"+i));
            Log.d("tag",arraylist.get(i).get("body"+i));

            user.setText(arraylist.get(i).get("name"+i));
            chat.setText(arraylist.get(i).get("body"+i));
            user.setTextColor(Color.WHITE);
            chat.setTextColor(Color.WHITE);

            if(arraylist.get(i).get("name"+i).equals(name))
            {
                user.setTextColor(Color.RED);
                chat.setTextColor(Color.RED);
            }




            return view;
        }
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            timer.setText("" + Minutes + "."
                    + String.format("%02d", Seconds)) ;


            handler_time.postDelayed(this, 0);
        }

    };
}