package com.example.administrator.customswitchdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    CustomSwitch c1,c2;
    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c1= (CustomSwitch) findViewById(R.id.c1);
        c2= (CustomSwitch) findViewById(R.id.c2);

        c1.setCallBack(new CustomSwitch.CustomSwitchCallBack() {
            @Override
            public void selected(int position) {
                Toast.makeText(MainActivity.this,"c1-->"+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void offset(float offset) {
                Log.d("C1","offset-->"+offset);
            }

            @Override
            public void state(boolean state) {
                btn.setText("开关为："+state);
            }
        });
        c2.setCallBack(new CustomSwitch.CustomSwitchCallBack() {
            @Override
            public void selected(int position) {
                Toast.makeText(MainActivity.this,"c2-->"+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void offset(float offset) {
                Log.d("C2","offset-->"+offset);
            }

            @Override
            public void state(boolean state) {

            }
        });
        btn= (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c1.getOnOffSwitch()){
                    c1.setOnOffSwitch(false);
                }else {
                    c1.setOnOffSwitch(true);
                }
            }
        });

    }

}
