package com.example.jang.application1.Server;
import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;

        import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.Uri;

public class ClientSocketService extends Service {

         	private static final int MY_NOTIFICATION_ID=1;
 	private NotificationManager notificationManager;
 	private Notification myNotification;
 	private final String myBlog = "http://micropilot.tistory.com/";

         	@Override
 	public void onCreate() {
         		super.onCreate();
         	}

         	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {

         		Log.i("서비스호출", "onStartCommand()실행됨");
         		Socket socket = null;
         		try{
             			socket = new Socket("13.209.17.56", 9999);
             			socket.setSoTimeout(10000);
             			DataInputStream din = new DataInputStream(socket.getInputStream());
             			int n = din.read();
             			if(n!=1) {
                 				return super.onStartCommand(intent, flags, startId);
                 			}
             		}catch(Exception e) {
             			Log.e("소켓접속상태", e.getMessage());
             			if(socket!=null)
                 				try {
                 					socket.close();
                 				} catch (IOException e1) {
                 				}
             			return super.onStartCommand(intent, flags, startId);
             		}
         /*		// Send Notification
         		notificationManager =
                 		 (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         		myNotification = new Notification(R.drawable.ic_launcher_foreground,
                 		  "Notification!",
                		  System.currentTimeMillis());
         		Context context = getApplicationContext();
         		String notificationTitle = "새로운 글 등록알림";
         		String notificationText = "http://micropilot.tistory.com 접속";
         		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myBlog));
         		@SuppressLint("WrongConstant") PendingIntent pendingIntent
         		  = PendingIntent.getActivity(getBaseContext(),
                 		    0, myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
         		myNotification.defaults |= Notification.DEFAULT_SOUND;
         		myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
         		myNotification.setLatestEventInfo(context,
                 		   notificationTitle,
                 		   notificationText,
                 		   pendingIntent);
         		notificationManager.notify(MY_NOTIFICATION_ID, myNotification);*/

         		return super.onStartCommand(intent, flags, startId);
         	}

         	@Override
 	public void onDestroy() {
         		super.onDestroy();
         	}

         	@Override
 	public IBinder onBind(Intent arg0) {
         		return null;
         	}
 }


