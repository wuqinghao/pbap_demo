package com.wzb.btphone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.socks.library.KLog;
import com.wzb.btphone.service.BluetoothService;
import com.wzb.btphone.util.LogUtil;
import com.wzb.btphone.view.DeviceListActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "wzb";
    public static final int CHOOSE_DEVICE = 1;

    public static BluetoothAdapter mBluetoothAdapter;
    public static boolean IS_BLUETOOTH_ENABLE = false;
    public static BluetoothPbapClient sClient;

    public static BluetoothService sService;

    public static String sDeviceAddress = "";

    @Bind(R.id.bluetooth_device_chosen)
    public TextView mDeviceChosen;

    public Button btnSocket,btnClient,btnPullBook;
    public TextView phoneMsg;
    public MyReceiver myReceiver=null;
    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d("action="+action);
            if(action.equals("libx.wzb.phonemsg")) {
                Bundle bundle = intent.getExtras();
                String msg = bundle.getString("msg");
                save_vcard(msg);
                phoneMsg.setText(msg);
            }else if(action.equals("libx.wzb.clientbutton")){
                btnClient.setEnabled(true);
                phoneMsg.setText("连接成功");
            }else if(action.equals("libx.wzb.pullphonebook")){
                btnPullBook.setEnabled(true);
                phoneMsg.setText("初始化成功");
            }else if(action.equals("libx.wzb.connectfail")){
                phoneMsg.setText("连接失败");
            }else if(action.equals("libx.wzb.clientfail")){
                phoneMsg.setText("初始化失败");
            }
        }


    }

    private void save_vcard(String vcardString){
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                OutputStreamWriter writer;
                File file = new File(Environment.getExternalStorageDirectory(),"example.vcf");
                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                writer.write(vcardString);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        phoneMsg=(TextView)findViewById(R.id.msg);
        btnClient=(Button)findViewById(R.id.btn_establish_pbap);
        btnSocket=(Button)findViewById(R.id.btn_establish_socket);
        btnPullBook=(Button)findViewById(R.id.btn_pull_phone_book);

        btnClient.setEnabled(false);
        btnPullBook.setEnabled(false);
        myReceiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("libx.wzb.phonemsg");
        filter.addAction("libx.wzb.clientbutton");
        filter.addAction("libx.wzb.pullphonebook");
        filter.addAction("libx.wzb.connectfail");
        filter.addAction("libx.wzb.clientfail");
        MainActivity.this.registerReceiver(myReceiver,filter);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        ButterKnife.bind(this);

        Intent serviceIntent = new Intent(this, BluetoothService.class);
//        startService(service);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("onServiceConnected, init the sService");
                sService = ((BluetoothService.BluetoothServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                sService = null;
            }
        };
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        KLog.d("on Resume ");
//        if (mBluetoothAdapter == null) {
//            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        }
//
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.cancelDiscovery();
//            IS_BLUETOOTH_ENABLE = true;
//            KLog.d("Bluetooth is enabled !");
//
//            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//            KLog.d("---------------------");
//            KLog.d("here is the devices found");
//            KLog.d(devices);
//            KLog.d("---------------------");
//            BluetoothDevice device = (BluetoothDevice) devices.toArray()[1];
////            UUID my_uuid = UUID.fromString("0000112f-0000-1000-8000-00805f9b34fb");
//            UUID my_uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//            UUID uuid = device.getUuids()[0].getUuid();
//            KLog.d("uuid is : " + uuid);
//
//            sClient = new BluetoothPbapClient(device, sHandler);
//            sClient.connect();
//            sClient.pullPhoneBook(BluetoothPbapClient.PB_PATH);
//
//            KLog.d("debug_ang", sClient.getState());
//        } else {
//            IS_BLUETOOTH_ENABLE = false;
//            KLog.d("Bluetooth is disabled !");
//
//            Intent startBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(startBluetoothIntent, 1001); //1001 = BT OPEN
//        }

//        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        if (sClient != null && sClient.getState() == BluetoothPbapClient.ConnectionState.CONNECTED) {
            sClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_DEVICE: {
                if (resultCode == RESULT_OK) {
                    KLog.d(TAG, "result_ok for choose device");
                    if (sService == null) {
                        return;
                    } else {
                        String info = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_INFO);
                        mDeviceChosen.setText(info);
                        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        LogUtil.d("address is : " + address);
                        sDeviceAddress = address;
//                        sService.connect(address);
                    }

                }
            }
            break;
            default: {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_choose_device)
    public void onChooseDevice() {
        Intent chooseDevice = new Intent(this, DeviceListActivity.class);
        startActivityForResult(chooseDevice, CHOOSE_DEVICE);
    }

    @OnClick(R.id.btn_pull_phone_book)
    public void onPullPhoneBook() {
        if (sService != null) {
            sService.getPhoneBook();
        }
    }

    @OnClick(R.id.btn_place_call)
    public void onPlaceCall() {
        sService.placeCall();
    }


    @OnClick(R.id.btn_establish_pbap)
    public void onEstablishPbap() {
        phoneMsg.setText("初始化...");
        sService.establishPbap(sDeviceAddress);

    }

    @OnClick(R.id.btn_establish_socket)
    public void onEstablishSocket() {
        phoneMsg.setText("连接中...");
        sService.establishSocket(sDeviceAddress);

    }


    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
//    private void connectDevice(Intent data, boolean secure) {
//        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mChatService.connect(device, secure);
//    }
}

