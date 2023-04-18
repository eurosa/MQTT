package com.myqtt.minpc.mqtt;


//import static com.github.mikephil.charting.charts.Chart.LOG_TAG;


import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;
import static java.nio.ByteBuffer.*;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



//import com.amazonaws.services.iot.client.AWSIotException;
//import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
//import com.amazonaws.services.iot.client.AWSIotException;
//import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetIdRequest;
import com.amazonaws.services.cognitoidentity.model.GetIdResult;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.util.IOUtils;
import com.example.rashminpc.mqtttest.R;
import com.google.android.material.navigation.NavigationView;
import com.kyleduo.switchbutton.SwitchButton;
import com.myqtt.minpc.mqtt.helpers.MqttHelper;


import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.CollationElementIterator;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    private static final String CHANNEL_NAME = "name";
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button b ;
    Button c;
    private  String API_KEY ="api_key" ;
    private String channel_url = "https://api.thingspeak.com/channels.json";

    TextView tvStatus;

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID ="us-east-1:bb5680ba-e40d-4454-be3c-ddaa5231eb85";//"us-east-1:d1a86c61-c800-4838-9033-43d4a49c7ae5";// "us-west-2_iQ1FI8tfj";
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
    private MqttClient mqttClient;

    private MqttConnectOptions options;
    private MqttAndroidClient mqttAndroidClient;
    Button startButton;

    String clientId ="ESP32_Test";//"iotconsole-69053fd3-d360-48b5-85ff-236cb1c89718" ;//"ESP32_Test";//""iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";//"iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";
    String broker = "ssl://a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com:8883";//"tcp://localhost:1883";8883
    String topicName = "test_topic/esp32";
    int qos =0;
    private InputStream caCrtFile;
    private InputStream crtFile;
    private InputStream keyFile;
    private MqttHelper mqttHelper;
    private TextView voltage;
    private Timer myTimer;


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            // Toast.makeText(MainActivity.this,
              //       " "+MyMqttService.result   ,
             //     Toast.LENGTH_LONG).show();
            if (bundle != null) {
                String string = bundle.getString(MyMqttService.MESSAGE);
                int resultCode = bundle.getInt(MyMqttService.RESULT);
                if (resultCode == RESULT_OK) {

                    voltage.setText(string);
                }
                //else {
                    //Toast.makeText(MainActivity.this, "Download failed",
                   //         Toast.LENGTH_LONG).show();
                 //   voltage.setText("Download failed");
               // }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton =   findViewById(R.id.startBtn);
        voltage = findViewById(R.id.voltage);
        // [cloudshell-user@ip-10-2-125-33 ~]$ aws iot attach-principal-policy --policy-name 'ESP32_Test_Policy' --principal 'us-east-1:d1a86c61-c800-4838-9033-43d4a49c7ae5'
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

         startButton.setOnClickListener(this);
         caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
         crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
         keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);


        //++++++===========================================================================================
        try {
          SubscribeToAWS();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

       startService(new Intent(this, MyMqttService.class));
    }


    public static PrivateKey parsePrivateKey(String privateKey) throws Exception {

        StringBuilder sb = new StringBuilder();

        sb.append("-----BEGIN PRIVATE KEY-----\n");
        sb.append(privateKey);/* www  .  j a v a  2  s .c o m*/

        if (!privateKey.endsWith("\n")) {
            sb.append("\n");
        }

        sb.append("-----END PRIVATE KEY-----");

        PEMParser pemParser = new PEMParser(new StringReader(sb.toString()));

        JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();

        return jcaPEMKeyConverter.getPrivateKey((PrivateKeyInfo) pemParser.readObject());
    }

    public static PrivateKey loadKey(String file) throws IOException {
        PEMParser parser = new PEMParser(new FileReader(file));
        try {/*w ww  .jav  a2 s. c om*/
            PEMKeyPair pemObject = (PEMKeyPair) parser.readObject();
            PrivateKeyInfo info = pemObject.getPrivateKeyInfo();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(info);
        } finally {
            //IOUtils.closeQuietly(parser);
        }
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



    @Override
    public void onRestart()
    {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                MyMqttService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

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

            try {
                 // JSONObject jsonObject = new JSONObject(response);// giving org.json.JSONArray cannot be converted to JSONObject
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
            case R.id.startBtn:
                switch2(v);
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void switch2(View v){
        //  SwitchButton b = (SwitchButton)v;
        //   Log.d("ranojan switch click",""+b.isChecked());
        //  if(b.isChecked()) {
        //      switch2.setThumbColorRes(R.color.red);
        // Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
        // }else{

        //  switch2.setThumbColorRes(R.color.limeGreen);

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        String clientId ="ESP32_Test";//"iotconsole-69053fd3-d360-48b5-85ff-236cb1c89718" ;//"ESP32_Test";//""iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";//"iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";
        String broker = "ssl://a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com:8883";//"tcp://localhost:1883";8883
        String topicName = "test_topic/esp32";
        int qos =0;


        try {
            InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
            InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
            InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            options = new MqttConnectOptions();
            options.setCleanSession(true); //no persistent session
            options.setKeepAliveInterval(1000);
            options.setConnectionTimeout(10000);
            // options.setAutomaticReconnect(true);
            MqttMessage message = new MqttMessage("Hi".getBytes());
            message.setQos(qos);     //sets qos level 1

            options.setSocketFactory(getSocketFactory(caCrtFile, crtFile, keyFile, ""));
            // mqttClient = new MqttClient(broker,clientId);
            mqttClient = new MqttClient(broker,clientId,new MemoryPersistence());
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        mqttClient.connect(options); //connects the broker with connect options

                        mqttClient.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable me) {
                                Log.d("ranojan","Connection lost");

                            }

                            @Override
                            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                Log.d("ranojan","message Arrived");
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                                Log.d("ranojan","deliverd--------");
                                try {
                                    MqttDeliveryToken token  = (MqttDeliveryToken) iMqttDeliveryToken;
                                    String h = token.getMessage().toString();
                                    Log.d("ranojan","delivered message :"+h);

                                } catch (MqttException me) {

                                }
                            }
                        });

                        mqttClient.publish(topicName, message);
                        MqttTopic topic2 = mqttClient.getTopic(topicName);
                      //  topic2.publish(message);    // publishes the message to the topic(test/topic)

                        Log.d("Message_sent","Is Connected: "+mqttClient.isConnected());
                      //  mqttClient.subscribe("test_topic/esp32");
                        //  mqttClient.publish("topic/test_topic/esp32", "Hello, World!".getBytes(), 0, false );

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            //mqttClient.disconnect();
        } catch (Exception e) {

            Log.d("Message_sent",e.toString()+" "+e.getCause()+" "+e.getLocalizedMessage());
            e.printStackTrace();
        }



    }


    @SuppressLint("SuspiciousIndentation")
    public void SubscribeToAWS() {
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        String clientId ="ESP32_Test";//"iotconsole-69053fd3-d360-48b5-85ff-236cb1c89718" ;//"ESP32_Test";//""iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";//"iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";
        String broker = "ssl://a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com:8883";//"tcp://localhost:1883";8883
        String topicName = "test_topic/esp32";
        int qos =0;


        try {
            InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
            InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
            InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            options = new MqttConnectOptions();
            options.setCleanSession(true); //no persistent session
            options.setKeepAliveInterval(1000);
            options.setConnectionTimeout(10000);
            // options.setAutomaticReconnect(true);
            MqttMessage message = new MqttMessage("Ed Sheeran".getBytes());
            message.setQos(qos);     //sets qos level 1

            options.setSocketFactory(getSocketFactory(caCrtFile, crtFile, keyFile, ""));
            // mqttClient = new MqttClient(broker,clientId);
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),broker,clientId, Ack.AUTO_ACK);
         //   Thread thread = new Thread() {
              //  @Override
          //      public void run() {

                    mqttAndroidClient.connect(options); //connects the broker with connect options
                    mqttAndroidClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable me) {
                            Log.d("ranojan","Connection lost message arrived");
                            mqttAndroidClient.connect(options); //connects the broker with connect options
                            mqttAndroidClient.subscribe("test_topic/esp32",0);
                        }

                        @Override
                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                            Log.d("ranojan","message Arrived"+mqttMessage.toString());
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                            Log.d("ranojan","arrived--------");
                            try {
                                MqttDeliveryToken token  = (MqttDeliveryToken) iMqttDeliveryToken;
                                String h = token.getMessage().toString();
                                Log.d("ranojan","arrived message :"+h);

                            } catch (MqttException me) {

                            }
                        }
                    });

                    //    mqttClient.publish(topicName, message);
                    // MqttTopic topic2 = mqttClient.getTopic(topicName);
                    //  topic2.publish(message);    // publishes the message to the topic(test/topic)


                    mqttAndroidClient.subscribe("test_topic/esp32",0);
                    Log.d("Message_sent","Is Connected: "+mqttAndroidClient.isConnected());

                    //  mqttClient.publish("topic/test_topic/esp32", "Hello, World!".getBytes(), 0, false );

               // }
           // };
          //  thread.start();
            //mqttClient.disconnect();
        } catch (Exception e) {

            Log.d("Message_sent",e.toString()+" "+e.getCause()+" "+e.getLocalizedMessage());
            e.printStackTrace();
        }


    }

    private  SSLSocketFactory getSocketFactory(InputStream caCrtFile, InputStream crtFile, InputStream keyFile,
                                                    String password) throws Exception {
       // Security.addProvider(new BouncyCastleProvider());
        Security.removeProvider("BC");
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
        // load CA certificate
        X509Certificate caCert = null;

        BufferedInputStream bis = new BufferedInputStream(caCrtFile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }

        // load client certificate
        bis = new BufferedInputStream(crtFile);
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
        }

        // load client private cert
        PEMParser pemParser = new PEMParser(new InputStreamReader(keyFile));
        Object object = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        KeyPair key = converter.getKeyPair((PEMKeyPair) object);

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("cert-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-cert", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    public PrivateKey loadPrivateKey(InputStream privateKeyIn) throws IOException, GeneralSecurityException {

        //need the full file - org.apache.commons.io.IOUtils is handy
        byte[] fullFileAsBytes = IOUtils.toByteArray( privateKeyIn );
        //remember this is supposed to be a text source with the BEGIN/END and base64 in the middle of the file
        String fullFileAsString = new String(fullFileAsBytes);
      //  Log.d("Message_sent encoded",fullFileAsString);
        //nifty regular expression to extract out between BEGIN/END
        Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        String encoded = parse.matcher(fullFileAsString).replaceFirst("$1");

        //decode the Base64 string
        byte[] keyDecoded = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            keyDecoded = Base64.getMimeDecoder().decode(encoded);
        }

        //for my example, the source is in common PKCS#8 format
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyDecoded);

        //from there we can use the KeyFactor to generate
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }
    public  PrivateKey getPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        File file = new File("android.resource://" + getPackageName() + "/" + R.raw.private_pem_key);
        try (FileReader keyReader = new FileReader(file); PemReader pemReader = new PemReader(keyReader)) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(content);
            PrivateKey privateKey = factory.generatePrivate(privateKeySpec);
            return privateKey;
        }
    }

    public PrivateKey getPrivateKeyObject(InputStream privateKey)
    {
        PrivateKey privateKeyObject = null;
        byte[] privateKeyBytes;
        try {
            privateKeyBytes = IOUtils.toByteArray(privateKey);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKeyObject = keyFactory.generatePrivate(privateKeySpec);
        } catch(Exception e){
            e.printStackTrace();
        }
        return privateKeyObject;
    }

    public void AWSIOTSubscribe() throws MqttException {


//Override methods from MqttCallback interface

    }

    private void startMqtt() throws Exception {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d("ranojan mqttservice",mqttMessage.toString());
              ///  voltage.setText(mqttMessage.toString());
               // mChart.addEntry(Float.valueOf(mqttMessage.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

}


