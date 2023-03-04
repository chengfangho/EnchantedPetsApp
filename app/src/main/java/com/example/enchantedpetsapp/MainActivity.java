package com.example.enchantedpetsapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.enchantedpetsapp.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    ActivityMainBinding binding;
    Connector connector;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.Home:
                    replaceFragment(new HomeFragment());
                    break;
                /*case R.id.Settings:
                    replaceFragment(new SettingsFragment());
                    break;
                case R.id.Bluetooth:
                    replaceFragment(new BluetoothFragment());
                    break;*/
                case R.id.BluetoothTest:
                    replaceFragment(new BluetoothFragmentTest());
            }
            return true;
        });
        connector = new Connector(this.getApplicationContext());
        connector.connect();
        connector.subscribeMotion();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManger = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void bluetoothConnect() {
        mBluetoothManager = getSystemService(BluetoothManager.class);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        try {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice("B8:27:EB:EC:43:6A");
        } catch (Exception NullPointerException) {
            Log.e(TAG, "create() failed", NullPointerException);
        }
        BluetoothSocket tmp = null;
        try{
            tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e){
            Log.e(TAG, "create() failed", e);
        }
        mBluetoothSocket = tmp;
        try {
            mBluetoothSocket.connect();
        } catch (IOException connectException) {
            try{
                mBluetoothSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }
    }

    public void dispense(View view) {
        connector.publishDispense();
    }
    public void laser(View view) {
        connector.publishLaser();
    }
    public void snap(View view) { connector.publishSnap();}
    public void bubble(View view) {connector.publishBubble();}
    public void bluetooth(View view) {bluetoothConnect();}

}