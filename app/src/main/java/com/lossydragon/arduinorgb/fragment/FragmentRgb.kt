package com.lossydragon.arduinorgb.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.lossydragon.arduinorgb.MainActivity
import com.lossydragon.arduinorgb.R
import kotlinx.android.synthetic.main.fragment_rgb.*


class FragmentRgb : Fragment() {

    companion object {
        fun newInstance(): FragmentRgb {
            val fragmentHome = FragmentRgb()
            val args = Bundle()
            fragmentHome.arguments = args
            return fragmentHome
        }
    }

    private var rgbValues: IntArray = IntArray(4)

    override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_rgb, viewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPickerView_rgb.subscribe { color, _, _ ->
            colorHex(color)
        }

        checkSeek()

        //Init values to Seekbar values.
        textView_R.text = getString(R.string.rgb_R, seekBar_R.progress)
        textView_G.text = getString(R.string.rgb_G, seekBar_G.progress)
        textView_B.text = getString(R.string.rgb_B, seekBar_B.progress)

        dialog_color.setOnClickListener {
            MaterialDialog(context!!)
                    .show {
                        title(R.string.dialog_chooseColor)
                                .colorChooser(primaryColors, primaryColorsSub) { _, color ->
                                    colorHex(color)
                                }
                        positiveButton(R.string.select)
                        negativeButton(android.R.string.cancel)
                    }
        }
    }

    private fun checkSeek() {

        seekBar_R.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView_R.text = getString(R.string.text_R, progress)
                rgbValues[0] = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendValues()
            }
        }
        )

        seekBar_G.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView_G.text = getString(R.string.text_G, progress)
                rgbValues[1] = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendValues()
            }
        }
        )

        seekBar_B.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView_B.text = getString(R.string.text_B, progress)
                rgbValues[2] = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendValues()
            }
        }
        )
    }

    private fun colorHex(color: Int) {
        seekBar_R.progress = Color.red(color)
        seekBar_G.progress = Color.green(color)
        seekBar_B.progress = Color.blue(color)
        sendValues()
    }

    //Dirty, but works.
    private fun sendValues() {
        (this.context as MainActivity).writeRgb(rgbValues)
    }
}