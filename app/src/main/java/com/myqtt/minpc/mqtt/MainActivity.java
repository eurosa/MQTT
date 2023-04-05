package com.myqtt.minpc.mqtt;

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

import com.example.rashminpc.mqtttest.R;
import com.google.android.material.navigation.NavigationView;
import com.kyleduo.switchbutton.SwitchButton;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    private static final String CHANNEL_NAME = "name";
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button b ;
    Button c;
    private  String API_KEY ="api_key" ;
    private String channel_url = "https://api.thingspeak.com/channels.json";
    private SwitchButton switch2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        connect();

        b = (Button)findViewById(R.id.button);
        c = (Button)findViewById(R.id.button2);
        b.setOnClickListener(v -> {
     //       Intent ii = new Intent(MainActivity.this, GraphActivity.class);
            //MainActivity.this.startActivity(ii);
        });

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(MainActivity.this,Main3Activity.class);
                MainActivity.this.startActivity(ii);
            }
        });
        try {
            GetESP32MAC();
            GetChannelList();
        }catch (Exception ex){}
        try {
            fetchJsonByUniqueId();
        }catch (Exception ex)
        {

        }

        switch2 = findViewById(R.id.switch2);
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

    public void connect(){

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
    }

    public void publish(MqttAndroidClient client, String payload){
        String topic = "channels/2071049/publish/fields/field3";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(MqttAndroidClient client , String topic){
        int qos = 1;
        IMqttToken subToken = client.subscribe(topic, qos);
        subToken.setActionCallback(new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // The message was published
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // Authorized to subscribe on the specified topic e.g. using wildcards
            }
        });
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        connect();
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
}


