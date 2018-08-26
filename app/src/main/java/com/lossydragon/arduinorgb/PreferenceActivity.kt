package com.lossydragon.arduinorgb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Switch
import butterknife.BindView
import butterknife.ButterKnife


class PreferenceActivity : AppCompatActivity() {

    @BindView(R.id.pref_switch_autoConnect) lateinit var autoSwitch: Switch

    companion object {
        private const val TAG = "PreferenceActivity"
        const val PREFERENCES = "com.lossydragon.arduinorgb.preferences"
        const val AUTO_CONNECT = "auto_connect"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        ButterKnife.bind(this)

        if(supportActionBar != null)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        Log.i(TAG, "Opened")

        val prefs = getSharedPreferences(PREFERENCES, 0)
        val auto = prefs.getInt(AUTO_CONNECT, 1)

        autoSwitch.isChecked = auto != 0

        autoSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs2 = getSharedPreferences(PREFERENCES, 0).edit()

            if (isChecked) {
                prefs2.putInt(AUTO_CONNECT, 1)
                //Toast.makeText(this, "Auto Connect enabled.", Toast.LENGTH_LONG).show()
            }else {
                prefs2.putInt(AUTO_CONNECT, 0)
                //Toast.makeText(this, "Auto Connect disabled.", Toast.LENGTH_LONG).show()
            }

            prefs2.apply()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}