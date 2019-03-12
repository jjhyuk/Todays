package com.example.jang.application1.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.example.jang.application1.Home;
import com.example.jang.application1.R;
import com.example.jang.application1.facebookSingUp;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class login extends AppCompatActivity {

    Button BtnSignIn, BtnSignUp;
    EditText inputID, inputPW;
    HttpPost httppost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;
    TextView tv;

    //facebook login
    private CallbackManager callbackManager;
    LoginButton facebook_login;
    boolean isLoggedIn;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("1","1");
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        AppEventsLogger.activateApp(this);
        setContentView(R.layout.login_activity);

        getHashKey();
        String token = FirebaseInstanceId.getInstance().getToken();



        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        facebook_login = findViewById(R.id.facebook_login);
        facebook_login.setReadPermissions("public_profile");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("TAG","onSucces LoginResult="+loginResult);
                        Log.d("Success1", String.valueOf(loginResult.getAccessToken().getToken()));
                        Log.d("Success2", String.valueOf(loginResult.getAccessToken().getUserId()));
                        final String[] email = new String[1];
                        final String[] name = new String[1];
                        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("result",object.toString());

                                try {
                                    email[0] = object.getString("email");       // 이메일
                                    name[0] = object.getString("name");         // 이름


                                    /*
                                    ImageView myImage = (ImageView)findViewById(R.id.facebookImage);

                                    URL url = new URL("https://graph.facebook.com/"+userId+"/picture");
                                    URLConnection conn = url.openConnection();
                                    conn.connect();
                                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                                    Bitmap bm = BitmapFactory.decodeStream(bis);
                                    bis.close();
                                    myImage.setImageBitmap(bm);
                                    */

                                    Log.d("TAG","페이스북 이메일 -> " + email[0]);
                                    Log.d("TAG","페이스북 이름 -> " + name[0]);

                                    Log.d("test",email[0]);
                                    Log.d("test2",name[0]);
                                    //페북 로그인 체크
                                    if(facebook_profile(email[0],name[0])==true)
                                    {
                                        Intent intent = new Intent(login.this,Home.class);
                                        intent.putExtra("id",email[0]);

                                        startActivity(intent);
                                        finish();
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(login.this,facebookSingUp.class);
                                        intent.putExtra("email",email[0]);
                                        intent.putExtra("name",name[0]);
                                        startActivity(intent);
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        graphRequest.setParameters(parameters);
                        graphRequest.executeAsync();


                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d("TAG","onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d("TAG","onError");
                    }
                });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        isLoggedIn = accessToken != null && !accessToken.isExpired();








        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        BtnSignUp = (Button)findViewById(R.id.btn_signup);
        BtnSignIn = (Button)findViewById(R.id.btn_signin);
        inputID = (EditText)findViewById(R.id.user_id);
        inputPW = (EditText)findViewById(R.id.user_pw);
        tv = (TextView)findViewById(R.id.textView2);



        BtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(login.this, "",
                        "Validating user...", true);
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        login_method();
                    }
                });
                t.start();
            }
        });




    }

    private boolean facebook_profile(String email, String name){
        try {

            httpclient = new DefaultHttpClient();

            httppost = new HttpPost("http://13.125.115.186/facebooklogin.php");
            httppost.addHeader("Cache-Control", "no-cache");
            nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("email", email));

            nameValuePairs.add(new BasicNameValuePair("name", name));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            response = httpclient.execute(httppost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            Log.d("responseHandler",responseHandler.toString());
            final String response = httpclient.execute(httppost, responseHandler);
            Log.d("response",response);
            System.out.println("Response : " + response);


            if (response.equalsIgnoreCase("Member")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(login.this, "Login Success", Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            } else {
                Toast.makeText(login.this, "Login Fail", Toast.LENGTH_SHORT).show();
                return false;
            }


        }
        catch(Exception e)
        {

            System.out.println("Exception : " + e.getMessage());
        }
        return false;

    }

    //포스트방식으로 값을 넘겨줌
    private void login_method() {

            try {

                httpclient = new DefaultHttpClient();

                httppost = new HttpPost("http://13.125.115.186/generalLogin.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("username", inputID.getText().toString()));

                nameValuePairs.add(new BasicNameValuePair("password", inputPW.getText().toString()));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d("responseHandler",responseHandler.toString());
                final String response = httpclient.execute(httppost, responseHandler);
                Log.d("response",response);
                System.out.println("Response : " + response);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("Response from PHP : " + response);
                        dialog.dismiss();
                    }
                });



                if (response.equalsIgnoreCase("User Found")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(login.this, "Login Success", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(login.this,Home.class);
                    intent.putExtra("id",inputID.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(login.this, "Login Fail", Toast.LENGTH_SHORT).show();
                }

            }
            catch(Exception e)
            {
                dialog.dismiss();
                System.out.println("Exception : " + e.getMessage());
            }


    }

    //회원가입 온클릭
    public void CliSignUp(View view)
    {
        Intent intent = new Intent(this, SignupPage.class);
        startActivityForResult(intent,1);
    }


    private void getHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("TAG", "key_hash=" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 1)
        {
            inputID.setText(data.getStringExtra("id").toString());
        }
    }






}
