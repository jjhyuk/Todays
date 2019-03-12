package com.example.jang.application1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class facebookSingUp extends Activity {

    EditText nick ;
    Button nickBtn;

    HttpPost httppost;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;


    String email;
    String name;

    private static final String TAG = "CreateWallet";
    String upLoadServerUri = "http://13.125.115.186/uploadWalletKey.php";
    int serverResponseCode = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snslogin);

        Intent get = getIntent();

        email = get.getStringExtra("email");
        Log.d("email",email);
        name = get.getStringExtra("name");
        Log.d("name",name);
        nick = findViewById(R.id.snsNick);
        nick.setFilters(new InputFilter[]{filter});
        nickBtn = findViewById(R.id.snsNickBtn);
        nick.setText(name);


    }

    protected InputFilter filter= new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end,

                                   Spanned dest, int dstart, int dend) {



            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");

            if (!ps.matcher(source).matches()) {

                return "";

            }

            return null;

        }

    };





    private void phpPost() {

        try {

            httpclient = new DefaultHttpClient();

            httppost = new HttpPost("http://13.125.115.186/facebookSign.php");

            nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("email", email));

            nameValuePairs.add(new BasicNameValuePair("name", nick.getText().toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));



            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            Log.d("responseHandler",responseHandler.toString());
            final String response = httpclient.execute(httppost, responseHandler);
            Log.d("response",response);
            System.out.println("Response : " + response);




            if (response.equalsIgnoreCase("success")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(facebookSingUp.this, "Login Success", Toast.LENGTH_SHORT).show();
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createWallet("1234");
                    }
                }).start();




            } else {
                Toast.makeText(facebookSingUp.this, "Login Fail", Toast.LENGTH_SHORT).show();
            }

        }
        catch(Exception e)
        {

            System.out.println("Exception : " + e.getMessage());
        }


    }

    public void snsinsert(View view){
        phpPost();
    }



    public String[] createWallet(final String password) {
        String[] result = new String[2];
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //다운로드 path 가져오기
            String path_Absol = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d("Path", path.toString());
            if (!path.exists()) {
                path.mkdir();
            }
            String fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path))); //지갑생성
            result[0] = path + "/" + fileName;
            Log.d(TAG, "createWallet: Path"+path);
            Log.d(TAG, "createWallet: fileName"+fileName);
            Log.d(TAG, "createWalletResult[0]: " + result[0]);

            Credentials credentials = WalletUtils.loadCredentials(password, result[0]);

            result[1] = credentials.getAddress();
            Log.d(TAG, "createWalletResult[]: " + result[1]);

            String rst = path_Absol + "/Download/"+fileName;
            Log.d(TAG, "createWallet: rstPath : "+rst);
            uploadFile(rst,result[1]);
            return result;
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | IOException
                | CipherException e) {
            e.printStackTrace();
            return null;
        }
    }
    public int uploadFile(String sourceFileUri,String walletAddr) {

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
                conn.setRequestProperty("name",nick.getText().toString());
                conn.setRequestProperty("walletAddr",walletAddr);

                conn.connect();

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + nick.getText().toString());
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"walletAddr\"\r\n\r\n" + walletAddr);


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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getApplicationContext(), "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {


                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("UploadfiletoserverExcep", "Exception : "
                        + e.getMessage(), e);
            }

            Intent intent = new Intent(getApplicationContext(),Home.class);
            intent.putExtra("id",email);
            startActivity(intent);
            return serverResponseCode;

        } // End else block
    }
}
