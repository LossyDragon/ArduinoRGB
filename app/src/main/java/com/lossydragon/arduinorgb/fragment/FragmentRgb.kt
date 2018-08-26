package com.lossydragon.arduinorgb.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lossydragon.arduinorgb.R
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.lossydragon.arduinorgb.MainActivity
import com.lossydragon.arduinorgb.colorpicker.ColorPickerView

class FragmentRgb : Fragment() {

    @BindView(R.id.seekBar_R) internal lateinit var seekBarRed: SeekBar
    @BindView(R.id.seekBar_G) internal lateinit var seekBarGreen: SeekBar
    @BindView(R.id.seekBar_B) internal lateinit var seekBarBlue: SeekBar
    @BindView(R.id.textView_R) internal lateinit var textViewRed: TextView
    @BindView(R.id.textView_G) internal lateinit var textViewGreen: TextView
    @BindView(R.id.textView_B) internal lateinit var textViewBlue: TextView
    @BindView(R.id.dialog_color) internal lateinit var dialogColor: ImageButton
    @BindView(R.id.colorPickerView) internal lateinit var colorPickerView: ColorPickerView

    private var rgbValues: IntArray? = null

    companion object {
        fun newInstance(): FragmentRgb {
            val fragmentHome = FragmentRgb()
            val args = Bundle()
            fragmentHome.arguments = args
            return fragmentHome
        }
    }

    override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rgbValues = IntArray(4)

        val view = inflater.inflate(R.layout.fragment_rgb, viewGroup, false)

        ButterKnife.bind(this, view)

        colorPickerView.subscribe { color, _ -> colorHex(color) }

        //Init values to Seekbar values.
        textViewRed.text = getString(R.string.rgb_R, seekBarRed.progress)
        textViewGreen.text = getString(R.string.rgb_G, seekBarGreen.progress)
        textViewBlue.text = getString(R.string.rgb_B, seekBarBlue.progress)

        dialogColor.setOnClickListener {
            MaterialDialog(this.context as MainActivity)
                    .show {
                        title(R.string.dialog_chooseColor)
                                .colorChooser(primaryColors, primaryColorsSub) { _, color ->
                                    colorHex(color)
                                }
                        positiveButton(R.string.select)
                        negativeButton(android.R.string.cancel)
                    }
        }

        checkSeek()

        return view
    }

    private fun checkSeek() {

        seekBarRed.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        textViewRed.text = getString(R.string.text_R, progress)
                        rgbValues!![0] = progress
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        sendValues()
                    }
                }
        )

        seekBarGreen.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        textViewGreen.text = getString(R.string.text_G, progress)
                        rgbValues!![1] = progress
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        sendValues()
                    }
                }
        )

        seekBarBlue.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        textViewBlue.text = getString(R.string.text_B, progress)
                        rgbValues!![2] = progress
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        sendValues()
                    }
                }
        )
    }

    private fun colorHex(color: Int) {
        seekBarRed.progress = Color.red(color)
        seekBarGreen.progress = Color.green(color)
        seekBarBlue.progress = Color.blue(color)
        sendValues()
    }

    //Dirty, but works.
    private fun sendValues() {
        (this.context as MainActivity).writeRgb(rgbValues!!)
    }
}