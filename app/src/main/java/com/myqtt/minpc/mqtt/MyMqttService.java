package com.myqtt.minpc.mqtt;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.rashminpc.mqtttest.R;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;


public class MyMqttService extends IntentService implements MqttCallback, IMqttActionListener {
    public static final String NOTIFICATION = "com.myqtt.minpc.mqtt";
    public static final String RESULT = "result";
    public static  int result = Activity.RESULT_CANCELED;
    private final IBinder binder = new MyBinder();

    private MqttAndroidClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;
    private static final MemoryPersistence persistence = new MemoryPersistence();
    private ArrayList<MqttAndroidClient> lostConnectionClients;


    private boolean isReady = false;
    private boolean doConnectTask = true;
    private boolean isConnectInvoked = false;

    private Handler handler = new Handler();
    private final int RECONNECT_INTERVAL = 10000; // 10 seconds
    private final int DISCONNECT_INTERVAL = 20000; // 20 seconds
    private final int CONNECTION_TIMEOUT = 60;
    private final int KEEP_ALIVE_INTERVAL = 200;
    public static final String MESSAGE = "message";

    String clientId ="ESP32_Test";//"iotconsole-69053fd3-d360-48b5-85ff-236cb1c89718" ;//"ESP32_Test";//""iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";//"iotconsole-be928d1a-3b3e-4370-aaa5-5fb498d652b2";
    String broker = "ssl://a2w5xcmt7e0hk6-ats.iot.us-east-1.amazonaws.com:8883";//"tcp://localhost:1883";8883
    String topic = "test_topic/esp32";

    public MyMqttService() {
        super("MyMqttService");
    }

    public class MyBinder extends Binder {
        public MyMqttService getService() {
            return MyMqttService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        result = Activity.RESULT_OK;
        publishResults("Myjhj",result);

    }
    private void publishResults(String message, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            initMqttClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyMqttService Message:","Destroyed");
        disconnectClients();
        if (isConnectInvoked && mqttClient != null && mqttClient.isConnected()) {
            // unsubscribe here
            unsubscribe(topic);
            mqttClient.disconnect();
        }

        handler.removeCallbacks(connect);
        handler.removeCallbacks(disconnect);
    }

    private void initMqttClient() throws Exception {

        //Log.d("MyMqttService Message:","received");

        if(mqttClient != null) {
            mqttClient = null;
        }

        lostConnectionClients = new ArrayList<>();

        mqttConnectOptions = new MqttConnectOptions();
        InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.aws_root_ca_pem);
        InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.certificate_pem_crt);
        InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.private_pem_key);
        mqttConnectOptions.setSocketFactory(getSocketFactory(caCrtFile, crtFile, keyFile, ""));
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
        mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);

        setNewMqttClient();

        handler.post(connect);
        handler.postDelayed(disconnect, DISCONNECT_INTERVAL);
    }

    private void setNewMqttClient() {
        mqttClient = new MqttAndroidClient(MyMqttService.this, broker, clientId, Ack.AUTO_ACK);
        mqttClient.setCallback(this);
    }

    public Runnable connect = new Runnable() {
        public void run() {
            connectClient();
            handler.postDelayed(connect, RECONNECT_INTERVAL);
        }
    };

    public Runnable disconnect = new Runnable() {
        public void run() {
            disconnectClients();
            handler.postDelayed(disconnect, DISCONNECT_INTERVAL);
        }
    };

    private void connectClient() {
        if(doConnectTask) {
            doConnectTask = false;

            isConnectInvoked = true;
            mqttClient.connect(mqttConnectOptions, null, this);
        }
    }

    private void disconnectClients() {
        if (lostConnectionClients.size() > 0) {
            // Disconnect lost connection clients
            for (MqttAndroidClient client : lostConnectionClients) {
                if (client.isConnected()) {
                    client.disconnect();
                }
            }

            // Close already disconnected clients
            for (int i = lostConnectionClients.size() - 1; i >= 0; i--) {
                try {
                    if (!lostConnectionClients.get(i).isConnected()) {
                        MqttAndroidClient client = lostConnectionClients.get(i);
                        client.close();
                        lostConnectionClients.remove(i);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.e("MyMqttService", e.toString());
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.e("MyMqttService", "deliveryComplete()");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)  {
        // String payload = new String(message.getPayload());
        // do something
        Log.d("MyMqttService Arrived:",message.toString());
        result = Activity.RESULT_OK;
        publishResults(message.toString(),result);

    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e("MyMqttService", cause.getMessage());
       // connectClient();
        //subscribe(topic);
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        isReady = true;

        Log.d("MyMqttService Message:",topic+" "+mqttClient.isConnected());
        // subscribe here
        subscribe(topic);
        Log.d("MyMqttService nection:",topic+" "+mqttClient.isConnected());
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        setNewMqttClient();
        isReady = false;
        doConnectTask = true;
        isConnectInvoked = false;
    }

    private void subscribe(String topic) {
        mqttClient.subscribe(topic, 0);
        isReady = true;
    }

    private void unsubscribe(String topic) {
        mqttClient.unsubscribe(topic);
    }

    private void publish(String topic, String jsonPayload) {
        if(!isReady) {
            return;
        }

        try {
            MqttMessage msg = new MqttMessage();
            msg.setQos(0);
            msg.setPayload(jsonPayload.getBytes("UTF-8"));
            mqttClient.publish(topic, msg);
        } catch (Exception ex) {
            Log.e("MyMqttService", ex.toString());
        }
    }

    private SSLSocketFactory getSocketFactory(InputStream caCrtFile, InputStream crtFile, InputStream keyFile,
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
}
