package com.example.drumpad;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //Global Variables
    Button snare;
    Button crash;
    Button hat;
    Button bass;
    ImageView bt;
    //Sounds
    MediaPlayer snareSound;
    MediaPlayer crashSound;
    MediaPlayer hatSound;
    MediaPlayer bassSound;
    MediaPlayer[] soundList;
    //Bluetooth
    public static BluetoothSocket socket;
    InputStream inStream;


    public void bluetoothInit(Context context) throws NullPointerException, IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter(); //Initialized in header
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();


                OutputStream outputStream;

                if (bondedDevices.size() == 1) {

                    runOnUiThread(() -> Toast.makeText(context, "Pairing...", Toast.LENGTH_SHORT).show());
                    Object[] devices = (Object[]) bondedDevices.toArray();
                    BluetoothDevice device = (BluetoothDevice) devices[0];
                    ParcelUuid[] uuids = device.getUuids();
                    socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid()); //Initialized in header
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();

                    bluetoothListener();

                    runOnUiThread(()->Toast.makeText(context, "Paired with device", Toast.LENGTH_SHORT).show());

                } else if(bondedDevices.size() > 1){
                    //If more than 1 bluetooth device is paired with the smartphone
                    //Work in progress
                    runOnUiThread(() ->Toast.makeText(context, "Pairing...", Toast.LENGTH_SHORT).show());
                    Object[] devices = (Object[]) bondedDevices.toArray();


                    int length = Array.getLength(devices);
                    String name;
                    BluetoothDevice device = null;

                    for(int i = 0; i < length; i++){
                        device = (BluetoothDevice) devices[i];
                        name = device.getName();

                        if(name.equals("HC-06")){
                           break;
                        } else {
                            device = null;
                        }
                    }

                    ParcelUuid[] uuids = device.getUuids();
                    socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid()); //Initialized in header
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();

                    bluetoothListener();

                    runOnUiThread(()->Toast.makeText(context, "Paired with device", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(context, "No appropriate paired devices", Toast.LENGTH_SHORT).show());
                }
            } else {
                blueAdapter = null;
                Toast.makeText(context, "Bluetooth is disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void bluetoothListener(){
        new Thread(() -> {
            BufferedReader reader;
            String readMessage = null;
            double command = 0;
            while(true){
                try {
                    reader = new BufferedReader(new InputStreamReader(inStream));
                    readMessage = reader.readLine();

                    readMessage = readMessage.replaceAll("[^\\d.]","");
                    command = Double.parseDouble(readMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(!readMessage.isEmpty() && !readMessage.equals("\\r\\n") && command < 4) {
                    playSound(soundList[(int) command]);
                }
            }
        }).start();
    }

    public void playSound(MediaPlayer mp){
        new Thread(() -> {
                mp.start();
            }).start();
    }

    public void init(){
        snare = findViewById(R.id.snareButton);
        hat = findViewById(R.id.hatButton);
        crash = findViewById(R.id.crashButton);
        bass = findViewById(R.id.bassButton);
        bt = findViewById(R.id.btConnect);

        //Initiate sounds
        snareSound = MediaPlayer.create(this, R.raw.snare);
        hatSound = MediaPlayer.create(this, R.raw.hhclose);
        crashSound = MediaPlayer.create(this, R.raw.crash3);
        bassSound = MediaPlayer.create(this, R.raw.bdrum);
        soundList = new MediaPlayer[]{snareSound, crashSound, hatSound, bassSound};

        try {
            bluetoothInit(getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Failed" ,Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        init();

        snare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(snareSound);
            }
        });

        hat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(hatSound);
            }
        });

        crash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(crashSound);
            }
        });

        bass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(bassSound);
            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bluetoothInit(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //init();
    }
}