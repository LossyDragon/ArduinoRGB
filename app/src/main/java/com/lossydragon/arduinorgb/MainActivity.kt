package com.lossydragon.arduinorgb

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lossydragon.arduinorgb.bluetooth.BluetoothComms
import com.lossydragon.arduinorgb.bluetooth.BluetoothConnectAdapter
import com.lossydragon.arduinorgb.bluetooth.BluetoothThread
import com.lossydragon.arduinorgb.bluetooth.Devices
import com.lossydragon.arduinorgb.fragment.FragmentHex
import com.lossydragon.arduinorgb.fragment.FragmentHue
import com.lossydragon.arduinorgb.fragment.FragmentRgb
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, BluetoothConnectAdapter.ConnectCallback {

    private val devicesList = ArrayList<Devices>()
    internal var bluetoothThread: BluetoothThread? = null
    private lateinit var bluetoothComms: BluetoothComms
    private var writeHandler: Handler? = null
    private var btMac: String? = null
    var btName: String? = null
    private var dialog: MaterialDialog? = null
    private var fragmentInt: Int = 0

    private lateinit var prefs: SharedPreferences

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        //Setup crash handler since its not a G play app.
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .apply()

        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(this)
        bluetoothComms = BluetoothComms(this)

        btMac = getSharedPreferences(PREFERENCES, 0).getString(MAC_ADDRESS, "null")
        btName = getSharedPreferences(PREFERENCES, 0).getString(DEVICE_NAME, "null")

        //Menu Options
        btn_menu.setOnClickListener {
            val popup = PopupMenu(this@MainActivity, btn_menu)
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
        switch_onOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (btMac != null || btMac != "null") {
                    startBluetoothThread(btMac)
                } else {
                    switch_onOff.isChecked = false
                    Toast.makeText(applicationContext, R.string.toastNoPair, Toast.LENGTH_LONG).show()
                }
            } else {
                if (bluetoothThread != null) {
                    bluetoothThread!!.interrupt()
                    bluetoothThread = null
                }
            }
        }

        if (BuildConfig.DEBUG)
            Log.v(TAG, "Created...")
    }

    @SuppressLint("PrivateResource")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        when (item.itemId) {
            R.id.navigation_rgb -> {
                transaction.replace(R.id.content, FragmentRgb()).commit()
                fragmentInt = 0
                return true
            }
            R.id.navigation_hex -> {
                transaction.replace(R.id.content, FragmentHex()).commit()
                fragmentInt = 1
                return true
            }
            R.id.navigation_hue -> {
                transaction.replace(R.id.content, FragmentHue()).commit()
                fragmentInt = 2
                return true
            }
        }
        return false
    }

    //Function to handle when Menu Connect is pressed.
    private fun listDevices() {

        devicesList.clear()
        if (bluetoothComms.pairedDevices.isNotEmpty()) {
            for (device in bluetoothComms.pairedDevices) {
                devicesList.add(
                        Devices(device.name, device.address)
                )
            }
        }

        //Custom dialog and pair on callback
        dialog = MaterialDialog(this)
                .title(R.string.connectTo)
                .customListAdapter(
                        BluetoothConnectAdapter(this, devicesList)
                )

        dialog?.show()
    }

    override fun onCallback(string: String) {
        pairDevice(string)
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
        if (bluetoothThread != null || macAddress == null)
            return

        bluetoothThread = BluetoothThread(macAddress, object : Handler() {

            override fun handleMessage(message: Message) {
                when (val s = message.obj) {
                    "CONNECTED" -> {
                        toolbar_title.text = getString(R.string.status_connected, btName)
                        switch_onOff.isChecked = true
                    }
                    "DISCONNECTED" -> {
                        toolbar_title.setText(R.string.status_not_connected)
                        switch_onOff.isChecked = false
                    }
                    "CONNECTION FAILED" -> {
                        toolbar_title.setText(R.string.status_failed)
                        switch_onOff.isChecked = false
                        bluetoothThread = null
                    }
                    else -> {
                        Log.v(TAG, s.toString())
                        toolbar_status.text = getString(R.string.text_RX, s)
                    }
                }
            }
        })

        writeHandler = bluetoothThread!!.writeHandler
        bluetoothThread!!.start()
        toolbar_title.setText(R.string.status_connecting)
    }

    @SuppressLint("PrivateResource")
    public override fun onResume() {
        super.onResume()
        val transaction = supportFragmentManager.beginTransaction()
        val prefs = getSharedPreferences(PREFERENCES, 0)
        this.navigation.menu.getItem(prefs.getInt(LAST_FRAGMENT, 0)).isChecked = true

        when (prefs.getInt(LAST_FRAGMENT, 0)) {
            0 -> transaction.replace(R.id.content, FragmentRgb.newInstance()).commit()
            1 -> transaction.replace(R.id.content, FragmentHex.newInstance()).commit()
            2 -> transaction.replace(R.id.content, FragmentHue.newInstance()).commit()
        }

        requestBluetooth()

        if (prefs.getBoolean(PreferenceActivity.PREF_RECONNECT, true)) {
            switch_onOff.isChecked = true
            startBluetoothThread(btMac)
        }
    }

    public override fun onPause() {
        super.onPause()

        //Save last fragment app was on.
        val editor = getSharedPreferences(PREFERENCES, 0).edit()
        editor.putInt(LAST_FRAGMENT, fragmentInt)
        editor.apply()

        interruptBluetooth()
    }

    private fun requestBluetooth() {
        if (!this.bluetoothComms.isEnabled)
            this.bluetoothComms.requestBluetooth()
    }

    private fun interruptBluetooth() {
        //Kill the thread if not null
        bluetoothThread?.interrupt()
        bluetoothThread = null

        //Set actionbar switch to off
        switch_onOff.isChecked = false
    }

    //Write to the Bluetooth module
    fun writeRgb(v: IntArray) {
        //R, G, B, L
        val data = "R" + v[0] + "G" + v[1] + "B" + v[2] + "L" + v[3]

        val msg = Message.obtain()
        msg.obj = data

        writeHandler?.sendMessage(msg)
    }

    companion object {
        private const val TAG = "MainActivity"
        const val PREFERENCES = "com.lossydragon.arduinorgb.preferences"
        const val LAST_FRAGMENT = "last_fragment"
        const val MAC_ADDRESS = "device_mac"
        const val DEVICE_NAME = "device_name"
    }
}
