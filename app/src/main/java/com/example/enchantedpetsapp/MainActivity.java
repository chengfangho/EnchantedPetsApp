package com.example.enchantedpetsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.example.enchantedpetsapp.databinding.ActivityMainBinding;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "ap9bgs3gweked-ats.iot.us-west-1.amazonaws.com";

    private static final String COGNITO_POOL_ID = "us-west-1:4c05e995-41b5-45c8-8758-21edc190a1ce";

    private static final Regions MY_REGION = Regions.US_WEST_1;

    Button buttonDispense;
    Button buttonInteract;
    Button buttonSnap;
    Button buttonVoice;

    AWSIotMqttManager mqttManager;
    String clientId;
    CognitoCachingCredentialsProvider credentialsProvider;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.Home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.Profile:
                    replaceFragment(new ProfileFragment());
                    break;
                case R.id.Settings:
                    replaceFragment(new SettingsFragment());
                    break;
            }
            return true;
        });

        /*buttonDispense = (Button) findViewById(R.id.dispense);
        buttonDispense.setOnClickListener(dispenseClick);

        buttonInteract = (Button) findViewById(R.id.interact);
        buttonInteract.setOnClickListener(interactClick);

        buttonSnap = (Button) findViewById(R.id.snap);
        buttonSnap.setOnClickListener(snapClick);

        buttonVoice = (Button) findViewById(R.id.voice);
        buttonVoice.setOnClickListener(voiceClick);*/

        clientId = UUID.randomUUID().toString();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();
        mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
            @Override
            public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {

            }
        });
        System.out.println("Connected");

    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManger = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }


}