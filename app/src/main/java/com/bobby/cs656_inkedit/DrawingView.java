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
import android.widget.CheckBox;

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
    private float lowestPoint, highestPoint, leftmostPoint, rightmostPoint;
    private PointF strokeStart, strokeEnd;
    private int centerCount;
    private float letterHeight, letterWidth, verticalThird, verticalTwoThirds, horizontalThird, horizontalTwoThirds;

    //Be able to look at check box here
    private CheckBox graffitiCheck;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();

        // Create checkbox for graffiti mode
        graffitiCheck = (CheckBox) findViewById(R.id.graffiti_check);
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
        // To do graffiti recognizer, this must also save the stroke coordinates.
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:   // Screen touched -- start line
                // Erase all the old stuff if we're just detecting letters.
                if(MainActivity.graffitiModeOn()){ // This is probably some sort of bad code practice. What's better?
                    newDrawing();
                    centerCount = 0;
                    leftmostPoint = touchX;
                    rightmostPoint = touchX;
                    highestPoint = touchY;
                    lowestPoint = touchY;
                }
                drawPath.moveTo(touchX,touchY);
                newStroke = new ArrayList<PointF>(); // This will point newStroke to a new blank list, yes?
                newStroke.add(tempPoint);   // Get all the points.
                strokeStart = new PointF(touchX,touchY);
                break;
            case MotionEvent.ACTION_MOVE:   // Move of finger -- draw line between points
                drawPath.lineTo(touchX, touchY);
                newStroke.add(tempPoint);   // Get all the points.

                // See if several strokes get in the center to decide if we have a B or E.
                if(MainActivity.graffitiModeOn()){
                    if(touchX > 250 && touchX < 500 && touchY > 333 && touchY < 666){
                        centerCount++;
                    }

                    // Keep our maximum points up to date.
                    if(touchX > rightmostPoint) rightmostPoint = touchX;
                    if(touchX < leftmostPoint) leftmostPoint = touchX;
                    if(touchY > lowestPoint) lowestPoint = touchY;
                    if(touchY < highestPoint) highestPoint = touchY;
                }
                break;
            case MotionEvent.ACTION_UP:     // Lift off screen - commit to canvas/reset ink
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                strokes.add(newStroke);     // Add the new points to the list of strokes.
                strokeEnd = new PointF(touchX,touchY);

                // Append new character to textedit box.
                if(MainActivity.graffitiModeOn()) {
                    // Attempt to detect which letter was drawn.
                    // This will need to get more advanced.
                    //char newChar = detectLetterFullScreen();
                    char newChar = detectLetterNormalized();

                    MainActivity.appendLetter(newChar);
                }
                else {
                    //MainActivity.appendLetter('l');
                    // debug?
                }

                break;
            default:
                return false;   // Error
        }

        invalidate();   // Causes onDraw to execute.

        return true;
    }

    //
    // Use features to detect which letter the stroke represents.
    // This version will normalize for size and location.
    //
    private char detectLetterNormalized() {
        char newLetter = 'x';
        letterHeight = lowestPoint - highestPoint;
        letterWidth = rightmostPoint - leftmostPoint;
        horizontalThird = leftmostPoint + (letterWidth/3);
        horizontalTwoThirds = rightmostPoint - (letterWidth/3);
        verticalThird = highestPoint + (letterHeight/3);
        verticalTwoThirds = lowestPoint - (letterHeight/3);

        findNormalizedCenterCount();

        if(strokeStart.x <= horizontalThird && strokeStart.y >= verticalTwoThirds) // Stroke began in bottom left corner. It's A.
            newLetter = 'A';
        else if(strokeStart.x <= horizontalThird && strokeStart.y <= verticalThird) { // Stroke began in top left corner. It's B, D, or h
            //newLetter = 'B';
            if(strokeEnd.x >= horizontalTwoThirds)
                newLetter = 'h';
            else {
                if(centerCount > 5)
                    newLetter = 'B';
                else
                    newLetter = 'D';
            }
        }
        else {    // Stroke began elsewhere (top right corner, specifically...should I make this explicit?)...it's C, E, F, or G.
            if(strokeEnd.x <= horizontalThird && strokeEnd.y >= verticalTwoThirds) // Stroke ended on the left side. It's F.
                newLetter = 'F';
            else if(strokeEnd.y <= verticalTwoThirds && strokeEnd.x >= horizontalThird)  // Stroke ended in the vertical center, on the right. It's G. TODO I'm not super confident about this feature! Fix?
                newLetter = 'G';
            else {    // It's C or E. These are going to be hard to distinguish.
                if(centerCount > 5)
                    newLetter = 'E';
                else
                    newLetter = 'C';
            }
        }

        return newLetter;
    }

    private void findNormalizedCenterCount() {
        centerCount = 0;

        // iterate through the points
        for(PointF point : newStroke){
            // Find how many points are in the center of the letter
            if(point.x >= horizontalThird && point.x <= horizontalTwoThirds && point.y >= verticalThird && point.y <= verticalTwoThirds) {
                centerCount++;
            }

            // Determine path length?
        }

        // Toast the center count for reference
        //Toast toasty = Toast.makeText(new MainActivity().getApplicationContext(), "Center count = " + centerCount, Toast.LENGTH_SHORT);
        //toasty.show();
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

    //
    // Use features to detect which letter the stroke represents.
    // Will be simple for now. Need to improve.
    // This version does not normalize for location or size.
    //
    private char detectLetterFullScreen() {
        char newLetter = 'x';

        if(strokeStart.x <= 250 && strokeStart.y >= 666) // Stroke began in bottom left corner. It's A.
            newLetter = 'A';
        else if(strokeStart.x <= 250 && strokeStart.y <= 333) { // Stroke began in top left corner. It's B, D, or h
            //newLetter = 'B';
            if(strokeEnd.x > 300)
                newLetter = 'h';
            else {
                if(centerCount > 10)
                    newLetter = 'B';
                else
                    newLetter = 'D';
            }
        }
        else {    // Stroke began elsewhere (top right corner, specifically...should I make this explicit?)...it's C, E, F, or G.
            if(strokeEnd.x < 325 && strokeEnd.y > 600) // Stroke ended on the left side. It's F.
                newLetter = 'F';
            else if(strokeEnd.y < 700 && strokeEnd.x >= 325)  // Stroke ended in the vertical center, on the right. It's G. TODO I'm not super confident about this feature! Fix?
                newLetter = 'G';
            else {    // It's C or E. These are going to be hard to distinguish.
                if(centerCount > 10)
                    newLetter = 'E';
                else
                    newLetter = 'C';
            }
        }

        return newLetter;
    }

}
