package com.lossydragon.arduinorgb.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.lossydragon.arduinorgb.MainActivity
import com.lossydragon.arduinorgb.R
import kotlinx.android.synthetic.main.fragment_hex.*
import java.util.*

//TODO stop softinput from pushing actionbar up
class FragmentHex : Fragment() {

    companion object {
        fun newInstance(): FragmentHex {
            val fragmentRgb = FragmentHex()
            val args = Bundle()
            fragmentRgb.arguments = args
            return fragmentRgb
        }
    }

    private var hexValues: IntArray = IntArray(4)
    private val prefix = "#"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_hex, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPickerView_Hex.subscribe { color, _, _ ->
            colorHex(color)
        }

        //TODO editText Caps only and No suggestions.

        checkSeek()
    }

    private fun checkSeek() {

        seekbar_hex.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hexValues[3] = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendValues()
            }
        }
        )

        editText_hex.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //Nothing
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().startsWith(prefix)) {
                    editText_hex.setText(prefix)
                    Selection.setSelection(editText_hex.text, editText_hex.text.length)

                }

            }
        })
    }

    private fun colorHex(color: Int) {
        hexValues[0] = Color.red(color)
        hexValues[1] = Color.green(color)
        hexValues[2] = Color.blue(color)

        editText_hex.setText(String.format("#%02x%02x%02x",
                hexValues[0], hexValues[1], hexValues[2]).toUpperCase(Locale.getDefault()))

        sendValues()
    }

    //Dirty, but works.
    private fun sendValues() {
        (this.context as MainActivity).writeRgb(hexValues)
    }
}
