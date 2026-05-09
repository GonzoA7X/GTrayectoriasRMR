package com.example.gtrayectoriasrmr;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class Conexion_Bluetooth extends AppCompatActivity {

    // --- Constants ---
    public static String EXTRA_ADDRESS = "device_address";
    private static final int BT_PERMISSIONS_REQUEST_CODE = 101;

    // --- UI Views ---
    private Button mBtnPaired;
    private ListView mDeviceList;

    // --- Bluetooth ---
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesAdapter;
    private ArrayList<BluetoothDevice> mPairedDevicesList = new ArrayList<>();

    // --- Activity Result Launchers ---
    private final ActivityResultLauncher<Intent> mTurnOnBtLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth no fue activado.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> mRequestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Check if all permissions were granted
                if (permissions.values().stream().allMatch(granted -> granted)) {
                    listPairedDevices(); // If permissions are granted, list devices
                } else {
                    Toast.makeText(this, "Se requieren permisos de Bluetooth para continuar", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conexion_bluetooth);

        mBtnPaired = findViewById(R.id.button);
        mDeviceList = findViewById(R.id.IdLista);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Dispositivo Bluetooth no disponible", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mPairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mDeviceList.setAdapter(mPairedDevicesAdapter);
        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = mPairedDevicesList.get(position);
            if (device != null) {
                Intent i = new Intent(Conexion_Bluetooth.this, envio_bt.class);
                i.putExtra(EXTRA_ADDRESS, device.getAddress()); // Pass the correct MAC address
                startActivity(i);
            }
        });

        mBtnPaired.setOnClickListener(v -> checkPermissionsAndListDevices());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check BT status and permissions every time the activity is resumed
        checkBluetoothState();
    }

    private void checkBluetoothState(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mTurnOnBtLauncher.launch(turnOn);
        }
    }

    private void checkPermissionsAndListDevices(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mRequestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
        } else {
            listPairedDevices(); // For older versions, just list them
        }
    }

    private void listPairedDevices() {
        mPairedDevicesAdapter.clear();
        mPairedDevicesList.clear();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 // This is a safeguard, permission should be granted by now.
                 Toast.makeText(this, "Error de permisos de conexión", Toast.LENGTH_SHORT).show();
                 return;
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.isEmpty()) {
                Toast.makeText(this, "No se encontraron dispositivos sincronizados", Toast.LENGTH_LONG).show();
            } else {
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesList.add(device);
                    mPairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        } catch (Exception e){
            Toast.makeText(this, "Error al listar dispositivos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
