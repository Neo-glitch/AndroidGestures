package com.neo.androidgesturespluralsight;


import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Fade;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.neo.androidgesturespluralsight.customviews.MyDragShadowBuilder;
import com.neo.androidgesturespluralsight.models.Product;
import com.neo.androidgesturespluralsight.resources.Products;
import com.neo.androidgesturespluralsight.util.CartManger;

import java.util.ArrayList;

/**
 * Activity with swipeable images in a ViewPager
 */
public class ViewProductActivity extends AppCompatActivity implements
        View.OnTouchListener,   // to detect the touch
        View.OnClickListener,
        GestureDetector.OnGestureListener,   // to determine the gesture
        GestureDetector.OnDoubleTapListener,  // to det doubleTap gesture
        View.OnDragListener{                    // to det drag gesture

    private static final String TAG = "ViewProductActivity";

    //widgets
    private ViewPager mProductContainer;
    private TabLayout mTabLayout;
    private RelativeLayout mAddToCart, mCart;
    private ImageView mCartIcon, mPlusIcon;

    //vars
    private Product mProduct;
    private ProductPagerAdapter mPagerAdapter;
    private GestureDetector mGestureDetector;
    private Rect mCartPositionRectangle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);
        mProductContainer = findViewById(R.id.product_container);
        mTabLayout = findViewById(R.id.tab_layout);
        mAddToCart = findViewById(R.id.add_to_cart);
        mCart = findViewById(R.id.cart);
        mPlusIcon = findViewById(R.id.plus_image);
        mCartIcon = findViewById(R.id.cart_image);

        mProductContainer.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(this, this);
        mCart.setOnClickListener(this);
        mAddToCart.setOnClickListener(this);

        getIncomingIntent();
        initPagerAdapter();
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_product))){
            mProduct = intent.getParcelableExtra(getString(R.string.intent_product));
        }
    }

    /**
     * init a new ViewProductFragment for each product variation
     */
    private void initPagerAdapter(){
        ArrayList<Fragment> fragments = new ArrayList<>();
        Products products = new Products();
        Product[] selectedProducts = products.PRODUCT_MAP.get(mProduct.getType());   // gets list of product obj variations of product passed from MainActivity
        for(Product product: selectedProducts){
            // add each product var obj to the fragment list bundles.
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.intent_product), product);
            ViewProductFragment viewProductFragment = new ViewProductFragment();
            viewProductFragment.setArguments(bundle);
            fragments.add(viewProductFragment);
        }
        mPagerAdapter = new ProductPagerAdapter(getSupportFragmentManager(), fragments);
        mProductContainer.setAdapter(mPagerAdapter);
        // assoc dot tab layout with the viewPager
        mTabLayout.setupWithViewPager(mProductContainer, true);
    }

    /**
     * fun used to set a rect area, that will be used to know if shadow being dragged is in that area
     */
    private void getCartPosition(){
        mCartPositionRectangle = new Rect();
        mCart.getGlobalVisibleRect(mCartPositionRectangle);  // defines where the rect will stay on screen, i.e where the mCart View is

        // gets width of the app screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        mCartPositionRectangle.left = mCartPositionRectangle.left - Math.round((int)(width * 0.18));        // extends left part of rect by 18% of screen width
        mCartPositionRectangle.top = 0;                                                                     // sets pos of top part of rect to 0(top most part)
        mCartPositionRectangle.right = width;                                                               // sets right most side of rect to be width of screen cord
        mCartPositionRectangle.bottom = mCartPositionRectangle.bottom - Math.round((int)(width * 0.03));    // subtract 3% of screen width from bottom part of rect
    }


    /**
     * hides or shows plusIcon depending on if we are dragging or not
     * @param isDragging
     */
    private void setDragMode(boolean isDragging){
        if(isDragging){
            mCartIcon.setVisibility(View.INVISIBLE);
            mPlusIcon.setVisibility(View.VISIBLE);
        }
        else{
            mCartIcon.setVisibility(View.VISIBLE);
            mPlusIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void addCurrentItemToCart(){
        // gets the product variation obj in viewPager View from the fragment inView
        Product selectedProduct = ((ViewProductFragment)mPagerAdapter.getItem(mProductContainer.getCurrentItem())).mProduct;

        CartManger cartManger = new CartManger(this);
        cartManger.addItemToCart(selectedProduct);
        Toast.makeText(this, "added to cart", Toast.LENGTH_SHORT).show();
    }


    /**
     * inflates the fullScreenProductFragment
     */
    private void inflateFullScreenProductFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FullScreenProductFragment fragment = new FullScreenProductFragment();

        // sets the product Variation to the bundle
        Bundle bundle = new Bundle();
        Product selectedProduct =((ViewProductFragment)mPagerAdapter.getItem(mProductContainer.getCurrentItem())).mProduct;
        bundle.putParcelable(getString(R.string.intent_product), selectedProduct);
        fragment.setArguments(bundle);

        // Enter Transition for New Fragment
        Fade enterFade = new Fade();
        enterFade.setStartDelay(1);
        enterFade.setDuration(300);
        fragment.setEnterTransition(enterFade);

        transaction.addToBackStack(getString(R.string.fragment_full_screen_product));
        transaction.replace(R.id.full_screen_container, fragment, getString(R.string.fragment_full_screen_product));
        transaction.commit();
    }


    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.cart:{
                //open Cart Activity
                Intent intent = new Intent(this, ViewCartActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.add_to_cart:{
                addCurrentItemToCart();
                break;
            }
        }
    }


    /*
        OnTouch
     */

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        getCartPosition();

        if(view.getId() == R.id.product_container){
            mGestureDetector.onTouchEvent(motionEvent);
        }

//        int action = motionEvent.getAction();
//
//        switch(action) {
//            case (MotionEvent.ACTION_DOWN):
//                Log.d(TAG, "Action was DOWN");
//                return false;
//            case (MotionEvent.ACTION_MOVE):
//                Log.d(TAG, "Action was MOVE");
//                return false;
//            case (MotionEvent.ACTION_UP):
//                Log.d(TAG, "Action was UP");
//                return false;
//            case (MotionEvent.ACTION_CANCEL):
//                Log.d(TAG, "Action was CANCEL");
//                return false;
//            case (MotionEvent.ACTION_OUTSIDE):
//                Log.d(TAG, "Movement occurred outside bounds " +
//                        "of current screen element");
//                return false;
//        }

        return false;
    }

    /*
        GestureDetector
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.d(TAG, "onDown: called");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        Log.d(TAG, "onShowPress: called.");

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d(TAG, "onSingleTapUp: called.");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent,
                            MotionEvent motionEvent1,
                            float v, float v1) {
        Log.d(TAG, "onScroll: called.");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d(TAG, "onLongPress: called.");

        ViewProductFragment fragment = ((ViewProductFragment)mPagerAdapter.getItem(mProductContainer.getCurrentItem()));
        // Instantiates the drag shadow builder.
        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(
                ((ViewProductFragment)fragment).mImageView, fragment.mProduct.getImage());

        // Starts the drag
        ((ViewProductFragment)fragment).mImageView.startDrag(null,  // the data to be dragged
                myShadow,  // the drag shadow builder
                null,      // no need to use local data
                0          // flags (not currently used, set to 0)
        );

        myShadow.getView().setOnDragListener(this);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent,
                           MotionEvent motionEvent1,
                           float v, float v1) {
        Log.d(TAG, "onFling: called.");
        return false;
    }

    /*
        DoubleTap(gesture detector auto associate with this, if the event in this interface fun() occurs)
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        Log.d(TAG, "onSingleTapConfirmed: called.");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Log.d(TAG, "onDoubleTap: called.");
        inflateFullScreenProductFragment();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        Log.d(TAG, "onDoubleTapEvent: called.");
        return false;
    }

    /*
        OnDragListener
     */
    @Override
    public boolean onDrag(View view, DragEvent event) {

        switch(event.getAction()) {

            case DragEvent.ACTION_DRAG_STARTED:
                Log.d(TAG, "onDrag: drag started.");
                setDragMode(true);
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:

                return true;

            case DragEvent.ACTION_DRAG_LOCATION:   // triggered when drag shadow is constantly moving the the bonding box
                Point currentPoint = new Point(Math.round(event.getX()), Math.round(event.getY()));
                if(mCartPositionRectangle.contains(currentPoint.x, currentPoint.y)){
                    // if drag point in rect
                    mCart.setBackgroundColor(this.getResources().getColor(R.color.blue2));
                }
                else{
                    mCart.setBackgroundColor(this.getResources().getColor(R.color.blue1));
                }

                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                return true;

            case DragEvent.ACTION_DROP:

                Log.d(TAG, "onDrag: dropped.");

                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                Log.d(TAG, "onDrag: ended.");

                Drawable background = mCart.getBackground();
                if (background instanceof ColorDrawable) {
                    if (((ColorDrawable) background).getColor() == getResources().getColor(R.color.blue2)) {
                        addCurrentItemToCart();
                    }
                }
                mCart.setBackground(this.getResources().getDrawable(R.drawable.blue_onclick_dark));
                setDragMode(false);
                return true;
            // An unknown action type was received.
            default:
                Log.e(TAG,"Unknown action type received by OnStartDragListener.");
                break;

        }
        return false;
    }
}




























