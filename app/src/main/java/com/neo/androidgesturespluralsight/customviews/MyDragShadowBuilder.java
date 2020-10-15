package com.neo.androidgesturespluralsight.customviews;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;


/**
 * class to create dragShadow(smaller version of view) of view that we want to drag
 */
public class MyDragShadowBuilder extends View.DragShadowBuilder{

    private static Drawable shadow;

    /**
     * @param view is view that shadow will be taken from
     * @param imageResource is the drawable resource(image of product)
     */
    public MyDragShadowBuilder(View view, int imageResource) {
        super(view);
        // normally in production app imageRes will be as String url from a server, so constructor will change to accept string url
        // and then download the image using the string url and conv it to a drawable
        shadow = getView().getContext().getResources().getDrawable(imageResource);
    }
    @Override
    public void onProvideShadowMetrics (Point outShadowSize, Point outShadowTouchPoint) {
        // fun to get metrics of the shadow image
        int width, height, imageRatio;
        imageRatio = shadow.getIntrinsicHeight() / shadow.getIntrinsicWidth();    // gets width and height of image res and divide
        width = getView().getWidth() / 2;
        height = width * imageRatio;
        shadow.setBounds(0, 0, width, height);
        outShadowSize.set(width, height);                        //def width and height of shadow.
        outShadowTouchPoint.set(width / 2, height / 2);          // def pos within shadow that should be underneath touchPoint during the drag and drop operation
    }
    @Override
    public void onDrawShadow(Canvas canvas) {
        shadow.draw(canvas);
    }


}


















