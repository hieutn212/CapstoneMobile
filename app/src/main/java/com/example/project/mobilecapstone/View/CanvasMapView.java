package com.example.project.mobilecapstone.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ADMIN on 3/5/2018.
 */

public class CanvasMapView extends View {
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap map;

    public CanvasMapView(Context context, Paint mPaint, Bitmap mapBackground) {
        super(context);
        this.map = mapBackground;
    }

    public CanvasMapView(Context context, @Nullable AttributeSet attrs, Paint mPaint, Bitmap mapBackground) {
        super(context, attrs);
        this.map = mapBackground;
    }
}
