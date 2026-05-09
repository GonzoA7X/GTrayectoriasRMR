package com.example.gtrayectoriasrmr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button conexbt;
    Button gtmc;

    Button conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conexbt= (Button) findViewById(R.id.conexionBT);

        conexbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this,Conexion_Bluetooth.class));
            }
        });

        conf= (Button) findViewById(R.id.config);

        conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this,Configuracion.class));
            }
        });


        gtmc= (Button) findViewById(R.id.gtrayectmodcin);

        gtmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this,Generar_TrayectoriaMC.class));
            }
        });
    }
}