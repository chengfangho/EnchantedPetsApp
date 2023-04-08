package com.example.enchantedpetsapp;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import java.util.UUID;

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
    }

    public void publishDispense(){
        mqttManager.publishString("toggle","picow/dispenser", AWSIotMqttQos.QOS1);
    }
    public void publishLaser(){
        mqttManager.publishString("toggle","picow/laser", AWSIotMqttQos.QOS1);
    }
    public void publishBubble(){
        mqttManager.publishString("toggle","picow/bubble", AWSIotMqttQos.QOS1);
    }
    public void publishSnap(){
        mqttManager.publishString("toggle","picow/motion", AWSIotMqttQos.QOS1);
    }
    public void subscribeMotion(){
        System.out.println("subscribed");
        mqttManager.subscribeToTopic("picow/motion", AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
            public void onMessageArrived(String topic, byte[] data) {
                Toast.makeText(getApplicationContext(),"motion detected",Toast.LENGTH_LONG).show();
                System.out.println("motion detected");
            }});
    }
}
