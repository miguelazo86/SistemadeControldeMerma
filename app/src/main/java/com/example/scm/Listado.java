package com.example.scm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Listado extends AppCompatActivity implements View.OnClickListener {

    Button btn_cambiar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        btn_cambiar = (android.widget.Button) findViewById(R.id.btn_cambiar);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_cambiar){
            Intent intent = new Intent(this, MainActivity.class );

        }

    }
}