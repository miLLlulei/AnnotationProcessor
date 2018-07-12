package com.mill.apt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.HelloWorld;
import com.mill.annotation.BindView;
import com.mill.annotation.HelloAnnotation;
import com.mill.annotation.OnClick;

@HelloAnnotation
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn1)
    public Button btn1;

    @BindView(R.id.btn2)
    public Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyAnnotationUtils.bind(this);
        HelloWorld.main(null);

        btn1.setText("测试成功1");

        btn2.setText("测试成功2");


    }

    @OnClick({R.id.btn1, R.id.btn2})
    public void onBtn1Click(View v) {
        Toast.makeText(this, "测试" + ((Button)v).getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
