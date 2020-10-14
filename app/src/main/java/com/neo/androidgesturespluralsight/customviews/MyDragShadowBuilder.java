package com.neo.androidgesturespluralsight.customviews;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by User on 3/4/2018.
 */

/*
    create a canvas of what we want to draw and make it draggable.
 */

public class MyDragShadowBuilder extends View.DragShadowBuilder{

    private static Drawable shadow;

    /**
     * @param view is view that shadow will be taken from
     * @param imageResource is the drawable of resource(image of product)
     */
    public MyDragShadowBuilder(View view, int imageResource) {
        super(view);
        shadow = getView().getContext().getResources().getDrawable(imageResource);
    }
    @Override
    public void onProvideShadowMetrics (Point outShadowSize, Point outShadowTouchPoint) {
        int width, height, imageRatio;
        imageRatio = shadow.getIntrinsicHeight() / shadow.getIntrinsicWidth();    // gets shadow of that imageResource
        width = getView().getWidth() / 2;
        height = width * imageRatio;
        shadow.setBounds(0, 0, width, height);
        outShadowSize.set(width, height);                        //def width and height of shadow.
        outShadowTouchPoint.set(width / 2, height / 2);                // def pos within shadow that should be underneath touchPoint during the drag and drop operation
    }
    @Override
    public void onDrawShadow(Canvas canvas) {
        shadow.draw(canvas);
    }


}


















