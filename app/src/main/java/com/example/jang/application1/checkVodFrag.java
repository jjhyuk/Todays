package com.example.jang.application1;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class checkVodFrag extends Fragment {

    View view;
    String nick ;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HashMap<String,String> hashMap = new HashMap<>();
    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    HttpPost httppost;
    Handler handler = new Handler();

    GridView gridView ;

    vod_adapter vod_adapter = new vod_adapter();
    String only_vod_name ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.checkvod,container,false);
        nick = ((Home)getActivity()).nickTv;
        Log.d("nick", "onCreateView: "+nick.toString());
        GetData_body getData_body = new GetData_body();
        getData_body.execute();


        gridView = view.findViewById(R.id.vod_grid);
        gridView.setAdapter(vod_adapter);



        return view;
    }

    public class vod_adapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
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
                view = inflater.inflate(R.layout.checkvod_item, viewGroup, false);
            }
            ImageView vodBodyImage = view.findViewById(R.id.vodBodyImage);
            final TextView vodBodyName = view.findViewById(R.id.vodBodyName);
            LinearLayout vodLinear = view.findViewById(R.id.vod_linear);

            Log.d("List",list.get(i).get("thumbnail"+i));
            Log.d("List",list.get(i).get("vod_name"+i));
            Glide.with(view).load("http://13.125.115.186/"+list.get(i).get("thumbnail"+i)).into(vodBodyImage);

            String[] vod_split = list.get(i).get("vod_name"+i).split("/");
            Log.d("vod_split ", vod_split[0]);
            Log.d("vod_split ", vod_split[1]);
            String[] mp4_split = vod_split[1].split("\\.");
            Log.d("mp4_split ", mp4_split[0]);
            Log.d("mp4_split ", mp4_split[1]);
            only_vod_name = mp4_split[0];
            vodBodyName.setText(only_vod_name);


            vodLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final String[] str = {"VOD 재생하기","VOD 통계확인"};
                    builder.setTitle("VOD")
                            .setNegativeButton("돌아가기",null)
                            .setItems(str, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int j) {
                                    if(j==0)
                                    {
                                        Intent intent = new Intent(getActivity(),Vod_watching.class);
                                        intent.putExtra("vod",list.get(i).get("vod_name"+i));
                                        startActivity(intent);

                                    }
                                    else if(j==1)
                                    {
                                        Intent intent = new Intent(getActivity(),vod_chart.class);
                                        intent.putExtra("vod",vodBodyName.getText().toString());
                                        startActivity(intent);

                                    }
                                }
                            });
                    builder.create();
                    builder.show();
                }
            });

            return view;
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

                httppost = new HttpPost("http://13.125.115.186/vodBody.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("name", nick.toString()));



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
            list = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String no = item.getString("no");
                String u_nick = item.getString("u_nick");
                String vod_name = item.getString("vod_name");

                String thumbnail = item.getString("thumbnail");
                if(thumbnail.equals("null"))
                {
                    thumbnail = "vod/"+thumbnail+".png";
                }


                hashMap = new HashMap<>();

                hashMap.put("no"+i, no);
                hashMap.put("u_nick"+i, u_nick);
                hashMap.put("vod_name"+i, vod_name);
                hashMap.put("thumbnail"+i, thumbnail);

                list.add(hashMap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        vod_adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }

}
