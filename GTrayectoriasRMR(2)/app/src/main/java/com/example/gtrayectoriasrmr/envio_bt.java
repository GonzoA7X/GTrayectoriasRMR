package com.example.gtrayectoriasrmr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class envio_bt extends AppCompatActivity {

    // --- Constants ---
    private static final int BT_CONNECT_PERMISSION_REQUEST_CODE = 101;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final Set<String> CONFIG_FILES_TO_IGNORE = new HashSet<>(Arrays.asList(
            "wmax.txt", "rllantas.txt", "dllantas.txt", "vlineal.txt", "largo.txt", "ancho.txt"
    ));

    // --- UI Views ---
    private Button mBtnDisconnect;
    private ListView mTrajectoryListView;
    private TextView mTitleTextView;

    // --- Bluetooth ---
    private String mDeviceAddress = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBtSocket = null;
    private boolean mIsBtConnected = false;

    // --- Data ---
    private ArrayList<String> mTrajectoryFiles = new ArrayList<>();
    private ArrayAdapter<String> mTrajectoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_bt);

        mDeviceAddress = getIntent().getStringExtra(Conexion_Bluetooth.EXTRA_ADDRESS);

        mBtnDisconnect = findViewById(R.id.button4);
        mTrajectoryListView = findViewById(R.id.listView_trajectories);
        mTitleTextView = findViewById(R.id.textView_title);

        // --- Initial UI State ---
        mTitleTextView.setText("Estableciendo conexión...");
        mTrajectoryListView.setEnabled(false);

        mTrajectoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTrajectoryFiles);
        mTrajectoryListView.setAdapter(mTrajectoryAdapter);

        mTrajectoryListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFile = mTrajectoryFiles.get(position);
            sendTrajectory(selectedFile);
        });

        mBtnDisconnect.setOnClickListener(v -> Disconnect());

        requestBluetoothPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mIsBtConnected){
            loadTrajectoryList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Disconnect();
    }

    private void loadTrajectoryList(){
        mTrajectoryFiles.clear();
        File dir = getFilesDir();
        // Filter to get .txt files that are NOT in the ignore list
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") && !CONFIG_FILES_TO_IGNORE.contains(name));

        if (files != null) {
            for (File file : files) {
                mTrajectoryFiles.add(file.getName());
            }
        }
        mTrajectoryAdapter.notifyDataSetChanged();

        if(mTrajectoryFiles.isEmpty()){
            Toast.makeText(this, "No hay trayectorias para enviar", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTrajectory(String filename) {
        if (!mIsBtConnected) {
            msg("Error: La conexión Bluetooth no está activa.");
            return;
        }
        try {
            FileInputStream fin = openFileInput(filename);
            StringBuilder temp = new StringBuilder();
            int c;
            while ((c = fin.read()) != -1) {
                temp.append((char) c);
            }
            fin.close();

            if (mBtSocket != null) {
                mBtSocket.getOutputStream().write(temp.toString().getBytes());
                msg("Enviando: " + filename);
                Toast.makeText(this, "Enviado Correctamente" + filename , Toast.LENGTH_LONG).show();
                new Handler().postDelayed(this::finish, 2000); // 2-second delay before finishing
            } else {
                msg("Error: El socket Bluetooth no es válido.");
            }
        } catch (Exception e) {
            msg("Error al leer o enviar el archivo.");
            e.printStackTrace();
        }
    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BT_CONNECT_PERMISSION_REQUEST_CODE);
            } else {
                new ConnectBT().execute();
            }
        } else {
            new ConnectBT().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BT_CONNECT_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ConnectBT().execute();
            } else {
                msg("Permiso de Bluetooth denegado.");
                finish();
            }
        }
    }

    private void Disconnect() {
        if (mBtSocket != null) {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                // Log error
            }
            mBtSocket = null;
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... devices) {
            try {
                if (mBtSocket == null || !mIsBtConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mDeviceAddress == null) return false;
                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

                    if (ActivityCompat.checkSelfPermission(envio_bt.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                         return false;
                    }
                    mBtSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBtSocket.connect();
                }
                return true;
            } catch (IOException e) {
                try {
                    if(mBtSocket != null) mBtSocket.close();
                } catch (IOException ex) { /* ignore */ }
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean connectSuccess) {
            super.onPostExecute(connectSuccess);

            if (!connectSuccess) {
                mTitleTextView.setText("Conexión Fallida");
                msg("Conexión Fallida. Intenta de nuevo.");
                new android.os.Handler().postDelayed(envio_bt.this::finish, 2000);
            } else {
                msg("Conectado con éxito");
                mTitleTextView.setText("Selecciona una Trayectoria");
                mIsBtConnected = true;
                mTrajectoryListView.setEnabled(true);
                loadTrajectoryList();
            }
        }
    }
}