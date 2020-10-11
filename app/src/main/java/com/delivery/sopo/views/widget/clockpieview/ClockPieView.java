package com.delivery.sopo.views.widget.clockpieview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.delivery.sopo.R;
import com.delivery.sopo.util.MyUtils;

import java.util.ArrayList;

public class ClockPieView extends View {

    private Paint textPaint;
    private Paint redPaint;
    private Paint linePaint;
    private Paint whitePaint;
    private Paint contentPaint;

    private int mViewWidth;
    private int mViewHeight;
    private int textSize;
    private int pieRadius;
    private Point pieCenterPoint;
    private int lineLength;
    private float leftTextWidth;
    private float rightTextWidth;
    private float topTextHeight;
    private int iconSize;
    private RectF cirRect;

    private Bitmap sunBitmap;
    private Bitmap moonBitmap;

    private ArrayList<ClockPieHelper> pieArrayList = new ArrayList<ClockPieHelper>();

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for(ClockPieHelper pie : pieArrayList){
                pie.update();
                if(!pie.isAtRest()){
                    needNewFrame = true;
                }
            }
            if (needNewFrame) {
                postDelayed(this, 0);
            }
            invalidate();
        }
    };

    public ClockPieView(Context context){
        this(context,null);
    }
    public ClockPieView(Context context, AttributeSet attrs){
        super(context, attrs);
        textSize = MyUtils.sp2px(context, 15);
        int lineThickness = MyUtils.dip2px(context, 1);
        lineLength = MyUtils.dip2px(context, 10);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        int TEXT_COLOR = Color.parseColor("#9B9A9B");
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = new Paint.FontMetrics();
        textPaint.getFontMetrics(fm);
        Rect textRect = new Rect();
        textPaint.getTextBounds("18",0,1, textRect);
        redPaint = new Paint(textPaint);
        int BLUE_COLOR = Color.parseColor("#5C94FF");
        redPaint.setColor(BLUE_COLOR);
        linePaint = new Paint(textPaint);
        linePaint.setStrokeWidth(lineThickness);
        whitePaint = new Paint(linePaint);
        whitePaint.setColor(Color.WHITE);
        contentPaint = new Paint(linePaint);
        int DEEP_GRAY_COLOR = Color.parseColor("#F1F1F4");
        contentPaint.setColor(DEEP_GRAY_COLOR);
        pieCenterPoint = new Point();
        cirRect = new RectF();
        leftTextWidth = textPaint.measureText("18");
        rightTextWidth = textPaint.measureText("6");
        topTextHeight = textRect.height();
        iconSize = MyUtils.dip2px(getContext(), 20);

        Resources resources = getResources();
        @SuppressLint("UseCompatLoadingForDrawables") BitmapDrawable sunBitmapDrawable = (BitmapDrawable)resources.getDrawable(R.drawable.ic_sun, null);
        sunBitmap = sunBitmapDrawable.getBitmap();
        @SuppressLint("UseCompatLoadingForDrawables") BitmapDrawable moonBitmapDrawable = (BitmapDrawable)resources.getDrawable(R.drawable.ic_moon, null);
        moonBitmap = moonBitmapDrawable.getBitmap();
    }

    public void setDate(ArrayList<ClockPieHelper> helperList){
        if(helperList != null && !helperList.isEmpty()){
            int pieSize = pieArrayList.isEmpty()? 0:pieArrayList.size();
            for(int i=0;i<helperList.size();i++){
                if(i>pieSize-1){
//                    float mStart = helperList.get(i).getStart();
                    pieArrayList.add(new ClockPieHelper(0,0,helperList.get(i)));
                }else{
                    pieArrayList.set(i, pieArrayList.get(i).setTarget(helperList.get(i)));
                }
            }
            int temp = pieArrayList.size() - helperList.size();
            for(int i=0; i<temp; i++){
                pieArrayList.remove(pieArrayList.size()-1);
            }
        }else {
            pieArrayList.clear();
        }

        removeCallbacks(animator);
        post(animator);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        if(pieArrayList != null){
            for(ClockPieHelper helper:pieArrayList){
                canvas.drawArc(cirRect,helper.getStart(),helper.getSweep(),true,redPaint);
            }
        }

        canvas.drawCircle(pieCenterPoint.x,pieCenterPoint.y,(float) pieRadius/3, whitePaint);

        sunBitmap = Bitmap.createScaledBitmap(sunBitmap,(int)iconSize ,(int)iconSize, true);
        canvas.drawBitmap(sunBitmap, (float)(pieCenterPoint.x-iconSize/2), (float)(pieCenterPoint.y + pieRadius/2+ iconSize/2),null);

        moonBitmap = Bitmap.createScaledBitmap(moonBitmap, (int)iconSize , (int)iconSize, true);
        canvas.drawBitmap(moonBitmap, (float)(pieCenterPoint.x-iconSize/2), (float)(pieCenterPoint.y-pieRadius/2-iconSize-iconSize/2),null);
    }

    private void drawBackground(Canvas canvas){
        canvas.drawCircle(pieCenterPoint.x,pieCenterPoint.y,pieRadius,contentPaint);
        canvas.drawText("24:00", pieCenterPoint.x, topTextHeight*2, textPaint);
        canvas.drawText("12:00",pieCenterPoint.x,mViewHeight-topTextHeight,textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        pieRadius = mViewWidth/2-lineLength*2-(int)(textPaint.measureText("18")/2);
        pieCenterPoint.set(mViewWidth/2-(int)rightTextWidth/2+(int)leftTextWidth/2,
                mViewHeight/2+textSize/2-(int)(textPaint.measureText("18")/2));
        cirRect.set(pieCenterPoint.x-pieRadius,
                pieCenterPoint.y-pieRadius,
                pieCenterPoint.x+pieRadius,
                pieCenterPoint.y+pieRadius);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private int measureWidth(int measureSpec){
        int preferred = 3;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec){
        int preferred = mViewWidth;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred){
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;

        switch(MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }
}