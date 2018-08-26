package com.lossydragon.arduinorgb.colorpicker;

/*
 * Color picker library from:
 * https://github.com/duanhong169/ColorPicker
 * https://github.com/duanhong169/ColorPicker/blob/master/LICENSE
 */

interface ColorObserver {
    void onColor(int color, boolean fromUser);
}
