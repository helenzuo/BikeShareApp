package com.example.mysecondapp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.core.IViewFinder;

public class CustomZXingScannerView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private static final float PORTRAIT_WIDTH_RATIO = 6f/8;
    private static final float PORTRAIT_WIDTH_HEIGHT_RATIO = 0.75f;

    private static final float LANDSCAPE_HEIGHT_RATIO = 5f/8;
    private static final float LANDSCAPE_WIDTH_HEIGHT_RATIO = 1.4f;
    private static final int MIN_DIMENSION_DIFF = 50;

    private static final float SQUARE_DIMENSION_RATIO = 5f/8;

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80L;

    private final int mDefaultLaserColor = getResources().getColor(R.color.viewfinder_laser, getContext().getTheme());
    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask, getContext().getTheme());
    private final int mDefaultBorderColor = getResources().getColor(R.color.offWhite, getContext().getTheme());
    private final int mDefaultBorderStrokeWidth = (int) (getResources().getInteger(R.integer.viewfinder_border_width) * 3);
    private final int mDefaultBorderLineLength = (int) (getResources().getInteger(R.integer.viewfinder_border_length)*1.5);

    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;
    protected boolean mSquareViewFinder;

    public CustomZXingScannerView(Context context) {
        super(context);
        init();
    }

    public CustomZXingScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setColor(mDefaultLaserColor);
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

        mBorderLineLength = mDefaultBorderLineLength;
    }

    public void setLaserColor(int laserColor) {
        mLaserPaint.setColor(laserColor);
    }
    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }
    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }
    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    // TODO: Need a better way to configure this. Revisit when working on 2.0
    public void setSquareViewFinder(boolean set) {
        mSquareViewFinder = set;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(getFramingRect() == null) {
            return;
        }
        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
    }


    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        RectF outerRectangle = new RectF(0, 0, width,height);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Anti alias allows for smooth corners
        canvas.drawRect(outerRectangle, mFinderMaskPaint);

//        Rect framingRect = getFramingRect();
//
//        canvas.drawRect(0, 0, width, framingRect.top, mFinderMaskPaint);
//        canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom + 1, mFinderMaskPaint);
//        canvas.drawRect(framingRect.right + 1, framingRect.top, width, framingRect.bottom + 1, mFinderMaskPaint);
//        canvas.drawRect(0, framingRect.bottom + 1, width, height, mFinderMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rectF = new RectF(framingRect.left, framingRect.top , framingRect.right, framingRect.bottom);
        paint.setColor(Color.TRANSPARENT); // An obvious color to help debugging
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg
        canvas.drawRoundRect(rectF, 15, 15, paint);

        canvas.drawLine((float)(framingRect.left + 10), (float)(framingRect.top + 10), (float)(framingRect.left + 10), (float)(framingRect.top + 10 + this.mBorderLineLength), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.left + 10), (float)(framingRect.top + 10), (float)(framingRect.left + 10 + this.mBorderLineLength), (float)(framingRect.top + 10), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.left + 10), (float)(framingRect.bottom - 10), (float)(framingRect.left + 10), (float)(framingRect.bottom - 10 - this.mBorderLineLength), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.left + 10), (float)(framingRect.bottom - 10), (float)(framingRect.left + 10 + this.mBorderLineLength), (float)(framingRect.bottom - 10), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.right - 10), (float)(framingRect.top + 10), (float)(framingRect.right - 10), (float)(framingRect.top + 10 + this.mBorderLineLength), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.right - 10), (float)(framingRect.top + 10), (float)(framingRect.right - 10 - this.mBorderLineLength), (float)(framingRect.top + 10), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.right - 10), (float)(framingRect.bottom - 10), (float)(framingRect.right - 10), (float)(framingRect.bottom - 10 - this.mBorderLineLength), this.mBorderPaint);
        canvas.drawLine((float)(framingRect.right - 10), (float)(framingRect.bottom - 10), (float)(framingRect.right - 10 - this.mBorderLineLength), (float)(framingRect.bottom - 10), this.mBorderPaint);
    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if(mSquareViewFinder) {
            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * SQUARE_DIMENSION_RATIO);
                width = height;
            } else {
                width = (int) (getWidth() * SQUARE_DIMENSION_RATIO);
                height = width;
            }
        } else {
            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * LANDSCAPE_HEIGHT_RATIO);
                width = (int) (LANDSCAPE_WIDTH_HEIGHT_RATIO * height);
            } else {
                width = (int) (getWidth() * PORTRAIT_WIDTH_RATIO);
                height = (int) (PORTRAIT_WIDTH_HEIGHT_RATIO * width);
            }
        }

        if(width > getWidth()) {
            width = getWidth() - MIN_DIMENSION_DIFF;
        }

        if(height > getHeight()) {
            height = getHeight() - MIN_DIMENSION_DIFF;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }
}
