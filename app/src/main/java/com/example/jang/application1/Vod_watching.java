package com.example.jang.application1;

import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Vod_watching extends AppCompatActivity {
    private String URLstring = "http://13.125.115.186/";
    public  String VIDEO_URL ;
    public final static int URL = 1;

    TextView tvResult;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    VideoView videoView;
    String vod;

    Player player;
    //ArrayList<HashMap<String,ArrayList<Vod_watching_class>>> arraylist = new ArrayList<>();

    adapter listAdapter = new adapter();
    ListView list ;

    ArrayList<Vod_watching_class> chat_contents = new ArrayList<>();
    HashMap<String, ArrayList<Vod_watching_class>> time_hash = new HashMap<>();
    ArrayList<Vod_watching_class> arraylist = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_watching);

        GetData_body getData_body = new GetData_body();
        getData_body.execute();

        Intent get = getIntent();
        String vod_location = get.getStringExtra("vod");
        vod = vod_location.split("/")[1];
        Log.d("vod_name is " , vod);

        URLstring += get.getStringExtra("vod");
        Log.d("Test_see",URLstring);
        list = findViewById(R.id.vod_listview);
        list.setAdapter(listAdapter);

        VIDEO_URL = URLstring;
        tvResult = (TextView) findViewById(R.id.tv_result);
        videoView =  findViewById(R.id.videoView);


        //미디어컨트롤러 추가하는 부분
        MediaController controller = new MediaController(Vod_watching.this);
        videoView.setMediaController(controller);

        //비디오뷰 포커스를 요청함
        videoView.requestFocus();

        int type = URL;
        switch (type) {
            case URL:

                //동영상 경로가 URL일 경우
                videoView.setVideoURI(Uri.parse(VIDEO_URL));
                break;
        }





        /*//동영상 재생이 완료된 걸 알 수 있는 리스너
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생이 완료된 후 호출되는 메소드
                Toast.makeText(Vod_watching.this,
                        "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    //시작 버튼 onClick Method
    public void StartButton(View v) {
        playVideo();
    }

    //정지 버튼 onClick Method
    public void StopButton(View v) {
        stopVideo();
    }

    //동영상 재생 Method
    private void playVideo() {
        //비디오를 처음부터 재생할 때 0으로 시작(파라메터 sec)
        videoView.seekTo(0);
        videoView.start();
    }

    //동영상 정지 Method
    private void stopVideo() {
        //비디오 재생 잠시 멈춤
        videoView.pause();
        //비디오 재생 완전 멈춤
//        videoView.stopPlayback();
        //videoView를 null로 반환 시 동영상의 반복 재생이 불가능
//        videoView = null;
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

                httppost = new HttpPost("http://13.125.115.186/getvodchat.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("vod", vod));



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

            for(int i=0;i<jsonArray.length();i++) {
                chat_contents = new ArrayList<>();
                JSONObject item = jsonArray.getJSONObject(i);

                String no = item.getString("no");
                String user = item.getString("user");
                String vod_name = item.getString("vod_name");
                String chatting = item.getString("chatting");
                String time = item.getString("time");

                Log.e("chatting ",chatting);
                Log.e("time ",time);

                Vod_watching_class contents = new Vod_watching_class();
                contents.no = no;
                contents.user = user;
                contents.vod_name = vod_name;
                contents.chatting = chatting;


                chat_contents.add(contents);

                /*if (i < jsonArray.length() && i != 0) {
                    if (time_hash.containsKey(time)) {
                        time_hash.get(time).add(contents);
                    }
                } else {
                    time_hash.put(time, chat_contents);
                }*/
               time_hash.put(time, chat_contents);
            ;

            }
            /*handler.post(new Runnable() {
                @Override
                public void run() {
                    roomInfoAdapter.notifyDataSetChanged();

                }
            });*/
            //동영상이 재생준비가 완료되었을 때를 알 수 있는 리스너 (실제 웹에서 영상을 다운받아 출력할 때 많이 사용됨)
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playVideo();
                    new Thread() {
                        SimpleDateFormat timer= new SimpleDateFormat("s");
                        @Override
                        public void run() {
                            while(videoView.isPlaying()) {


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvResult.setText(timer.format(videoView.getCurrentPosition()));
                                        if(time_hash.containsKey(tvResult.getText().toString())) {
                                            Log.e("size : ", String.valueOf(time_hash.get(tvResult.getText().toString()).size()));
                                            Log.e("user : ", time_hash.get(tvResult.getText().toString()).get(0).user);
                                            Log.e("chatting : ", time_hash.get(tvResult.getText().toString()).get(0).chatting);
                                            for(int i =0;i < time_hash.get(tvResult.getText().toString()).size() ; i++)
                                            {
                                                Vod_watching_class chat = new Vod_watching_class();
                                                chat.user = time_hash.get(tvResult.getText().toString()).get(i).user;
                                                chat.chatting = time_hash.get(tvResult.getText().toString()).get(i).chatting;
                                                chat.no = time_hash.get(tvResult.getText().toString()).get(i).no;
                                                chat.vod_name = time_hash.get(tvResult.getText().toString()).get(i).vod_name;
                                                arraylist.add(chat);
                                                listAdapter.notifyDataSetChanged();
                                                list.setSelection(listAdapter.getCount()-1+
                                                1);

                                            }




                                        }
                                    }
                                });
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();


                }
            });
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
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

            //Log.d("tag",arraylist.get(i).get);
            //Log.d("tag",arraylist.get(i).get("body"+i));

            user.setText(arraylist.get(i).user);
            chat.setText(arraylist.get(i).chatting);
            user.setTextColor(Color.WHITE);
            chat.setTextColor(Color.WHITE);



            return view;
        }
    }



}
