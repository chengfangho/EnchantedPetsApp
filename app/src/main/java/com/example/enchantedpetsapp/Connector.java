package com.example.enchantedpetsapp;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Connector extends AppCompatActivity {

    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "ap9bgs3gweked-ats.iot.us-west-1.amazonaws.com";
    private static final String COGNITO_POOL_ID = "us-west-1:4c05e995-41b5-45c8-8758-21edc190a1ce";
    private static final Regions MY_REGION = Regions.US_WEST_1;

    CognitoCachingCredentialsProvider credentialsProvider;
    AWSIotMqttManager mqttManager;
    String clientId;

    public Connector(Context context){
        clientId = UUID.randomUUID().toString();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context, // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);
    }
    public void connect(){
        mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
            public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {

            }
        });
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //mqttManager.publishString("toggle","picow/led", AWSIotMqttQos.QOS1);
    }
}
