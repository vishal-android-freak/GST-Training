package in.autonxt.siesgst;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private boolean isLivingRoomLightOn = false, isBedRoomLightOn = false,
            isStoreRoomLightOn = false, isTerraceLightOn = false;

    private boolean isFirebaseSelected;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectServiceDialog();

        LinearLayout livingRoom = findViewById(R.id.livingRoom);
        livingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirebaseSelected) {
                    reference.child("/LR/value").setValue(isLivingRoomLightOn ? "0" : "1");
                } else {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isLivingRoomLightOn ? "0".getBytes() : "1".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    try {
                        client.publish("/siesgst/LR", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        LinearLayout bedRoom = findViewById(R.id.bedroom);
        bedRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirebaseSelected) {
                    reference.child("/BR/value").setValue(isBedRoomLightOn ? "0" : "1");
                } else {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isBedRoomLightOn ? "0".getBytes() : "1".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    try {
                        client.publish("/siesgst/BR", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        LinearLayout storeRoom = findViewById(R.id.storeRoom);
        storeRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirebaseSelected) {
                    reference.child("/SR/value").setValue(isStoreRoomLightOn ? "0" : "1");
                } else {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isStoreRoomLightOn ? "0".getBytes() : "1".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    try {
                        client.publish("/siesgst/SR", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        LinearLayout terrace = findViewById(R.id.terrace);
        terrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirebaseSelected) {
                    reference.child("/T/value").setValue(isTerraceLightOn ? "0" : "1");
                } else {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isTerraceLightOn ? "0".getBytes() : "1".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    try {
                        client.publish("/siesgst/T", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.unregisterResources();
        client.close();
    }

    private void connectMqtt() {
        client = new MqttAndroidClient(this, "tcp://test.mosquitto.org:1883", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("success", "mqtt connected");
                    try {
                        client.subscribe("/siesgst/#", 1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Log.d("success", "mqtt connection error");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("mqtt", "client disconnected");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String value = message.toString();
                switch (topic) {

                    case "/siesgst/LR":
                        isLivingRoomLightOn = value.equals("1");
                        ImageView livingRoomLight = findViewById(R.id.livingRoomImg);
                        livingRoomLight.setImageResource(isLivingRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                        break;

                    case "/siesgst/BR":
                        isBedRoomLightOn = value.equals("1");
                        ImageView bedRoomLight = findViewById(R.id.bedroomImg);
                        bedRoomLight.setImageResource(isBedRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                        break;

                    case "/siesgst/SR":
                        isStoreRoomLightOn = value.equals("1");
                        ImageView storeRoomLight = findViewById(R.id.storeRoomImg);
                        storeRoomLight.setImageResource(isStoreRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                        break;

                    case "/siesgst/T":
                        isTerraceLightOn = value.equals("1");
                        ImageView terraceRoomLight = findViewById(R.id.terraceImg);
                        terraceRoomLight.setImageResource(isTerraceLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void connectFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("/siesgst");
        reference.child("/LR/value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    isLivingRoomLightOn = value.equals("1");
                    ImageView livingRoomLight = findViewById(R.id.livingRoomImg);
                    livingRoomLight.setImageResource(isLivingRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.child("/BR/value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    isBedRoomLightOn = value.equals("1");
                    ImageView bedRoomLight = findViewById(R.id.bedroomImg);
                    bedRoomLight.setImageResource(isBedRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.child("/SR/value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    isStoreRoomLightOn = value.equals("1");
                    ImageView storeRoomLight = findViewById(R.id.storeRoomImg);
                    storeRoomLight.setImageResource(isStoreRoomLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.child("/T/value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    isTerraceLightOn = value.equals("1");
                    ImageView terraceRoomLight = findViewById(R.id.terraceImg);
                    terraceRoomLight.setImageResource(isTerraceLightOn ? R.drawable.bulb_on : R.drawable.bulb_off);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to enable to firebase? If not the mqtt will be enabled");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                connectFirebase();
                isFirebaseSelected = true;
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                connectMqtt();
                isFirebaseSelected = false;
            }
        });
        builder.create();
        builder.show();
    }
}
