package com.example.gtrayectoriasrmr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Configuracion extends AppCompatActivity {

    private EditText et1, et2, et3, et4, et5,et6;
    private Button guardar;
    private Button leer;
    private static final String WMAX_FILE = "wmax.txt";
    private static final String RLLANTAS_FILE = "rllantas.txt";
    private static final String DLLANTAS_FILE = "dllantas.txt";
    private static final String VLINEAL_FILE = "vlineal.txt";
    private static final String LARGO_FILE = "largo.txt";
    private static final String ANCHO_FILE = "ancho.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setUpView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView() {
        et1 = findViewById(R.id.wmax);
        et2 = findViewById(R.id.rllantas);
        et3 = findViewById(R.id.dllantas);
        et4 = findViewById(R.id.largo);
        et5 = findViewById(R.id.ancho);
        et6 = findViewById(R.id.vlineal);
        guardar = findViewById(R.id.salvar);

        guardar.setOnClickListener(view -> savefile());
        leer = findViewById(R.id.read);
        leer.setOnClickListener(view -> readfile());
    }


    private void savefile() {
        String textoASalvar1 = et1.getText().toString();
        String textoASalvar2 = et2.getText().toString();
        String textoASalvar3 = et3.getText().toString();
        String textoASalvar4 = et4.getText().toString();
        String textoASalvar5 = et5.getText().toString();
        String textoASalvar6 = et6.getText().toString();

        // Complete validation for all fields before deleting or writing
        if (textoASalvar1.isEmpty() || textoASalvar2.isEmpty() || textoASalvar3.isEmpty() || textoASalvar4.isEmpty() || textoASalvar5.isEmpty() || textoASalvar6.isEmpty()) {
            Toast.makeText(getBaseContext(), "Debes ingresar todos los parametros", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // No need to delete files first, openFileOutput with MODE_PRIVATE overwrites them.
            writeFile(WMAX_FILE, textoASalvar1);
            writeFile(RLLANTAS_FILE, textoASalvar2);
            writeFile(DLLANTAS_FILE, textoASalvar3);
            writeFile(VLINEAL_FILE, textoASalvar6);
            writeFile(LARGO_FILE, textoASalvar4);
            writeFile(ANCHO_FILE, textoASalvar5);

            et1.setText("");
            et2.setText("");
            et3.setText("");
            et4.setText("");
            et5.setText("");
            et6.setText("");

            Toast.makeText(getBaseContext(), "Datos Guardados", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Error al guardar los datos", Toast.LENGTH_LONG).show();
        }
    }

    private void writeFile(String filename, String data) throws Exception {
        // Using MODE_PRIVATE will overwrite the file, which is the desired behavior here.
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(data.getBytes());
            Log.d("Configuracion", "Informacion guardada en: " + getFilesDir() + "/" + filename);
        }
    }

    private void readfile() {
        et1.setText(readFile(WMAX_FILE));
        et2.setText(readFile(RLLANTAS_FILE));
        et3.setText(readFile(DLLANTAS_FILE));
        et4.setText(readFile(LARGO_FILE));
        et5.setText(readFile(ANCHO_FILE));
        et6.setText(readFile(VLINEAL_FILE));
    }

    private String readFile(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = openFileInput(filename);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line); // Avoid adding extra newline characters from the read process
            }
        } catch (Exception e) {
            Log.e("Configuracion", "Error al leer archivo: " + filename, e);
        }
        return stringBuilder.toString();
    }
}
