package com.lossydragon.arduinorgb.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import butterknife.BindView
import butterknife.ButterKnife
import com.lossydragon.arduinorgb.MainActivity
import com.lossydragon.arduinorgb.R
import com.lossydragon.arduinorgb.colorpicker.ColorPickerView


//TODO stop softinput from pushing actionbar up
class FragmentHex : Fragment() {

    @BindView(R.id.colorPickerView_Hex)
    internal lateinit var hexColorPicker: ColorPickerView
    @BindView(R.id.editText_hex)
    internal lateinit var hexEditText: EditText
    @BindView(R.id.seekbar_hex)
    internal lateinit var hexSeekBar: SeekBar

    private var hexValues: IntArray? = null
    private val prefix = "#"

    companion object {
        fun newInstance(): FragmentHex {
            val fragmentRgb = FragmentHex()
            val args = Bundle()
            fragmentRgb.arguments = args
            return fragmentRgb
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_hex, container, false)
        hexValues = IntArray(4)
        ButterKnife.bind(this, view)

        hexColorPicker.subscribe { color, _ -> colorHex(color) }

        //TODO editText Caps only and No suggestions.

        checkSeek()

        return view
    }

    private fun checkSeek() {

        hexSeekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        hexValues!![3] = progress
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        sendValues()
                    }
                }
        )

        hexEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().startsWith(prefix)) {
                    hexEditText.setText(prefix)
                    Selection.setSelection(hexEditText.text, hexEditText.text.length)

                }

            }
        })
    }

    private fun colorHex(color: Int) {
        hexValues!![0] = Color.red(color)
        hexValues!![1] = Color.green(color)
        hexValues!![2] = Color.blue(color)

        hexEditText.setText(String.format("#%02x%02x%02x",
                hexValues!![0], hexValues!![1], hexValues!![2]).toUpperCase())

        sendValues()
    }

    //Dirty, but works.
    private fun sendValues() {
        (this.context as MainActivity).writeRgb(hexValues!!)
    }
}
