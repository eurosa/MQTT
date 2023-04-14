package com.myqtt.minpc.mqtt;


//import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.regions.Regions;
import com.example.rashminpc.mqtttest.R;
import com.google.android.material.navigation.NavigationView;
import com.kyleduo.switchbutton.SwitchButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivityCopy extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    private static final String CHANNEL_NAME = "name";
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button b ;
    Button c;
    private  String API_KEY ="api_key" ;
    private String channel_url = "https://api.thingspeak.com/channels.json";
    private SwitchButton switch2;
    TextView tvStatus;

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID ="us-east-1:d1a86c61-c800-4838-9033-43d4a49c7ae5";// "us-west-2_iQ1FI8tfj";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "PMQTT";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";
    CognitoCachingCredentialsProvider credentialsProvider;
    AWSIotMqttManager mqttManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     //   tvStatus = (TextView) findViewById(R.id.tt);
        //++++++++++++++++++++++++++++++++++ Navigation Drawer +++++++++++++++++++++++++++++++++++++
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


      // try {
            connect1();
       //} catch (AWSIotException e) {
      //     e.printStackTrace();
       // }


     //   switch2 = findViewById(R.id.switch2);
        switch2.setOnClickListener(this);
    }

    public void GetWifiMacAddress()
    {

        WifiManager wifiMan = (WifiManager) this.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddr = wifiInf.getMacAddress();
        String ipAddr = String.valueOf(wifiInf.getIpAddress());
        String ssIdAddr = String.valueOf(wifiInf.getSSID());
        Log.d("WIFI_INFO","MAC Address: "+macAddr+" IP Address: "+ipAddr+" SSID: "+ssIdAddr+" MAC "+getMacAddr());
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public void GetESP32MAC() throws SocketException, UnknownHostException {
        String ipAddress = "192.168.1.100"; // replace with the IP address of your ESP32
        InetAddress inetAddress = InetAddress.getByName("192.168.0.102");
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
        byte[] macBytes = new byte[0];
        try {
            macBytes = networkInterface.getHardwareAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (macBytes != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < macBytes.length; i++) {
                sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
            }
            String macAddress = sb.toString();
            //Toast.makeText(getApplicationContext(),""+macAddress,Toast.LENGTH_LONG);
            Log.d("ranojan mac ID","MAC Address: "+macAddress);
            // use macAddress variable as the MAC address of your ESP32
        } else {
            // unable to get MAC address
        }

    }

    public void connect1()  {

        String clientEndpoint = "a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com";
        String clientId ="iotconsole-54a71cad-495b-4009-8a41-fa7ef1f580db";
        String certificateFile = "/my/path/XXXX-certificate.pem.crt";
        String privateKeyFile = "/my/path/XXXXX-private.pem.key";


        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );


        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, clientEndpoint);

        try {
            mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d("ranojan", "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {
                                tvStatus.setText("Connecting...");

                            } else if (status == AWSIotMqttClientStatus.Connected) {
                                tvStatus.setText("Connected");

                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e("ranojan", "Connection error.", throwable);
                                }
                                tvStatus.setText("Reconnecting");
                            } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e("ranojan", "Connection error.", throwable);
                                    throwable.printStackTrace();
                                }
                                tvStatus.setText("Disconnected");
                            } else {
                                tvStatus.setText("Disconnected");
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e("ranojan", "Connection error.", e);
            tvStatus.setText("Error! " + e.getMessage());
        }
        /*SampleUtil.KeyStorePasswordPair pair =
                SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);

        AWSIotMqttClient mqttclient = new AWSIotMqttClient(clientEndpoint, clientId,
                pair.keyStore, pair.keyPassword);

        mqttclient.connect();*/

        // Create an AWS IoT MQTT Client
    /* AWSIotMqttClient mqttClient = new AWSIotMqttClient(clientEndpoint, clientId, "-----BEGIN CERTIFICATE-----\n" +
                "MIIDWjCCAkKgAwIBAgIVAJPCTZdmpV8nDdfQyKrzfOkhWv8TMA0GCSqGSIb3DQEB\n" +
                "CwUAME0xSzBJBgNVBAsMQkFtYXpvbiBXZWIgU2VydmljZXMgTz1BbWF6b24uY29t\n" +
                "IEluYy4gTD1TZWF0dGxlIFNUPVdhc2hpbmd0b24gQz1VUzAeFw0yMzA0MDgwNTM1\n" +
                "MjZaFw00OTEyMzEyMzU5NTlaMB4xHDAaBgNVBAMME0FXUyBJb1QgQ2VydGlmaWNh\n" +
                "dGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQChcIkxx05vXd5Xk3xi\n" +
                "8lJ9buflwVK50jStsNW5Wz7v+OFLPszzq+tP5Pw/YcvYcCTM9huJpt/ml6arp3wx\n" +
                "diIpU5hKfDxG/qEoirl3VbK0tFcLzKmT5WqjVEuoObQmoIK4xt6ETkHH9+TH/k3g\n" +
                "RslWnLMp6I7MXMuQBAToPCGl4UbElpf1t3WRUYQTdfdHTjDvMrTQLWNbr0PbRQfB\n" +
                "TpV52fjg/UzsiSmnGY845Sl1svk0EDsMSyEyutWrsJrng/k8GnaZBDXvfA2XN2DY\n" +
                "cCsfovPsfFVdgsIHSsvrbpMkOOsyEA2y4qClo8bzQNVF3xXyEd+l4oydOiUqQ8wC\n" +
                "w2rVAgMBAAGjYDBeMB8GA1UdIwQYMBaAFEmnEOqQA7nqONKhDKYCXkiJVUuNMB0G\n" +
                "A1UdDgQWBBSYYycFPW/EVLlWbhbPBMrMbj/08TAMBgNVHRMBAf8EAjAAMA4GA1Ud\n" +
                "DwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAMMXwYTqiPq0Bx5xAVWUqX2zF\n" +
                "vvZg2RG3/8KMB+NOTXvHeM4VKU4zqIT2iyWUkKcCQgrWWDyDpvr/J5HoEiWomN2L\n" +
                "0wrZtm1YxRdbpo4Z0/Q7YFRbWmG/wGby+KY3vggpz41S1dsLWoINDeYOqzmdSBMt\n" +
                "R/TDxussjdVUTeEmXbKZ73uoJXVYU700c3a1R6DHPD3LOnsg4CWgxgEsj3g54lpV\n" +
                "TNA74tpHbi1AggbWtDLogeWlsnc++iHM0drmqa0qnrd7txOFkRQHXWKlkJVww8TH\n" +
                "9ahnlfyP9K0Z0B/k11YTsIKSEX24qvIiveY29UYh64bg/XJGgienQyIWCjsUmA==\n" +
                "-----END CERTIFICATE-----", "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEAoXCJMcdOb13eV5N8YvJSfW7n5cFSudI0rbDVuVs+7/jhSz7M\n" +
                "86vrT+T8P2HL2HAkzPYbiabf5pemq6d8MXYiKVOYSnw8Rv6hKIq5d1WytLRXC8yp\n" +
                "k+Vqo1RLqDm0JqCCuMbehE5Bx/fkx/5N4EbJVpyzKeiOzFzLkAQE6DwhpeFGxJaX\n" +
                "9bd1kVGEE3X3R04w7zK00C1jW69D20UHwU6Vedn44P1M7IkppxmPOOUpdbL5NBA7\n" +
                "DEshMrrVq7Ca54P5PBp2mQQ173wNlzdg2HArH6Lz7HxVXYLCB0rL626TJDjrMhAN\n" +
                "suKgpaPG80DVRd8V8hHfpeKMnTolKkPMAsNq1QIDAQABAoIBADZhbhVyiZ1CBW+C\n" +
                "otfBwL+36C2gnXkyscQAWT4C2oSDVYC/OtKqCq3y+HVxP/U8cWkJTeVkbO+EDgSs\n" +
                "ek0++erp2dbdWoCfrTG26Rqlp3jvdpLm8gh7sxwpfQLBzUllsCMF+lae9dGiU1J6\n" +
                "+0idD505U7C+QbvdVkTA1dZUyxDQ9Mru6Q7FbLuC8SY5MZeZOrKI5eMOcijvCBPX\n" +
                "cq21wgBvz4NANXJGj1MjWf5fEzY/+eTGpVegHIOlK7ElplB9sm3pGwy9qOTv1Ocy\n" +
                "G+1Cjv8Ddm7tO8P04PYyDXK36jrGK2PAmPCWVgBUdAPAdD4fd/KXUOJtaJUebvLE\n" +
                "2G/QjgECgYEAzLTGSKjTOcpaRcTfgOiNvpAVAg+NaY9t5p2kfKaxSi7idSNH09Gg\n" +
                "C3I7JquuglnvR6JZQmJwZN98/44GZYGqVrFU6777sjO0iAEGhGxm0+XsmVB4BWA6\n" +
                "zPNHEW7/Tvg0SxdAiS5zpu0MZWc3TDSW1Lg/Dct4no4OAoD0eVMmBLUCgYEAyeRa\n" +
                "rU2+e1BosadLpSni4mUC+bjrzS63ooHFwvA2mcBM6Q6ngMa4rFuF5G0x/vJaEqUN\n" +
                "FYj/MVueI8jDrij+GzHPaUs0eAOrBRxlDff/jOvZc2XXwF3wvhN4KNPa3nbhEbnp\n" +
                "fD88MzUzL1sYbNXduvaS0HD//2DGgS40tQPWwaECgYEAwiIOiYnSB9RnmBMFA3OI\n" +
                "OVjbE4E8Uwe66iJGhBBxwjCEgyJaU/9REIncnufiL6yqx/ynOdWxUXjBSnqehlVZ\n" +
                "/a1fI9OTT4TJiNGwJJXJTtuWbi9qI28HVKbClz300ieBMFV01qQ++eeFAgXI43Rc\n" +
                "NpAk/Cgi0/tUPfud3hGE1KECgYEAl+5gCsFR0mztjJvQQmfmFOddON5fnVZF3WZ5\n" +
                "k7y/6i6b8lsT1MY3XYW2mfNOx4RMInHRCd7B5LwEovtHvv2cVIzEgIGW56YjAkKf\n" +
                "DccOqlcmmkAO//Xx4Ki4KUldEUM3FubofZb8z7B+Z2nPVMARD8zVKUWQcPe8CqTi\n" +
                "B0LvT2ECgYAO6D6Tw8IxgYpvGoPxjQ97DqQCLEJy0oxnT89Hc10kFpA6jYRgErJa\n" +
                "nnLV/yyfzHDiBDKkW2H/8rLFbVCgeeWBGvPICOTANmZPGHOENsHhxKXNWKoPgjDP\n" +
                "3K4QuHnthgW2gRyqsRsrN3yfHSYdAFhre//o15xuBIwKj/n7l1FmJg==\n" +
                "-----END RSA PRIVATE KEY-----\n");*/


// Connect to AWS IoT Core
     //  mqttClient.connect();
    //    String topic = "test_topic/esp32";
// Subscribe to a topic
        /*mqttClient.subscribe(topic, QOS0, new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                // Handle incoming message
            }
        });*/
 //   String topic1 = "test_topic/esp32";
       // Publish a message to a topic
     //  mqttClient.publish(topic1, String.valueOf(23));

// Disconnect from AWS IoT Core
     //   mqttClient.disconnect();

        // Initialize the AWSIotMqttManager with the configuration
       /* AWSIotMqttManager mqttManager = new AWSIotMqttManager(
                "iotconsole-9d7b6184-5135-4ddb-8f31-f99a098fb5e1",
                "a2w5xcmt7e0hk6-ats.iot.eu-north-1.amazonaws.com");

        mqttManager.connect(KeyStore.getInstance(""), new AWSIotMqttClientStatusCallback() {
            @Override
            public void onStatusChanged(final AWSIotMqttClientStatus status,
                                        final Throwable throwable) {
                Log.d("god_damn", "Status = " + String.valueOf(status));
            }
        });

        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d(LOG_TAG, "Connection Status: " + String.valueOf(status));
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error: ", e);
        }*/

      /*  try {
            String tokenKeyName ="";// <TOKEN_KEY_NAME>;
            String token ="";// <TOKEN>;
            String tokenSignature ="";// <TOKEN_SIGNATURE>;
            String customAuthorizerName ="";// <AUTHORIZER_NAME>;
            mqttManager.connect(
                    tokenKeyName,
                    token,
                    tokenSignature,
                    customAuthorizerName,
                    (status, throwable) -> {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                            }
                        });
                    });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
*/
      /*  try {
            mqttManager.subscribeToTopic("myTopic", AWSIotMqttQos.QOS0
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(LOG_TAG, "Message received: " + message);
                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error: ", e);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error: ", e);
        }
        try {
            mqttManager.publishString("Hello to all subscribers!", "myTopic", AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error: ", e);
        }

        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error: ", e);
        }*/

        /*
        GetWifiMacAddress();

        String clientId ="KBUjEzIRCBsvCgcNCSwfHQ4"; MqttClient.generateClientId();
        final MqttAndroidClient client =  new MqttAndroidClient(getApplicationContext(), "tcp://mqtt3.thingspeak.com:1883", clientId, Ack.AUTO_ACK);
            //    new MqttAndroidClient(this.getApplicationContext(), "tcp://mqtt3.thingspeak.com:1883",//mqtt:// not working > tcp://working
              //          clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setCleanSession(false);
        options.setUserName("KBUjEzIRCBsvCgcNCSwfHQ4");
        options.setPassword("syDJo4Ad/oVb8rjbSAuf8nQE".toCharArray());
        IMqttToken token = client.connect(options);
        //IMqttToken token = client.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // We are connected
                Log.d("file", "onSuccess");
                //publish(client,"payloadd");
                subscribe(client,"channels/2071049/subscribe/fields/field3");
                publish(client, String.valueOf(45));
             //   subscribe(client,"channels/2071049/fields/field1");
                client.setCallback(new MqttCallback() {
                    TextView tt = (TextView) findViewById(R.id.tt);
                    TextView th = (TextView) findViewById(R.id.th);
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.d("file", "connectionLost");
                        Log.d("file 2","some question has been solved");
                        Log.d("file 3","go for dinner system ready for your unbreakable");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.d("file 1", message.toString());

                        if (topic.equals("channels/2071049/subscribe/fields/field3")){
                            tt.setText(message.toString());
                        }

                        if (topic.equals("channels/2071049/subscribe/fields/field3")){
                            th.setText(message.toString());
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });


            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // Something went wrong e.g. connection timeout or firewall problems
                Log.d("file", "onFailure");
                // This for failure to share this heavy duty onFailure to solve the problem to overcome this problem my goodness

            }
        });
        */
    }





    @Override
    public void onRestart()
    {
        super.onRestart();
/*
        try {
            connect1();
        } catch (AWSIotException e) {
            e.printStackTrace();
        }*/


        // finish();
        // startActivity(getIntent());
    }

//    public void printMessege(MqttAndroidClient client){
//        client.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d("file", message.toString());
//
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_credential:
                // Do something
                Intent myIntent = new Intent(getApplicationContext(), WifiActivity.class);
                // myIntent.putExtra("","");
                startActivity(myIntent);
                // Toast.makeText(getApplicationContext(),"My credentials",Toast.LENGTH_SHORT);
                Log.d("click_credential",""+"click credential");
                break;
            case R.id.nav_channe:
                CreateChannel();
                break;

        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void CreateChannel() {
        Log.d("sent_data","Create Channel");

        Thread sendThread = new Thread() {

            public void run() {

                RequestHandler rh = new RequestHandler();
                HashMap<String, String> param = new HashMap<String, String>();
                //----------------------------------------------------------------
                param.put(API_KEY, "ILA15VPFYBECOKAS");
                param.put(CHANNEL_NAME, "XChannel");
              

                //  Log.d("sent_data","id "+key_id+" user_id "+user_id+" device_code "+device_code+" device room "+device_room_name);
                String result=rh.sendPostRequest("https://api.thingspeak.com/channels.json", param);


                try {
                    JSONObject json = new JSONObject(result);
                    runOnUiThread(() -> {
                        try {
                            if(json.getString("query_result").equals("SUCCESS")){
                                Toast.makeText(getApplicationContext(),"Data saved successfully",Toast.LENGTH_LONG).show();
                            }else{

                                Toast.makeText(getApplicationContext(),"Data Saved Failure!",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                  //https://github.com/eurosa/stock-master.git
                    Log.d("sent_data",json.getString("query_result"));

                } catch (Exception e) {
                    Log.d("my_error",""+e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        sendThread.start();

    }

    public void GetChannelList() {

        Log.d("json_iot","");
        Thread sendThread = new Thread() {

            @SuppressLint("ResourceType")
            public void run() {

                OkHttpClientGet example = new OkHttpClientGet();
                String response = null;
                try {
                    response = example.run("https://api.thingspeak.com/channels.json?api_key=ILA15VPFYBECOKAS");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Log.d("GET_INFO", response);
                }catch (Exception ex){}
                // System.out.println(response);

            //    RequestHandler rh = new RequestHandler();
               // HashMap<String, String> param = new HashMap<String, String>();

               // Populate the request parameters
             //    param.put(API_KEY, "ILA15VPFYBECOKAS");
               // param.put(KEY_BILL_ID, "");

               // String result=rh.sendPostRequest(channel_url, param);

                //pDialog.dismiss();



            try {
                 //JSONObject jsonObject = new JSONObject(response);// giving org.json.JSONArray cannot be converted to JSONObject
                if(response!=null) {
                    JSONArray jsonarray = new JSONArray(response);
                    //  JSONObject jsonObject = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));
                    //  Log.d("channel_id",result);
                    //  jsonObject = new JSONObject(response);
                    //  JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String id = jsonobject.getString("id");
                        String name = jsonobject.getString("name");
                        String api_keys = jsonobject.getString("api_keys");

                        JSONArray jsonArray2 = new JSONArray(api_keys);
                        for (int j = 0; j < jsonArray2.length(); j++) {
                            JSONObject jsonobject2 = jsonArray2.getJSONObject(j);
                            String api_key = jsonobject2.getString("api_key");
                            String write_flag = jsonobject2.getString("write_flag");
                            Log.d("ranojan", api_key + " " + write_flag);
                        }

                    }
                }

               } catch (JSONException e) {
                  e.printStackTrace();
                }
           }
  };

        sendThread.start();

    }

    @SuppressLint("StaticFieldLeak")
    protected void fetchJsonByUniqueId(){
        // to show the progressbar
        // Log.d("my_info_id",mm_id);
        // netQuantityPopUp=0;
        // progbar.setVisibility(View.VISIBLE);
        // showSimpleProgressDialog(this, "Loading...","Fetching Json",false);

        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                try {
                    HttpRequest req = new HttpRequest(channel_url);
                    //Populate the request parameters
                    map.put(API_KEY, "ILA15VPFYBECOKAS");
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                // do something with response
                Log.d("my_info",result);
                // onTaskUniqueIdCompleted(result,uniqueJsoncode);
            }
        }.execute();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick(View v) {
        switch (v.getId() /*to get clicked view id**/) {
       //     case R.id.switch2:
                //switch2(v);
          //      break;
         //   default:
               // break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void switch2(View v){
        SwitchButton b = (SwitchButton)v;
        Log.d("ranojan switch click",""+b.isChecked());
            if(b.isChecked()) {
                 switch2.setThumbColorRes(R.color.red);
                 // Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }else{

                switch2.setThumbColorRes(R.color.limeGreen);
            }

    }
}


