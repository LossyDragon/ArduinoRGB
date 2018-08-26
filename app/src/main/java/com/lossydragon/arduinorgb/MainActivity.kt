package com.lossydragon.arduinorgb

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.lossydragon.arduinorgb.bluetooth.BluetoothComms
import com.lossydragon.arduinorgb.bluetooth.BluetoothConnectAdapter
import com.lossydragon.arduinorgb.bluetooth.BluetoothThread
import com.lossydragon.arduinorgb.bluetooth.Devices
import com.lossydragon.arduinorgb.fragment.FragmentHex
import com.lossydragon.arduinorgb.fragment.FragmentHue
import com.lossydragon.arduinorgb.fragment.FragmentRgb

import butterknife.BindView
import butterknife.ButterKnife

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.btn_menu) lateinit var menuButton: ImageButton
    @BindView(R.id.toolbar_title) lateinit var toolBarTitle: TextView
    @BindView(R.id.toolbar_status) lateinit var toolbarStatus: TextView
    @BindView(R.id.switch_onOff) lateinit var menuSwitch: Switch
    @BindView(R.id.content) lateinit var content: FrameLayout
    @BindView(R.id.navigation) lateinit var navigation: BottomNavigationView

    private val devicesList = ArrayList<Devices>()
    internal var bluetoothThread: BluetoothThread? = null
    private lateinit var bluetoothComms: BluetoothComms
    private var writeHandler: Handler? = null
    private var btMac: String? = null
    var btName: String? = null
    private var dialog: MaterialDialog? = null
    private var fragmentInt: Int = 0

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)
        navigation.setOnNavigationItemSelectedListener(this)
        bluetoothComms = BluetoothComms(this)

        btMac = getSharedPreferences(PREFERENCES, 0).getString(MAC_ADDRESS, "null")
        btName = getSharedPreferences(PREFERENCES, 0).getString(DEVICE_NAME, "null")

        //Menu Options
        menuButton.setOnClickListener {
            val popup = PopupMenu(this@MainActivity, menuButton)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_connect -> listDevices()
                    R.id.menu_settings -> startActivity(Intent(this, PreferenceActivity::class.java))
                }
                true
            }
        }

        //Menu on/off Switch
        menuSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (btMac != null || btMac != "null") {
                    startBluetoothThread(btMac)
                } else {
                    menuSwitch.isChecked = false
                    Toast.makeText(applicationContext, R.string.toastNoPair, Toast.LENGTH_LONG).show()
                }
            } else {
                if(bluetoothThread!!.isConnected)
                    interruptBluetooth()
            }
        }

        if (BuildConfig.DEBUG)
            Log.v(TAG, "Created...")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.navigation_rgb -> {
                transaction.replace(R.id.content, FragmentRgb()).commit()
                return true
            }
            R.id.navigation_hex -> {
                transaction.replace(R.id.content, FragmentHex()).commit()
                return true
            }
            R.id.navigation_hue -> {
                transaction.replace(R.id.content, FragmentHue()).commit()
                return true
            }
        }
        return false
    }

    //Function to handle when Menu Connect is pressed.
    private fun listDevices() {
        val adapter = BluetoothConnectAdapter(devicesList)

        devicesList.clear()
        val pairedDevices = bluetoothComms.pairedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val devices = Devices(device.name, device.address)
                devicesList.add(devices)
            }
        }

        //Custom dialog and pair on callback
        dialog = MaterialDialog(this)
                .title(R.string.connectTo)
                .customListAdapter(adapter)

        adapter.setCallbacks(this::pairDevice)
        dialog?.show()
    }

    private fun pairDevice(deviceInfo: String) {
        //Make sure there are no BT connection already
        interruptBluetooth()

        val info = deviceInfo.split(",").toTypedArray()
        val editor = getSharedPreferences(PREFERENCES, 0).edit()
        editor.putString(MAC_ADDRESS, info[0])
        editor.putString(DEVICE_NAME, info[1])
        btName = info[1]
        editor.apply()

        //Connect to BT if not connected.
        startBluetoothThread(info[0])

        dialog?.dismiss()
    }

    @SuppressLint("HandlerLeak")
    private fun startBluetoothThread(macAddress: String?) {
        if (bluetoothThread != null || macAddress == null) { return }

        bluetoothThread = BluetoothThread(macAddress, object : Handler() {

            override fun handleMessage(message: Message) {
                val s = message.obj as String
                when (s) {
                    "CONNECTED" -> {
                        toolBarTitle.text = getString(R.string.status_connected, btName)
                        menuSwitch.isChecked = true
                    }
                    "DISCONNECTED" -> {
                        toolBarTitle.setText(R.string.status_not_connected)
                        menuSwitch.isChecked = false
                    }
                    "CONNECTION FAILED" -> {
                        toolBarTitle.setText(R.string.status_failed)
                        menuSwitch.isChecked = false
                        bluetoothThread = null
                    }
                    else -> {
                        Log.v(TAG, s)
                        toolbarStatus.text = getString(R.string.text_RX, s)
                    }
                }
            }
        })

        writeHandler = bluetoothThread!!.writeHandler
        bluetoothThread!!.start()
        toolBarTitle.setText(R.string.status_connecting)
    }

    @SuppressLint("PrivateResource")
    public override fun onResume() {
        super.onResume()
        var selectedFragment: Fragment? = null
        val prefs = getSharedPreferences(PREFERENCES, 0)
        this.navigation.menu.getItem(prefs.getInt(LAST_FRAGMENT, 0)).isChecked = true

        when(prefs.getInt(LAST_FRAGMENT, 0)){
            0 -> selectedFragment = FragmentRgb.newInstance()
            1 -> selectedFragment = FragmentHex.newInstance()
            2 -> selectedFragment = FragmentHue.newInstance()
        }

        if(prefs.getInt(PreferenceActivity.AUTO_CONNECT, 0) == 1) {
            menuSwitch.isChecked = true
            startBluetoothThread(btMac)
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
        transaction.replace(R.id.content, selectedFragment!!)
        transaction.commit()

        requestBluetooth()
    }

    public override fun onPause() {
        super.onPause()
        val editor = getSharedPreferences(PREFERENCES, 0).edit()
        editor.putInt(LAST_FRAGMENT, fragmentInt)
        editor.apply()

        interruptBluetooth()
    }

    private fun requestBluetooth() {
        if (!this.bluetoothComms.isEnabled) {
            this.bluetoothComms.requestBluetooth()
        }
    }

    private fun interruptBluetooth() {
        //Kill the thread if not null
        if (bluetoothThread != null) {
            bluetoothThread!!.interrupt()
            bluetoothThread = null
        }

        //Set actionbar switch to off
        menuSwitch.isChecked = false
    }

    //Write to the Bluetooth module
    fun writeRgb(v: IntArray) {
        //val data = v.joinToString(","){it.toString()}
        //R, G, B, L
        val data = "R" + v[0] + "G" + v[1] + "B" + v[2] + "L" + v[3]

        val msg = Message.obtain()
        msg.obj = data

        if (writeHandler != null)
            writeHandler!!.sendMessage(msg)
    }

    companion object {
        private const val TAG = "MainActivity"
        const val PREFERENCES = "com.lossydragon.arduinorgb.preferences"
        const val LAST_FRAGMENT = "last_fragment"
        const val MAC_ADDRESS = "device_mac"
        const val DEVICE_NAME = "device_name"
    }
}
