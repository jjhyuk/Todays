package com.example.jang.application1.login;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jang.application1.R;
import com.example.jang.application1.login.Util;

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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class SignupPage extends Activity {
    private static final String TAG = "CreateWallet";
    private EditText editTextId;
    private EditText editTextPw;
    private EditText editTextNick;
    private Spinner spinner;
     Button birthday;

    String Id_cnt;
    String upLoadServerUri = "http://13.125.115.186/uploadWalletKey.php";
    int serverResponseCode = 0;

    ArrayAdapter<CharSequence> sAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        editTextId = (EditText) findViewById(R.id.new_id);

        editTextPw = (EditText) findViewById(R.id.new_pw);
        editTextNick = findViewById(R.id.new_nick);
        editTextNick.setFilters(new InputFilter[]{filter});
        spinner = findViewById(R.id.gender_spinner);

        Date today = new Date();      // birthday 버튼의 초기화를 위해 date 객체와 SimpleDataFormat 사용
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String result = dateFormat.format(today);

        birthday = (Button) findViewById(R.id.birthday);
        birthday.setText(result);       // 오늘 날짜로 birthday 버튼 텍스트 초기화

        sAdapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(sAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




    }

    public int getAge(int birthYear, int birthMonth, int birthDay)
    {
        Calendar current = Calendar.getInstance();
        int currentYear  = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH) + 1;
        int currentDay   = current.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - birthYear;
        // 생일 안 지난 경우 -1
        if (birthMonth * 100 + birthDay > currentMonth * 100 + currentDay)
            age--;

        return age;
    }

    public void onBirthdayClicked (View v) {
        DialogFragment newFragment = new DatePickerFragment();   //DatePickerFragment 객체 생성
        newFragment.show(getFragmentManager(), "datePicker");                //프래그먼트 매니저를 이용하여 프래그먼트 보여주기
    }

    public void insert(View view) {
        String Id = editTextId.getText().toString();
        if(!Util.validateEmail(Id))
        {
            Toast.makeText(getApplicationContext(),"이메일형식을 지켜주세요",Toast.LENGTH_SHORT).show();
            return;
        }

        String Pw = editTextPw.getText().toString();
        if(!Util.validatePassword(Pw))
        {
            Toast.makeText(getApplicationContext(),"비밀번호를 영어와 특수문자포함 8자리 이상만들어주세요",Toast.LENGTH_SHORT).show();
            return;
        }
        String Nick = editTextNick.getText().toString();

        String gender = spinner.getSelectedItem().toString();
        String tmp = birthday.getText().toString();

        Log.d("birthday is",tmp);
        String[] YMD = tmp.split("-");
        Log.d("YMD",String.valueOf(YMD.length));

        int year = Integer.parseInt(YMD[0]);
        Log.d("year is ",String.valueOf(year));
        int month = Integer.parseInt(YMD[1]);
        Log.d("month is ",String.valueOf(month));
        int day = Integer.parseInt(YMD[2]);
        Log.d("day is ",String.valueOf(day));

        int age = getAge(year,month,day);
        Log.d("your age is ",String.valueOf(age));


        insertoToDatabase(Id, Pw,Nick,gender,String.valueOf(age));
    }
    private void insertoToDatabase(String Id, String Pw,String Nick,String gender, String age) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(SignupPage.this, "Please Wait", null, true, true);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equals("success")){
                Intent intent = new Intent();
                intent.putExtra("id",Id_cnt);
                setResult(1,intent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            createWallet("1234");
                        }
                    }).start();
                finish();
                }
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
            @Override
            protected String doInBackground(String... params) {

                try {
                    String Id = (String) params[0];
                    Id_cnt = Id;
                    String Pw = (String) params[1];
                    String Nick = (String)params[2];
                    String gender = params[3];
                    String age = params[4];

                    String link = "http://13.125.115.186/signUp.php";
                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                    data += "&" + URLEncoder.encode("Pw", "UTF-8") + "=" + URLEncoder.encode(Pw, "UTF-8");
                    data += "&" + URLEncoder.encode("Nick", "UTF-8") + "=" + URLEncoder.encode(Nick, "UTF-8");
                    data += "&" + URLEncoder.encode("gender", "UTF-8") + "=" + URLEncoder.encode(gender, "UTF-8");
                    data += "&" + URLEncoder.encode("age", "UTF-8") + "=" + URLEncoder.encode(age, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(Id, Pw,Nick,gender,age);


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
                conn.setRequestProperty("name",editTextNick.getText().toString());
                conn.setRequestProperty("walletAddr",walletAddr);

                conn.connect();

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + editTextNick.getText().toString());
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

            return serverResponseCode;

        } // End else block
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




}
