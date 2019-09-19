package com.lossydragon.arduinorgb

import android.os.Bundle
import android.view.MenuItem
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

class PreferencesFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        if (arguments != null) {
            setPreferencesFromResource(R.xml.preferences, arguments!!.getString("rootKey"))
        } else {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        val applicationPreferencesFragment = PreferencesFragment()
        val args = Bundle()
        args.putString("rootKey", preferenceScreen.key)
        applicationPreferencesFragment.arguments = args
        fragmentManager!!
                .beginTransaction()
                .replace(id, applicationPreferencesFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (fragmentManager!!.backStackEntryCount > 0) {
                    fragmentManager?.popBackStack()
                } else {
                    activity?.onBackPressed()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
