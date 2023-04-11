package com.myqtt.minpc.mqtt;


//import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

import static org.eclipse.paho.android.service.MqttAndroidClient.*;
import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;
import static java.nio.ByteBuffer.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

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


import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
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
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = (TextView) findViewById(R.id.tt);

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


      // try {
            //connect1();
       // connect1();
       //} catch (AWSIotException e) {
      //     e.printStackTrace();
       // }


        b = (Button)findViewById(R.id.button);
        c = (Button)findViewById(R.id.button2);
        b.setOnClickListener(v -> {
        //   Intent ii = new Intent(MainActivity.this, GraphActivity.class);
            //MainActivity.this.startActivity(ii);
        });

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent ii = new Intent(MainActivity.this,Main3Activity.class);
               // MainActivity.this.startActivity(ii);
            }
        });
        try {
          //  GetESP32MAC();
         //   GetChannelList();
        }catch (Exception ex){}
        try {
          //  fetchJsonByUniqueId();
        }catch (Exception ex)
        {

        }

        switch2 = findViewById(R.id.switch2);
        switch2.setOnClickListener(this);


        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        String clientId = "iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";//"iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";
        String broker = "tcp://a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com:1883";//"tcp://localhost:1883";8883
        String topicName = "topic/test_topic/esp32";
        int qos = 0;


        //try {
           // SubscribeToAWS(broker,clientId,topicName, qos);
        //} catch (MqttException e) {
         //   e.printStackTrace();
      //  }
      /*  try {
             mqttClient = new MqttClient(broker,clientId);

        } catch (MqttException e) {
            e.printStackTrace();
        }*/

        //Mqtt ConnectOptions is used to set the additional features to mqtt message




       // SSLSocketFactory sslSocketFactory = ...
        //options.setSocketFactory(sslSocketFactory);

        try {
            InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
            InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
            InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            options = new MqttConnectOptions();
            options.setCleanSession(true); //no persistent session
            options.setKeepAliveInterval(1000);
           // options.setAutomaticReconnect(true);
            MqttMessage message = new MqttMessage("Ed Sheeran".getBytes());
            message.setQos(qos);     //sets qos level 1
            message.setRetained(true); //sets retained message
            options.setMqttVersion(MQTT_VERSION_3_1);
           // Log.d("mypath", String.valueOf(getPrivateKey()));
            options.setSocketFactory(getSocketFactory(caCrtFile, crtFile, keyFile, ""));
            mqttClient = new MqttClient(broker,clientId);
            // mqttClient = new MqttClient(broker,clientId,new MemoryPersistence());

           // mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), broker, clientId,
             //       new MemoryPersistence(), MqttAndroidClient.Ack.AUTO_ACK);

            mqttClient.connect(options); //connects the broker with connect options

            MqttTopic topic2 = mqttClient.getTopic(topicName);

            topic2.publish(message);    // publishes the message to the topic(test/topic)
        } catch (Exception e) {

            Log.d("Message_sent",e.toString()+" "+e.getCause()+" "+e.getLocalizedMessage());
            e.printStackTrace();
        }

     //++++++===========================================================================================
    }

    public void SubscribeToAWS(String broker,String clientId,String topic, int qos) throws MqttException {
        try{

        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);

            InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
            InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
            InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);


            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true); //no persistent session
           // connectOptions.setKeepAliveInterval(1000);
           // connectOptions.setSocketFactory(getSocketFactory(caCrtFile, crtFile, keyFile, ""));
 ;
        connectOptions.setCleanSession(true);
        Log.d("Connecting to broker: ", broker);
        sampleClient.connect(connectOptions);
        Log.d("Connected","connected");
        sampleClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                try {
                    sampleClient.subscribe(topic, qos);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
       }catch (MqttException me)

            {

                me.printStackTrace();
                me.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
            e.printStackTrace();
        }

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

    public void connect2(){

        // Initialize the AWSIotMqttManager with the configuration
        AWSIotMqttManager mqttManager = new AWSIotMqttManager(
                "iotconsole-54a71cad-495b-4009-8a41-fa7ef1f580db",
                "a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com");

        //AWSMobileClient.getInstance().getIdentityId();

        AttachPolicyRequest attachPolicyReq = new AttachPolicyRequest();
        attachPolicyReq.setPolicyName("ESP32_Test_Policy"); // name of your IOT AWS policy
        attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());
        AWSIotClient mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
        mIotAndroidClient.setRegion(Region.getRegion(Regions.US_EAST_1)); // name of your IoT Region such as "us-east-1"
        mIotAndroidClient.attachPolicy(attachPolicyReq);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        }


    public void connect1()  {

        String clientEndpoint = "a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com";
        String clientId ="iotconsole-54a71cad-495b-4009-8a41-fa7ef1f580db2";
        String certificateFile = "/my/path/XXXX-certificate.pem.crt";
        String privateKeyFile = "/my/path/XXXXX-private.pem.key";


        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        /*

        -----BEGIN PUBLIC KEY-----
MIICITANBgkqhkiG9w0BAQEFAAOCAg4AMIICCQKCAgB+quMAbV3OWhX5NejBopv0
5Zlp4YiaCMYN3nhjWm3TICmRFO1VKJVN8lmvc50iCEougCEK/qF70efDgf5msDHc
s2dj2Ql/VR7mbJc3MI85JDrcMZbJaiDrgU/kXtZqq+CbNSVBJ9XGjsk5Hs8pwe2o
SSNHXFyMv/o3D9sX2gfXePev9BHMzEBBBG5JXNMpjwyFzqm2q2jTc9OYGTqOuI3f
dU4vSK1Xbb2odWvh1pMC08zeDWqzcooyDdzY0YvCJYzEej3igm2pHaKVieB+fgbI
5je4oQAEUvXV7IDb6Yu0ovIkIha33NaBgOq6SCvecOU9nQxrUQc/mW3VrZ7Hpct4
NJUGtqWhY9chUmsjhOdo6dBHB12251iU0Y2Lwc0BVBCwY++S0Upwk++J6Dg71kHV
FAF5Y4BUIRiLDAa2xKetCV0bvGS6Fp8ZPWuN5rOIvAyES7zuSIA5tihCnmTcz04n
VC4djJUyvxaMYSCQWctjmK/MXkkB53LeFw1KTXMugMHZcxm/ZvsrM62XUbCK2Lg5
3RKE8xNK1zqd4LmylzDDTefXF6twx35+TFZdsjKXwwqTRlBieNl2zy/cTn/UACtZ
pD2ZlOwuTRtNKpdxj9iqH2kfNQEZM/9t+MOsUDhmVQXMqX6In3FInbW0q1D7RBDe
1kKiSKzjxs90+wPGoFjO6QIDAQAB
-----END PUBLIC KEY-----

-----BEGIN RSA PRIVATE KEY-----
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
-----END RSA PRIVATE KEY-----
https://cryptotools.net/rsagen
        * */

        /* AttachPolicyRequest attachPolicyReq = new AttachPolicyRequest();
        attachPolicyReq.setPolicyName("ESP32_Test_Policy"); // name of your IOT AWS policy
        attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());
        AWSIotClient mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
        mIotAndroidClient.setRegion(Region.getRegion(Regions.US_EAST_1)); // name of your IoT Region such as "us-east-1"
        mIotAndroidClient.attachPolicy(attachPolicyReq);*/

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
            case R.id.switch2:
                switch2(v);
                break;
            default:
                break;
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


    public  SSLSocketFactory getSocketFactory(InputStream caCrtFile, InputStream crtFile, InputStream keyFile,
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

}


