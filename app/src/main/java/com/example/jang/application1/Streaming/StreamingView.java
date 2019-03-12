package com.example.jang.application1.Streaming;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jang.application1.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class StreamingView extends AppCompatActivity {
    private static  String URLstring = "rtmp://13.125.115.186/myapp/";
    String name ;
    String room;

    String data;
    SocketChannel socketChannel;
    private static final String HOST = "13.125.115.186";
    private static final int PORT = 5001;


    private DataInputStream dis;
    private DataOutputStream dos;
    private String ip = "13.125.115.186";
    private int port = 9999;
    String test;

    TextView chatText ;
    Button send;
    ListView list;

    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> arraylist = new ArrayList<>();
    Thread t;
    adapter listAdapter = new adapter();
    Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmp_see);


        //////////////네티 연결///////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    new SendmsgTask().execute("200:"+room+":"+name);
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();

        //////////////////////////////////

        Intent get = getIntent();
        name = get.getStringExtra("name");
        room = get.getStringExtra("room");

        chatText = findViewById(R.id.viewText);
        send =findViewById(R.id.viewSend);
        list = findViewById(R.id.viewList);
        list.setAdapter(listAdapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    //dos.writeUTF("400:"+room+":"+name+":"+chatText.getText().toString());
                    new SendmsgTask().execute("400:"+room+":"+name+":"+chatText.getText().toString());
                    chatText.setText("");

            }
        });

        URLstring += room;
        Log.d("Test_see",URLstring);
        //initiate Player
        //Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //Create the player
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        PlayerView playerView = findViewById(R.id.simple_player);
        playerView.setPlayer(player);

        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                .createMediaSource(Uri.parse(URLstring));

        // Prepare the player with the source.
        player.prepare(videoSource);
        //auto start playing
        player.setPlayWhenReady(true);
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
            user.setTextColor(Color.BLACK);
            chat.setTextColor(Color.BLACK);

            if(arraylist.get(i).get("name"+i).equals(name))
            {
                user.setTextColor(Color.RED);
                chat.setTextColor(Color.RED);
            }




            return view;
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
                if(str[0].equals("400")&&str[1].equals(room)) {
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
                else if(str[0].equals("401")&&str[1].equals(room)){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(StreamingView.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("방송이 종료되었습니다")
                                    .setMessage("나가시겠어요?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
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
