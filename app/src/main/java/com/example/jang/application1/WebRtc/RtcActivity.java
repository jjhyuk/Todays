package com.example.jang.application1.WebRtc;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jang.application1.R;

import org.json.JSONException;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import fr.pchab.webrtcclient.WebRtcClient;
import fr.pchab.webrtcclient.PeerConnectionParameters;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.List;

public class RtcActivity extends Activity implements WebRtcClient.RtcListener {
    private final static int VIDEO_CALL_SENT = 666;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private WebRtcClient client;
    private String mSocketAddress;
    private String callerId;


    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    //private String ip = "13.125.115.186";
    //private int port = 9999;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "192.168.219.105";
    private static final int PORT = 5001;

    TextView isConnect;

    String selectNum;
    String my ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                LayoutParams.FLAG_FULLSCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_DISMISS_KEYGUARD
                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.rtc);
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

            }
        }).start();

        mSocketAddress = "http://" + getResources().getString(R.string.host);
        mSocketAddress += (":" + getResources().getString(R.string.port) + "/");
        isConnect = (TextView)findViewById(R.id.isConnect);
        Intent get = getIntent();
        selectNum = get.getStringExtra("selectNum");
        my = get.getStringExtra("my");
        if(get.hasExtra("callerId")) {
            callerId = get.getStringExtra("callerId");
            Log.d("callerID", callerId);
            String[] test = callerId.split("/");
            for (int i = 0; i < test.length; i++) {
                Log.d("callerID" + i, test[i]);
            }
            callerId = test[3];
        }

        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                init();
            }
        });

        // local and remote render
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Log.d("tag","onCreateIF");
            Log.d("Tag","someone");
            final List<String> segments = intent.getData().getPathSegments();
            callerId = segments.get(0);
            Log.d("TAG",callerId);
        }
    }

    private void init() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);

        client = new WebRtcClient(this, mSocketAddress, params, VideoRendererGui.getEGLContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        vsv.onPause();
        if(client != null) {
            client.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        vsv.onResume();
        if(client != null) {
            client.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if(client != null) {
            client.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onCallReady(String callId) {
        Log.d("tag","onCallReady");
        if (callerId != null) {
            try {
                Log.d("callerID","answer = "+callId);
                answer(callerId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            call(callId);
        }
    }

    public void answer(String callerId) throws JSONException {
        Log.d("tag","answer");
        client.sendMessage(callerId, "init", null);
        startCam();
    }

    public void call(final String callId) {
        Log.d("tag","call");
        //Intent msg = new Intent(Intent.ACTION_SEND);
        //msg.putExtra(Intent.EXTRA_TEXT, mSocketAddress + callId);
        Log.d("CallID",callId);
        //msg.setType("text/plain");
        //startActivityForResult(Intent.createChooser(null, "Call someone :"), VIDEO_CALL_SENT);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //setSocket(ip,port);
                    dos.writeUTF("300:"+selectNum+":"+my+":"+"v"+mSocketAddress + callId);
                    Log.d("tcp Trans",mSocketAddress + callId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).run();*/



        new SendmsgTask().execute("300:"+selectNum+":"+my+":"+"v"+mSocketAddress + callId);
        startCam();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("tag","onActResult");
        if (requestCode == VIDEO_CALL_SENT) {
            startCam();
        }
    }

    public void startCam() {
        Log.d("tag","startCam");
        // Camera settings
        client.start(callerId);
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        if(newStatus.equals("CONNECTING"))
        {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isConnect.setVisibility(View.GONE);
                }
            });

        }
        if(newStatus.equals("DISCONNECTED"))
        {
            finish();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(remoteRender,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                scalingType);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);
    }

  /*  public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }*/

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed(); //지워야 실행됨

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("정말 종료하시겠습니까?");
        d.setPositiveButton("예", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // process전체 종료
                finish();
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();





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