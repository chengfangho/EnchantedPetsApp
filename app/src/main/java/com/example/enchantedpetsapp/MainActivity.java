package com.example.enchantedpetsapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.example.enchantedpetsapp.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
        ActivityResultLauncher<Intent> requestManageAllFilesAccessPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            // Permission granted
                            // Take screenshot and save it to external storage
                        } else {
                            // Permission denied
                        }
                    }
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            requestManageAllFilesAccessPermissionLauncher.launch(intent);
        } else {
            // Take screenshot and save it to external storage
        }
        super.onCreate(savedInstanceState);

        // Inflate the welcome screen layout
        setContentView(R.layout.activity_welcome);

        // Set a click listener on the welcome screen to switch to the main activity immediately
        findViewById(R.id.welcome_screen_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToMainActivity();
            }
        });


    }

    private void switchToMainActivity() {
        // Switch to the main activity
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        HomeFragment home = new HomeFragment();
        replaceFragment(home);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.Home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.Settings:
                    home.onPause();
                    replaceFragment(new SettingsFragment());
                    break;
                case R.id.BluetoothTest:
                    home.onPause();
                    replaceFragment(new BluetoothFragmentTest());
                    break;
                case R.id.Profile:
                    home.onPause();
                    replaceFragment(new ProfileFragment());
            }
            return true;
        });
        connector = new Connector(getApplicationContext());
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

    public void dispense(View view) {connector.publishDispense();}
    public void laser(View view) {connector.publishLaser();}
    public void snap(View view) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }}
    public void bubble(View view) {connector.publishBubble();}
    public void bluetooth(View view) {bluetoothConnect();}}