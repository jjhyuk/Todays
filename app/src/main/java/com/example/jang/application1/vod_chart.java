package com.example.jang.application1;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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
import java.util.List;

public class vod_chart extends AppCompatActivity {

    TextView char_vod_text;
    TextView chart_vod_viewer_num;


    String vod;
    PieChart agePieChart ;
    PieChart genderPieChart ;

    HttpPost httppost;
    String mJsonString;
    private static final String TAG_JSON="webnautes";
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;

    ArrayList<vod_chart_class> list =new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_chart);

        agePieChart = findViewById(R.id.agePieChart);
        genderPieChart = findViewById(R.id.genderPieChart);

        Intent intent = getIntent();
        vod = intent.getStringExtra("vod");
        char_vod_text = findViewById(R.id.chart_vod_text);
        char_vod_text.setText(vod);
        chart_vod_viewer_num = findViewById(R.id.chart_vod_viewer_num);

        GetData getData = new GetData();
        getData.execute();

    }

    //JSON파싱
    private class GetData extends AsyncTask<String, Void, String> {

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

                httppost = new HttpPost("http://13.125.115.186/getvodchart.php");
                httppost.addHeader("Cache-Control", "no-cache");
                nameValuePairs = new ArrayList<>(1);

                nameValuePairs.add(new BasicNameValuePair("vod", vod));



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

    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            list = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){
                vod_chart_class chart_contents = new vod_chart_class();

                JSONObject item = jsonArray.getJSONObject(i);
                String u_nick = item.getString("u_nick");
                String vod_name = item.getString("vod_name");
                String u_gender = item.getString("u_gender");
                String tmp = item.getString("u_age");
                int u_age = Integer.parseInt(tmp);

                Log.d("u_nick ",u_nick);
                Log.d("vod_name ",vod_name);
                Log.d("u_gender ",u_gender);
                Log.d("u_age ",String.valueOf(u_age));


                chart_contents.u_nick = u_nick;
                chart_contents.u_age = u_age;
                chart_contents.u_gender = u_gender;
                chart_contents.vod_name = vod_name;

                list.add(chart_contents);


            }

            chart_vod_viewer_num.setText(list.size()+"명");

            int man = 0;
            int woman = 0;
            for(int i = 0; i<list.size();i++)
            {
                if(list.get(i).u_gender.equals("남자"))
                {
                    man++;
                }
                else
                {
                    woman++;
                }
            }
            genderPieChart.setUsePercentValues(true);

            genderPieChart.getDescription().setEnabled(false);
            genderPieChart.setExtraOffsets(5,10,5,5);

            genderPieChart.setDragDecelerationFrictionCoef(0.95f);

            genderPieChart.setDrawHoleEnabled(false);
            genderPieChart.setHoleColor(Color.WHITE);
            genderPieChart.setEntryLabelColor(Color.BLACK);
            genderPieChart.setTransparentCircleRadius(61f);
            Log.d("man",String.valueOf(man));
            Log.d("woman",String.valueOf(woman));

            ArrayList<PieEntry> yValues_gender = new ArrayList<PieEntry>();
            yValues_gender.add(new PieEntry(man,"남자: "+man+"명"));
            yValues_gender.add(new PieEntry(woman,"여자: "+woman+"명"));

            Description description_gender = new Description();
            description_gender.setText("성별"); //라벨
            description_gender.setTextSize(15);
            genderPieChart.setDescription(description_gender);

            genderPieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

            PieDataSet dataSet_gender = new PieDataSet(yValues_gender,"");
            dataSet_gender.setSliceSpace(3f);
            dataSet_gender.setSelectionShift(5f);
            dataSet_gender.setColors(ColorTemplate.JOYFUL_COLORS);

            PieData data_gender = new PieData((dataSet_gender));
            data_gender.setValueTextSize(10f);
            data_gender.setValueFormatter(new PercentFormatter());
            data_gender.setValueTextColor(Color.BLACK);

            genderPieChart.setData(data_gender);


            int age[] = new int[8];
            for(int i = 0; i<list.size();i++)
            {
                int age_value = list.get(i).u_age/10;

                switch (age_value)
                {
                    case 0:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 1:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 2:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 3:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 4:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 5:
                    {
                        age[age_value]++;
                        break;
                    }
                    case 6:
                    {
                        age[age_value]++;
                        break;
                    }
                   default:
                   {
                       age[7]++;
                       break;
                   }
                }
            }

            agePieChart.setUsePercentValues(true);

            agePieChart.getDescription().setEnabled(true);
            agePieChart.setExtraOffsets(5,10,5,5);

            agePieChart.setDragDecelerationFrictionCoef(0.95f);
            agePieChart.setEntryLabelColor(Color.BLACK);
            agePieChart.setDrawHoleEnabled(false);
            agePieChart.setHoleColor(Color.WHITE);
            genderPieChart.setTransparentCircleRadius(61f);

            ArrayList<PieEntry> yValues_age= new ArrayList<PieEntry>();

            if(age[0]!=0)
            yValues_age.add(new PieEntry(age[0],"10세 미만: "+age[0]+"명"));

            if(age[1]!=0)
            yValues_age.add(new PieEntry(age[1],"10대: "+age[1]+"명"));

            if(age[2]!=0)
            yValues_age.add(new PieEntry(age[2],"20대: "+age[2]+"명"));

            if(age[3]!=0)
            yValues_age.add(new PieEntry(age[3],"30대: "+age[3]+"명"));

            if(age[4]!=0)
            yValues_age.add(new PieEntry(age[4],"40대: "+age[4]+"명"));

            if(age[5]!=0)
            yValues_age.add(new PieEntry(age[5],"50대: "+age[5]+"명"));

            if(age[6]!=0)
            yValues_age.add(new PieEntry(age[6],"60대: "+age[6]+"명"));

            if(age[7]!=0)
            yValues_age.add(new PieEntry(age[7],"70세 이상: "+age[7]+"명"));


            Description description_age = new Description();
            description_age.setText("나이"); //라벨
            description_age.setTextSize(15);
            agePieChart.setDescription(description_age);

            agePieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

            PieDataSet dataSet_age = new PieDataSet(yValues_age,"");
            dataSet_age.setSliceSpace(3f);
            dataSet_age.setSelectionShift(5f);
            dataSet_age.setColors(ColorTemplate.JOYFUL_COLORS);



            PieData data_age = new PieData((dataSet_age));
            data_age.setValueFormatter(new PercentFormatter());
            data_age.setValueTextSize(10f);
            data_age.setValueTextColor(Color.BLACK);

            agePieChart.setData(data_age);

        } catch (JSONException e) {

            Log.d("TAG", "showResult : ", e);
        }

    }
}
