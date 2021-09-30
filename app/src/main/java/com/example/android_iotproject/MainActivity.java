package com.example.android_iotproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;
import java.util.UUID;
import java.util.Vector;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.estimote.sdk.Region;


import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.util.UUID;

import static java.util.UUID.fromString;

public class MainActivity extends AppCompatActivity {

    Button stopButton;
    boolean btnState = true;
    String busNum, bell = "off";
    int rssi;

    //비콘 변수
    private TextView beaconCheckText;
    private TextView beaconNameText;

    private BeaconManager beaconManager;
    private Region regionBeacon;

    private TextView dhtHumid;
    private TextView dhtTemp;

    //DynamoDB 변수

    //AWS 추가
    static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a29pxj4x9uc9dt-ats.iot.ap-northeast-2.amazonaws.com";
    private static final String COGNITO_POOL_ID = "ap-northeast-2:f1e3aa02-a85b-4426-ba40-f8aaf69c363a";
    private static final String AWS_IOT_POLICY_NAME = "Esp32_policy";
    private static final Regions MY_REGION = Regions.AP_NORTHEAST_2 ;
    private static final String topic = "$aws/things/BusBellTest/shadow/update";
    private static final String topic2 = "$aws/things/BusBellTest2/shadow/update";


    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";

    private static final String msgden1_on = "{ \"state\": { \"desired\": { \"LED\": \"ON\" } } }";
    private static final String msgden1_off = "{ \"state\": { \"desired\": { \"LED\": \"OFF\" } } }";


    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;

    DynamoDBMapper DHTDynamoDBMapper;

    CognitoCachingCredentialsProvider credentialsProvider;

    /**
     * btnState는 연결된 LED에서 가져온 값
     *  하차벨이 눌러져있으면 true, 안눌러져 있으면 false
     *
     * btnDefault는 안드로이드 내에서 button의 색상을 결정한다
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconManager = new BeaconManager(this);

        setContentView(R.layout.activity_main);

        stopButton = (Button) findViewById(R.id.stopBtn);

        beaconCheckText = (TextView) findViewById(R.id.beaconCheck);
        beaconNameText = (TextView) findViewById(R.id.beaconName);

        //비콘

        dhtHumid = (TextView) findViewById(R.id.tv_humid);
        dhtTemp = (TextView) findViewById(R.id.tv_temp);

        AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider DHTcredentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();

        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(DHTcredentialsProvider);
        DHTDynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();


        beaconManager = new BeaconManager(this);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    Log.e("moon beacon", String.valueOf(nearestBeacon.getRssi()));
                    rssi = nearestBeacon.getRssi();
                    if (nearestBeacon.getRssi() > -50) {
                        stopButton.setText("STOP");
                        stopButton.setEnabled(true);
                        beaconCheckText.setText(busNum);
                        beaconNameText.setText(String.valueOf(nearestBeacon.getRssi()));
                        stopButton.setBackgroundResource(R.drawable.rounded_red);
                        stopButton.setTextColor(Color.WHITE);
                        if(bell.equals("off")){
                            stopButton.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v){
                                    Toast.makeText(getApplicationContext(), "하차 버튼을 눌렀습니다", Toast.LENGTH_SHORT).show();
                                    stopButton.setEnabled(false);
                                    stopButton.setText("버튼 눌림");
                                    stopButton.setTextColor(Color.WHITE);
                                    stopButton.setBackgroundResource(R.drawable.rounded_red);
                                    bell = "on";
                                    subscribe1(topic);
                                    publish(msgden1_on,topic);

                                    new updatetable().execute();
                                }
                            });
                        }
                    } else if (nearestBeacon.getRssi() <= -50) {
                        beaconNameText.setText("인식 버스 없음");
                        beaconCheckText.setText("인식 버스 없음");
                        stopButton.setText("STOP");
                        stopButton.setEnabled(false);
                        stopButton.setBackgroundResource(R.drawable.rounded_base);
                        stopButton.setTextColor(Color.BLACK);

                    }
                }
                new downloadData().execute();
                if(bell.equals("on") && busNum.equals("moon beacon") && rssi > -50){
                    beaconCheckText.setText(busNum);
                    stopButton.setEnabled(false);
                    stopButton.setText("버튼 눌림");
                    stopButton.setBackgroundResource(R.drawable.rounded_red);
                    stopButton.setTextColor(Color.WHITE);
                }
            }


        });

        regionBeacon = new Region("moon beacon", UUID.fromString("1b514C39-1974-83B8-5646-257955565233"),
                null, null);
        busNum = regionBeacon.getIdentifier();


        // AWS MQTT 추가
        clientId = UUID.randomUUID().toString();
        System.out.println("ID#################" + clientId);


        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        com.amazonaws.regions.Region region = com.amazonaws.regions.Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    // btnConnect.setEnabled(true);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //  btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }
        //connect();


        queryDHT();
    };


    @Override
    protected void onResume(){
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(regionBeacon);
            }
        });
    }
    @Override
    protected void onPause() {
        beaconManager.stopRanging(regionBeacon); super.onPause();

    }

    @Override
    protected void onDestroy() {
        beaconManager.stopRanging(regionBeacon); super.onDestroy();

    }


    public class downloadData extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params){

            ManagerClass managerClass = new ManagerClass();
            CognitoCachingCredentialsProvider credentialsProvider = managerClass.getcredentials(MainActivity.this);
            DynamoDBMapper dynamoDBMapper = managerClass.initDynamoClient(credentialsProvider);
            MapperClass item = dynamoDBMapper.load(MapperClass.class,busNum);
            busNum = item.getBusNum();
            bell = item.getBell();
            Log.e("아이템",busNum +" "+ bell);
            return null;
        }
    }

    private class updatetable extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            ManagerClass managerClass = new ManagerClass();
            CognitoCachingCredentialsProvider credentialsProvider = managerClass.getcredentials(MainActivity.this);
            MapperClass mapperClass = new MapperClass();
            mapperClass.setBusNum(busNum);
            mapperClass.setBell(bell);
            Log.e("bell 상태: ",bell);
            if (credentialsProvider != null ) {
                DynamoDBMapper dynamoDBMapper = managerClass.initDynamoClient(credentialsProvider);
                dynamoDBMapper.save(mapperClass);
                Log.e("업로","성공");
            } else {
                return 2;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == 1) {
                Toast.makeText(MainActivity.this, "Update Success", Toast.LENGTH_SHORT);
            } else if (integer == 2) {
                Toast.makeText(MainActivity.this, "Update failed", Toast.LENGTH_SHORT);
            }
        }




    }

    // AWS MQTT 추가

    public void connect()
    {
        Log.d(LOG_TAG, "clientId = " + clientId);

        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            // tvStatus.setText("Error! " + e.getMessage());
        }
    }

    public void subscribe1(String topic)
    {

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);
                                        //Toast.makeText(MainActivity.this, String.valueOf(message.length()), Toast.LENGTH_SHORT).show();
                                        try {
                                            if(message.length()<50)
                                            {
                                                JSONObject root = new JSONObject(message);
                                                JSONObject state1 = root.getJSONObject("state");
                                                JSONObject reported1 = state1.getJSONObject("desired");
                                                String ttden1 = reported1.getString("LED");
                                                Toast.makeText(MainActivity.this, ttden1, Toast.LENGTH_SHORT).show();

                                            }
                                            else
                                            {
                                                JSONObject root = new JSONObject(message);
                                                JSONObject state1 = root.getJSONObject("state");
                                                JSONObject reported1 = state1.getJSONObject("desired");
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //tvLastMessage.setText(message.length());

                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                        //tvStatus.setText("subscribe error");
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }
    public void subscribe2(String topic)
    {

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);
                                        try {
                                            if(message.length()<50)
                                            {
                                                JSONObject root = new JSONObject(message);
                                                JSONObject state1 = root.getJSONObject("state");
                                                JSONObject reported1 = state1.getJSONObject("desired");
                                                String ttden1 = reported1.getString("LED");
                                                Toast.makeText(MainActivity.this, ttden1, Toast.LENGTH_SHORT).show();

                                            }
                                            else
                                            {
                                                JSONObject root = new JSONObject(message);
                                                JSONObject state1 = root.getJSONObject("state");
                                                JSONObject reported1 = state1.getJSONObject("desired");

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //tvLastMessage.setText(message.length());

                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                        //tvStatus.setText("subscribe error");
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }
    public void publish(String msg,String topic)
    {
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }


    public void queryDHT() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DHTtable dht = new DHTtable();
                //dht.setId(7);
                dht.setqr("dht");

                Condition rangeKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.GE)
                        .withAttributeValueList(new AttributeValue().withN("1"));

                Log.d("Main Activity", rangeKeyCondition.toString());
                DynamoDBQueryExpression<DHTtable> queryExpression = new DynamoDBQueryExpression<DHTtable>()
                        .withHashKeyValues(dht)
                        .withRangeKeyCondition("idx",rangeKeyCondition)
                        .withConsistentRead(false)
                        .withScanIndexForward(false);
                //.withLimit(1);

                final PaginatedList<DHTtable> result = DHTDynamoDBMapper.query(DHTtable.class, queryExpression);

                System.out.println("Humid:" +result.get(0).getHumid() + " Temp: " +result.get(0).getTemp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dhtHumid.setText(Integer.toString(result.get(0).getHumid()));
                        dhtTemp.setText(Float.toString(result.get(0).getTemp()));
                    }
                });


            }
        }).start();
    }
}