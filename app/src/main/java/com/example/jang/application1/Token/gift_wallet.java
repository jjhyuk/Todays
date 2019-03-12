package com.example.jang.application1.Token;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jang.application1.Home;
import com.example.jang.application1.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Markelov
 * Telegram group: https://t.me/joinchat/D62dXAwO6kkm8hjlJTR9VA
 *
 * Если есть вопросы, отвечу в телеграме
 * If you have any questions, I will answer the telegram
 *
 *    Russian:
 *    Пример включает следующие функции:
 *       - Получаем адрес кошелька
 *       - Получаем баланс Eth
 *       - Получаем баланс Токена
 *       - Получаем название Токена
 *       - Получаем символ Токена
 *       - Получаем адрес Контракта Токена
 *       - Получаем общее количество выпущеных Токенов
 *
 *
 *   English:
 *   The example includes the following functions:
 *       - Get address wallet
 *       - Get balance Eth
 *       - Get balance Token
 *       - Get Name Token
 *       - Get Symbol Token
 *       - Get contract Token address
 *       - Get supply Token
 *
 */

public class gift_wallet extends AppCompatActivity {

    //서버와의 연동
    HttpPost httppost;
    String mJsonString_wallet;


    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    String walletName;

    String TAG = "Main";
    WalletCreate wc = new WalletCreate();

    String url = "https://rinkeby.infura.io/86ce59c5eb0e4ecdb3c581ba3245a644";

    Web3j web3 = Web3jFactory.build(new HttpService(url));

    String smartcontract = "0xd5EE485636906Ee1Cc234db489c27a0FFf3BDfEc";
    String passwordwallet = "1234";

    File DataDir;

    TextView ethaddress, ethbalance, tokenname, tokensymbol, tokensupply, tokenaddress, tokenbalance, tokensymbolbalance,sendtoaddress,sendtonick;
    //TextView tv_gas_limit, tv_gas_price, tv_fee;
    TextView tv_fee;
    EditText sendtokenvalue, sendethervalue;


    ImageView qr_small, qr_big;

    BigInteger GasPrice, GasLimit;

    final Context context = this;

    IntentIntegrator qrScan;

    String Key;
    String otherNick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gift_wallet);

        SharedPreferences sharedPreferences = getSharedPreferences("key",MODE_PRIVATE);
        Key = sharedPreferences.getString("key","에러");
        Log.d("Key Value : ",Key);


        Intent get = getIntent();
        otherNick = get.getStringExtra("nick");
        Log.d("otherNick : ",otherNick);

        GetData_wallet getData_wallet = new GetData_wallet();
        getData_wallet.execute();

        ethaddress = (TextView) findViewById(R.id.ethaddress); // Your Ether Address
        ethbalance = (TextView) findViewById(R.id.ethbalance); // Your Ether Balance

        tokenname = (TextView) findViewById(R.id.tokenname); // Token Name
        tokensymbol = (TextView) findViewById(R.id.tokensymbol); // Token Symbol
        tokensupply = (TextView) findViewById(R.id.tokensupply); // Token Supply
        tokenaddress = (TextView) findViewById(R.id.tokenaddress); // Token Address
        tokenbalance = (TextView) findViewById(R.id.tokenbalance); // Token Balance
        tokensymbolbalance = (TextView) findViewById(R.id.tokensymbolbalance);

        sendtoaddress = (TextView) findViewById(R.id.sendtoaddress); // Address for sending ether or token
        sendtonick = findViewById(R.id.sendtonick);

        sendtokenvalue = (EditText) findViewById(R.id.SendTokenValue); // Ammount token for sending
        sendethervalue = (EditText) findViewById(R.id.SendEthValue); // Ammount ether for sending

        qr_small = (ImageView)findViewById(R.id.qr_small);

        qrScan = new IntentIntegrator(this);

        //tv_gas_limit = (TextView) findViewById(R.id.tv_gas_limit);
        //tv_gas_price = (TextView) findViewById(R.id.tv_gas_price);
        tv_fee = (TextView) findViewById(R.id.tv_fee);

        //final SeekBar sb_gas_limit = (SeekBar) findViewById(R.id.sb_gas_limit);
        //sb_gas_limit.setOnSeekBarChangeListener(seekBarChangeListenerGL);
        //final SeekBar sb_gas_price = (SeekBar) findViewById(R.id.sb_gas_price);
        //sb_gas_price.setOnSeekBarChangeListener(seekBarChangeListenerGP);

        GetFee();

        /**
         * Получаем полный путь к каталогу с ключами
         * Get the full path to the directory with the keys
         */
        //DataDir = this.getExternalFilesDir("/keys/");
        DataDir = Environment.getExternalStoragePublicDirectory("/key");
        Log.d(TAG, "DataDir + "+DataDir.toString());
        File KeyDir = new File(this.DataDir.getAbsolutePath());
        Log.d(TAG, "KeyDir + "+KeyDir.toString());
        /**
         * Проверяем есть ли кошельки
         * Check whether there are purses
         */
        File[] listfiles = KeyDir.listFiles();
        if (listfiles.length == 0 ) {
            /**
             * Если в директории файла кошелька, добавляем кошелек
             * If the directory file of the wallet, add the wallet
             */
            try {
                String fileName = WalletUtils.generateNewWalletFile(passwordwallet, DataDir, false);

                System.out.println("FileName: " + DataDir.toString() + fileName);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            /**
             * Если кошелек создан, начинаем выполнение потока
             * If the wallet is created, start the thread
             */
            wc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    ///////////////////// QR Generation //////////////////////
    /**
     * QR генерация Ether Адреса
     * QR Generation Ether Address
     */
    public Bitmap QRGen(String Value, int Width, int Heigth) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(Value, BarcodeFormat.DATA_MATRIX.QR_CODE, Width, Heigth);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    ////////////////// END QR Generation ////////////////////

    ///////////////////// QR SCAN ///////////////////////////
    /**
     * QR сканирование Ether Адреса
     * QR scan Ether Address
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                sendtoaddress.setText(result.getContents());
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //////////////////// END QR SCAN ////////////////////////

    /////////////////// SeekBar Listener ////////////////////
    /**
     * SeekBar Слушатель
     * SeekBar Listener
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGL = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasLimit(String.valueOf(seekBar.getProgress()*1000+1000000));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGP = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasPrice(String.valueOf(seekBar.getProgress()+4));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    ///////////////// END SeekBar Listener /////////////////

    ///////////////////// Gas View /////////////////////////

    /**
     * Значение присваивается визуальным элементам
     * The value is assigned to the visual elements
     * @param value Value Gas Limit and Gas Price
     */
    public void GetGasLimit(String value) {
        //tv_gas_limit.setText(value);
        GetFee();
    }
    public void GetGasPrice(String value) {
        //tv_gas_price.setText(value);
        GetFee();
    }
    /////////////////////////////////////////////////////////////////

    /////////////////////////// Get Fee /////////////////////////////

    /**
     * Значение GazLimit и GasPrice конвертируеться в BigInteger и присваиваеться глобальным переменным
     * The value GazLimit and GasPrice converteres in BigInteger and prizhivaetsya global variables
     *
     * Расчет вознагрождения для майнеров
     * calculate the fee for miners
     */

    public void GetFee(){
        GasPrice = Convert.toWei("10",Convert.Unit.GWEI).toBigInteger();
        GasLimit = BigInteger.valueOf(Integer.valueOf(String.valueOf("74444")));

        // fee
        BigDecimal fee = BigDecimal.valueOf(GasPrice.doubleValue()*GasLimit.doubleValue());
        BigDecimal feeresult = Convert.fromWei(fee.toString(),Convert.Unit.ETHER);
        tv_fee.setText(feeresult.toPlainString() + " ETH");
    }
    ///////////////////////// End Get Fee ///////////////////////////

    /////////////////////// On Click /////////////////////////
    /**
     * Начать выполнение потока для отправки эфира или Токена
     * Start executing thread for sending Ether or sending Token
     */
    public void onClick(View view) {
        SendingToken st = new SendingToken();
        SendingEther se = new SendingEther();
        switch (view.getId()) {
            case R.id.SendEther:
                se.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.SendToken:
                st.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.qr_small:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.qr_view);
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(QRGen(ethaddress.getText().toString(), 600, 600));
                dialog.show();
                break;
            case R.id.qrScan:
                qrScan.setOrientationLocked(false);
                qrScan.setBarcodeImageEnabled(true);
                qrScan.initiateScan();
                break;
        }

    }
    /////////////////////// end on click /////////////////////

    ///////////////////// Create and Load Wallet /////////////////
    public class WalletCreate extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(KeyDir.getAbsolutePath()+"/"+Key);

            Log.d("WC : ",KeyDir.getAbsolutePath()+"/"+Key);
            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 // Получаем Баланс
                 // Get balance Ethereum
                 */
                EthGetBalance etherbalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalance);

                /**
                 // Загружаем Токен
                 // Download Token
                 */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);

                /**
                 // Получаем название токена
                 // Get the name of the token
                 */
                String tokenname = token.name().send();
                System.out.println("Token Name: " + tokenname);

                /**
                 // Получаем Символ Токена
                 // Get Symbol marking token
                 */
                String tokensymbol = token.symbol().send();
                System.out.println("Symbol Token: " + tokensymbol);

                /**
                 // Получаем адрес Токена
                 // Get The Address Token
                 */
                String tokenaddress = token.getContractAddress();
                System.out.println("Address Token: " + tokenaddress);

                /**
                 // Получаем общее количество выпускаемых токенов
                 // Get the total amount of issued tokens
                 */
                BigInteger totalSupply = token.totalSupply().send();
                System.out.println("Supply Token: "+totalSupply.toString());

                /**
                 // Получаем количество токенов в кошельке
                 // Receive the Balance of Tokens in the wallet
                 */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());

                JSONObject result = new JSONObject();
                result.put("ethaddress",address);
                result.put("ethbalance", ethbalance);
                result.put("tokenbalance", Convert.fromWei(String.valueOf(tokenbalance),Convert.Unit.ETHER));
                result.put("tokenname", tokenname);
                result.put("tokensymbol", tokensymbol);
                result.put("tokenaddress",tokenaddress);
                result.put("tokensupply",Convert.fromWei(String.valueOf(totalSupply),Convert.Unit.ETHER));
                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null ){
                ethaddress.setText(result.get("ethaddress").toString());
                ethbalance.setText(result.get("ethbalance").toString());
                tokenname.setText(result.get("tokenname").toString());
                tokensymbol.setText(result.get("tokensymbol").toString());
                tokensupply.setText(result.get("tokensupply").toString());
                tokenaddress.setText(result.get("tokenaddress").toString());
                tokenbalance.setText(result.get("tokenbalance").toString());
                tokensymbolbalance.setText(" "+result.get("tokensymbol").toString());

                qr_small.setImageBitmap(QRGen(result.get("ethaddress").toString(), 200, 200));
            }
            else{
                System.out.println("Error!!!");
            }

        }
    }
    ////////////////// End create and load wallet ////////////////

    ///////////////////////// Sending Tokens /////////////////////
    public class SendingToken extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(KeyDir.getAbsolutePath()+"/"+Key);



            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 * Загружаем Токен
                 * Load Token
                 */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);

                String status = null;
                String balance = null;

                /**
                 * Конвертируем сумму токенов в BigInteger и отправляем на указанные адрес
                 * Convert the amount of tokens to BigInteger and send to the specified address
                 */
                BigInteger sendvalue = BigInteger.valueOf(Long.parseLong(String.valueOf(sendtokenvalue.getText())));
                Log.d("Token Num", String.valueOf(Convert.toWei(String.valueOf(sendvalue),Convert.Unit.ETHER).toBigInteger()));
                status = token.transfer(String.valueOf(sendtoaddress.getText()), Convert.toWei(String.valueOf(sendvalue),Convert.Unit.ETHER).toBigInteger()).send().getTransactionHash();

                /**
                 * Обновляем баланс Токенов
                 * Renew Token balance
                 */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());
                balance = tokenbalance.toString();

                /**
                 * Возвращаем из потока, Статус транзакции и баланс Токенов
                 * Returned from thread, transaction Status and Token balance
                 */
                JSONObject result = new JSONObject();
                result.put("status",status);
                result.put("balance",balance);

                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

                if (result != null) {
                    // Convert.fromWei(String.valueOf(tokenbalance),Convert.Unit.ETHER)

                tokenbalance.setText(String.valueOf(Double.parseDouble(result.get("balance").toString())/Math.pow(10,18)));
                Toast toast = Toast.makeText(getApplicationContext(),result.get("status").toString(), Toast.LENGTH_LONG);
                toast.show();
                System.out.println("거래주소: " +result.get("status").toString());
            } else {System.out.println();}
        }
    }
    /////////////////////// End Sending Tokens ///////////////////

    ///////////////////////// Sending Ether //////////////////////
    public class SendingEther  extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(KeyDir.getAbsolutePath()+"/"+Key);

            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 * Получаем счетчик транзакций
                 * Get count transaction
                 */
                EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                /**
                 * Convert ammount ether to BigInteger
                 */
                BigInteger value = Convert.toWei(String.valueOf(sendethervalue.getText()), Convert.Unit.ETHER).toBigInteger();

                /**
                 * Транзакция
                 * Transaction
                 */
                RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(nonce, GasPrice, GasLimit, String.valueOf(sendtoaddress.getText()), value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = "0x"+ Hex.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue.toString()).sendAsync().get();

                /**
                 * Get Transaction Error and Hash
                 */
                System.out.println("Error: "+ ethSendTransaction.getError());
                System.out.println("Transaction: " + ethSendTransaction.getTransactionHash());

                /**
                 * Возвращаем из потока, Адрес и Хэш транзакции
                 * Returned from thread, Ether Address and transaction hash
                 */
                JSONObject JsonResult = new JSONObject();
                JsonResult.put("Address", address);
                JsonResult.put("TransactionHash", ethSendTransaction.getTransactionHash());

                return JsonResult;

            }catch (Exception ex) {ex.printStackTrace();}
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                /**
                 * Получаем баланс Ethereum
                 * Get balance Ethereum
                 */
                EthGetBalance etherbalance = web3.ethGetBalance(result.get("Address").toString(), DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalanceafter = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalanceafter);

                ethbalance.setText(ethbalanceafter);
            } catch(Exception ex) {System.out.println(ex);}

            Toast toast = Toast.makeText(getApplicationContext(),result.get("TransactionHash").toString(), Toast.LENGTH_LONG);
            toast.show();
        }

    }
    //////////////////// End Sending Ether ///////////////////////



    //JSON파싱
    private class GetData_wallet extends AsyncTask<String, Void, String> {

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
                mJsonString_wallet = result;
                showResult_wallet();

            }
        }


        @Override
        protected String doInBackground(String... params) {


            try {
                httpclient = new DefaultHttpClient();
                httppost = new HttpPost("http://13.125.115.186/walletAddr.php");
                nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair("name", otherNick));
                Log.d("otherNick",otherNick);
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

    private void showResult_wallet(){
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(mJsonString_wallet);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                org.json.JSONObject item = jsonArray.getJSONObject(i);


                walletName = item.getString("wallet");




            }



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(),walletName+"",Toast.LENGTH_SHORT).show();
                    Log.d("walletName",walletName);
                    sendtoaddress.setText(walletName);
                    sendtonick.setText(otherNick+"||");
                }
            });

        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

}
