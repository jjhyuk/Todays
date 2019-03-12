package com.example.jang.application1.TEST;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jang.application1.R;

public class Test_Broad extends AppCompatActivity {

    TextView Broad_name ;
    Button btn;
    String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmp_broad);
        Intent get = getIntent();
        name = get.getStringExtra("name");
        Broad_name = findViewById(R.id.Test_edit);
        btn = findViewById(R.id.Test_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Test_See.class);
                intent.putExtra("name",name);
                intent.putExtra("room",Broad_name.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }
}
