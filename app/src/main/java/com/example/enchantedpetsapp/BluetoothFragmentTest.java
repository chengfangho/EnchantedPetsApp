package com.example.enchantedpetsapp;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragmentTest extends Fragment {

    BluetoothSocket mmSocket;
    Spinner devicesSpinner;
    Button refreshDevicesButton;
    TextView ssidTextView;
    TextView pskTextView;
    Button startButton;
    TextView messageTextView;
    private DeviceAdapter adapter_devices;
    final UUID uuid = UUID.fromString("815425a5-bfac-47bf-9321-c5ff980b5e11");
    final byte delimiter = 33;
    int readBufferPosition = 0;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BluetoothFragmentTest() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BluetoothFragmentTest newInstance(String param1, String param2) {
        BluetoothFragmentTest fragment = new BluetoothFragmentTest();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_bluetoothtest, container, false);
        ssidTextView = (TextView) view.findViewById(R.id.ssid_text);
        pskTextView = (TextView) view.findViewById(R.id.psk_text);
        messageTextView = (TextView) view.findViewById(R.id.messages_text);
        devicesSpinner = (Spinner) view.findViewById(R.id.devices_spinner);
        refreshDevicesButton = (Button) view.findViewById(R.id.refresh_devices_button);
        startButton = (Button) view.findViewById(R.id.start_button);
        refreshDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDevices();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidTextView.getText().toString();
                String psk = pskTextView.getText().toString();

                BluetoothDevice device = (BluetoothDevice) devicesSpinner.getSelectedItem();
                (new Thread(new workerThread(ssid, psk, device))).start();
            }
        });

        refreshDevices();
        return view;
    }

    private void refreshDevices() {
        adapter_devices = new DeviceAdapter(getContext(), R.layout.spinner_devices, new ArrayList<BluetoothDevice>());
        devicesSpinner.setAdapter(adapter_devices);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter_devices.add(device);
            }
        }
    }

    final class workerThread implements Runnable {
        private String ssid;
        private String psk;
        private BluetoothDevice device;

        public workerThread(String ssid, String psk, BluetoothDevice device) {
            this.ssid = ssid;
            this.psk = psk;
            this.device = device;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            clearOutput();
            writeOutput("Starting config update.");

            writeOutput("Device: " + device.getName() + " - " + device.getAddress());

            try {
                mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                if (!mmSocket.isConnected()) {
                    mmSocket.connect();
                    Thread.sleep(1000);
                }

                writeOutput("Connected.");

                OutputStream mmOutputStream = mmSocket.getOutputStream();
                final InputStream mmInputStream = mmSocket.getInputStream();

                waitForResponse(mmInputStream, -1);

                writeOutput("Sending SSID.");

                mmOutputStream.write(ssid.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, -1);

                writeOutput("Sending PSK.");

                mmOutputStream.write(psk.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, -1);

                mmSocket.close();

                writeOutput("Success.");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                writeOutput("Failed.");
            }

            writeOutput("Done.");
        }
    }

    private void writeOutput(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = messageTextView.getText().toString();
                messageTextView.setText(currentText + "\n" + text);
            }
        });
    }

    private void clearOutput() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTextView.setText("");
            }
        });
    }

    /*
     * TODO actually use the timeout
     */
    private void waitForResponse(InputStream mmInputStream, long timeout) throws IOException {
        int bytesAvailable;

        while (true) {
            bytesAvailable = mmInputStream.available();
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                byte[] readBuffer = new byte[1024];
                mmInputStream.read(packetBytes);

                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = packetBytes[i];

                    if (b == delimiter) {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");

                        writeOutput("Received:" + data);

                        return;
                    } else {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }
        }
    }
}