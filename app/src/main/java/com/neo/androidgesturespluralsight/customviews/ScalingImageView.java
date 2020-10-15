package com.neo.androidgesturespluralsight.customviews;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;


/**
 * custom ImageView class, that allows us to scale an ImageView
 */
public class ScalingImageView extends AppCompatImageView implements
        View.OnTouchListener,                 // to detect touch
        GestureDetector.OnGestureListener,    // to det gesture
        GestureDetector.OnDoubleTapListener {  // to det the double tap gesture

    private static final String TAG = "ScalingImageView";


    //shared constructing
    Context mContext;
    ScaleGestureDetector mScaleDetector;
    GestureDetector mGestureDetector;
    Matrix mMatrix;     // matrix for scaling and translating image
    float[] mMatrixValues;    // array for getting keypoints in matrix reping image

    // Image States
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Scales
    float mSaveScale = 1f;
    float mMinScale = 1f;
    float mMaxScale = 4f;

    // view dimensions
    float origWidth, origHeight;
    int viewWidth, viewHeight;    // total Image View width and height

    // Tracks pos of image on Screen
    PointF mLast = new PointF();   // holds last point where user pressed on
    PointF mStart = new PointF();


    public ScalingImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public ScalingImageView(Context context, @Nullable AttributeSet attrs) {  // constructor needed to use the widget in a layout file
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);        // allows gesture interaction with Image
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mMatrixValues = new float[9];
        setImageMatrix(mMatrix);  // assoc ImageView with the matrix
        setScaleType(ScaleType.MATRIX);

        // init gesture Detector
        mGestureDetector = new GestureDetector(context, this);
        setOnTouchListener(this);
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {  // called when we begin changing scale
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {   // called anytime change is made to scaleFactor
            Log.d(TAG, "onScale: " + detector.getScaleFactor());
            float mScaleFactor = detector.getScaleFactor();                 // zoom ret val large than 1 and zoom out of orig image ret val less than 1
            float prevScale = mSaveScale;                                   // saves scale val from previous scale to prevScale var
            mSaveScale *= mScaleFactor;                                     // gets new scale value

            // logic max sure scale is not larger than max scale or lower than min scale
            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                mScaleFactor = mMaxScale / prevScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                mScaleFactor = mMinScale / prevScale;
            }

            // logic for scaling the image
            if (origWidth * mSaveScale <= viewWidth
                    || origHeight * mSaveScale <= viewHeight) {               // image is not occupying entire view, scale from middle
                mMatrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                        viewHeight / 2);
                Log.d(TAG, "onScale: VIEW CENTER FOCUS");
            } else {                                                          // image is occupying entireView and Scale from where user is pinching
                mMatrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());
                Log.d(TAG, "onScale: VIEW ON PINCH FOCUS");
            }


            fixTranslation();
            return true;
        }
    }


    /**
     * gets dim of drawable set to the ImageView and then scaling matrix accordingly
     * inorder to fit imageView to the screen when imageView is double tapped
     */
    public void fitToScreen() {
        mSaveScale = 1;      // resets scaleValue to 1

        float scale;
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0
                || drawable.getIntrinsicHeight() == 0)
            return;
        // gets width and height of the image drawable
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        Log.d(TAG, "imageWidth: " + imageWidth + " imageHeight : " + imageHeight);

        float scaleX = (float) viewWidth / (float) imageWidth;
        float scaleY = (float) viewHeight / (float) imageHeight;
        scale = Math.min(scaleX, scaleY);   // gets the limiting factor i.e smallest of two scales
        mMatrix.setScale(scale, scale);

        // Center the image
        float redundantYSpace = (float) viewHeight  // gets empty space in height dimension
                - (scale * (float) imageHeight);
        float redundantXSpace = (float) viewWidth   // gets empty space in X dir(will be zero since no space), since X is limiting factor
                - (scale * (float) imageWidth);
        redundantYSpace /= (float) 2;               // done for even distro of space on tp and bottom of View
        redundantXSpace /= (float) 2;

        Log.d(TAG, "fitToScreen: redundantXSpace: " + redundantXSpace);
        Log.d(TAG, "fitToScreen: redundantYSpace: " + redundantYSpace);

        mMatrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = viewWidth - 2 * redundantXSpace;  // sets actual width of the image
        origHeight = viewHeight - 2 * redundantYSpace;  // sets actual height of image
        setImageMatrix(mMatrix);
    }



    /*
        ImageView Translation Correction Method, called after posting trans to matrix
     */
    void fixTranslation() {
        mMatrix.getValues(mMatrixValues); //put matrix values into a float array(mMatrixValues) so we can analyze
        float transX = mMatrixValues[Matrix.MTRANS_X]; //get the most recent translation in x direction
        float transY = mMatrixValues[Matrix.MTRANS_Y]; //get the most recent translation in y direction

        // ret 0 if trans doesn't meet boundary condition, i.e translation is valid
        float fixTransX = getFixTranslation(transX, viewWidth, origWidth * mSaveScale);
        float fixTransY = getFixTranslation(transY, viewHeight, origHeight * mSaveScale);

        if (fixTransX != 0 || fixTransY != 0)
            mMatrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTranslation(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;  // min trans means -viewSize - contentSize up to 0 and maxtrans is 0 up to viewSize - ContentSize

        if (contentSize <= viewSize) { // case: NOT ZOOMED
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else { //CASE: ZOOMED
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) { // negative x or y translation (down or to the right)
            // activated when we hit ImageView bounds to cancel out any other trans towards view bounds
            // ret same trans but with opposite values of translation to counter it
            Log.d(TAG, "getFixTranslation: minTrans: " + minTrans + ", trans: " + trans);
            Log.d(TAG, "getFixTranslation: return: " + (-trans + minTrans));
            return -trans + minTrans;
        }

        if (trans > maxTrans) { // positive x or y translation (up or to the left)
            Log.d(TAG, "getFixTranslation: maxTrans: " + maxTrans + ", trans: " + trans);
            Log.d(TAG, "getFixTranslation: return: " + (-trans + maxTrans));
            return -trans + maxTrans;
        }
        return 0;
    }


    /**
     * fun to decide whether or not to allow translation in  X or Y dir during a drag
     * @param delta : trans mag i.e dx or dy
     * @param viewSize : size of the View i.e View width or View height
     * @param contentSize : original scaled width or height of image
     * @return
     */
    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        // exec only when if statement fails and that's image is occupying entire View
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // called before drawable is set to the ImageView, calc space req for the View
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (mSaveScale == 1) {
            // Fit to screen.(must be called here, since we want to set this before image is drawn to View)
            fitToScreen();
        }

    }

    /*
        Ontouch
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        PointF currentPoint = new PointF(event.getX(), event.getY());  // gets current user press pos

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLast.set(currentPoint);
                mStart.set(mLast);
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // relative drag movement in X and Y dir respectively
                    float dx = currentPoint.x - mLast.x;
                    float dy = currentPoint.y - mLast.y;

                    float fixTransX = getFixDragTrans(dx, viewWidth, origWidth * mSaveScale);
                    Log.d(TAG, "onTouch: fixTransX: " + fixTransX);
                    float fixTransY = getFixDragTrans(dy, viewHeight, origHeight * mSaveScale);
                    Log.d(TAG, "onTouch: fixTransY: " + fixTransY);
                    mMatrix.postTranslate(fixTransX, fixTransY);        // moves the image

                    fixTranslation();

                    mLast.set(currentPoint.x, currentPoint.y);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        setImageMatrix(mMatrix);                    // post the changes made to the image
        return false;
    }

    /*
        GestureListener
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    /*
        onDoubleTap
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        fitToScreen();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }
}
































