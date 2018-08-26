package com.lossydragon.arduinorgb.colorpicker;

/*
 * Color picker library from:
 * https://github.com/duanhong169/ColorPicker
 * https://github.com/duanhong169/ColorPicker/blob/master/LICENSE
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ColorPickerView extends LinearLayout implements ColorObservable {

    private final ColorObservable observableOnDuty;

    private final int sliderMargin;
    private final int sliderHeight;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);


        ColorWheelView colorWheelView = new ColorWheelView(context);
        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (8 * density);
        sliderMargin = 2 * margin;
        sliderHeight = (int) (24 * density);

        {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(colorWheelView, params);
        }

        {
            BrightnessSliderView brightnessSliderView = new BrightnessSliderView(context);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, sliderHeight);

            params.topMargin = sliderMargin;

            //addView(brightnessSliderView, params);
            brightnessSliderView.bind(colorWheelView);

            observableOnDuty = brightnessSliderView;
        }

        setPadding(margin, margin, margin, margin);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = maxHeight - (getPaddingTop() + getPaddingBottom()) + (getPaddingLeft() + getPaddingRight());
        desiredWidth -= (sliderMargin + sliderHeight);


        int width = Math.min(maxWidth, desiredWidth);
        int height = width - (getPaddingLeft() + getPaddingRight()) + (getPaddingTop() + getPaddingBottom());

        height += (sliderMargin + sliderHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec)));
    }

    @Override
    public void subscribe(ColorObserver observer) {
        observableOnDuty.subscribe(observer);
    }

    @Override
    public void unsubscribe(ColorObserver observer) {
        observableOnDuty.unsubscribe(observer);
    }

    @Override
    public int getColor() {
        return observableOnDuty.getColor();
    }
}