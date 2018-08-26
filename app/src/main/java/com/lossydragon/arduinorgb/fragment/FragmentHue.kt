package com.lossydragon.arduinorgb.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lossydragon.arduinorgb.R


class FragmentHue : Fragment() {

    companion object {
        fun newInstance(): FragmentHue {
            val fragmentHue = FragmentHue()
            val args = Bundle()
            fragmentHue.arguments = args
            return fragmentHue
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_hue, container, false)
    }

}