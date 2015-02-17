package com.bobby.cs656_inkedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bobby on 1/21/2015.
 */
public class DrawingView extends View {


    private Path drawPath; //drawing path...as in, the vector of points the user draws
    private Paint drawPaint, canvasPaint; // drawing and canvas paint--why are these separate?
    private Canvas drawCanvas; // What the user will draw on
    private Bitmap canvasBitmap; // This will be what's saved
    private List<List<PointF>> strokes; // Saving the strokes for classifying graffiti
    private List<PointF> newStroke;  // Each new stroke
    private float oldX, oldY;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    // Create the drawing area that will draw based on touch interaction from user.
    private void setupDrawing() {

        // Design the ink line that will be created when the user draws
        drawPaint = new Paint();
        drawPaint.setColor(0xFFFFFFFF); // Draw white
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);

        // Saving the drawing as strokes.
        strokes = new ArrayList<List<PointF>>();

        // Path of the users drawing
        drawPath = new Path();

        // The ink to be placed on the canvas
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        // instantiate the bitmap and pass that bitmap to the canvas object
        // What is this relation for? A drawable object and a savable one
        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(canvasBitmap,0,0,canvasPaint);
        canvas.drawPath(drawPath,drawPaint);
    }

    // Detect the user "drawing" on the screen.
    @Override
    public boolean onTouchEvent(MotionEvent event){

        // Get touch coordinates
        float touchX, touchY;
        touchX = event.getX();
        touchY = event.getY();

        PointF tempPoint = new PointF(touchX,touchY);

        // Detect the user drawing a stroke.
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:   // Screen touched -- start line
                drawPath.moveTo(touchX,touchY);
                newStroke = new ArrayList<PointF>(); // This will point newStroke to a new blank list, yes?
                newStroke.add(tempPoint);   // Get all the points.
                break;
            case MotionEvent.ACTION_MOVE:   // Move of finger -- draw line between points
                drawPath.lineTo(touchX, touchY);
                newStroke.add(tempPoint);   // Get all the points.
                break;
            case MotionEvent.ACTION_UP:     // Lift off screen - commit to canvas/reset ink
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                strokes.add(newStroke);     // Add the new points to the list of strokes.
                break;
            default:
                return false;   // Error
        }

        invalidate();   // Causes onDraw to execute.

        oldX = touchX;
        oldY = touchY;

        return true;
    }

    // Clear the screen for a new drawing
    public void newDrawing() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR); // Draw all black on the screen.
        strokes.clear();    // Delete all the previous strokes.
        invalidate();
    }

    //
    // Draw a previously drawn bitmap, from gallery, on canvas
    // I'll just use the bitmaps for this, not the strokes.
    //
    public void loadBitmap(Bitmap bitmap){
        System.out.println("Made it into load bitmap!"); // Where can I see this message?
        //newDrawing();
        //Buffer buf = null;
        drawCanvas.drawBitmap(bitmap,0,0,null);
        //bitmap.copyPixelsToBuffer(buf);
        //canvasBitmap.copyPixelsFromBuffer(buf);
        System.out.println("Done with attempt to load bitmap!");
    }

    //
    // strokes getter
    //
    public List<List<PointF>> getStrokes(){
        return strokes;
    }
}
