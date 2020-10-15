package com.neo.androidgesturespluralsight;


import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neo.androidgesturespluralsight.models.Product;
import com.neo.androidgesturespluralsight.resources.ProductHeaders;
import com.neo.androidgesturespluralsight.touchhelpers.CartItemTouchHelperCallback;
import com.neo.androidgesturespluralsight.util.CartManger;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * activity that holds list of items added to cart in Rv
 */
public class ViewCartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ViewCartActivity";

    //widgets
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    //vars
    CartRecyclerViewAdapter mAdapter;
    private ArrayList<Product> mProducts = new ArrayList<>();
    private boolean mIsScrolling;     // true when list is scrolling i.e we are scrolling rv

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);
        mRecyclerView = findViewById(R.id.recycler_view);
        mFab = findViewById(R.id.fab);

        mFab.setOnClickListener(this);

        getProducts();
        initRecyclerView();
    }

    private void getProducts(){
        //add the headers with no type
        mProducts.add(new Product(ProductHeaders.HEADER_TITLES[0], 0, "", new BigDecimal(0), 0));
        mProducts.add(new Product(ProductHeaders.HEADER_TITLES[1], 0, "", new BigDecimal(0), 0));
        mProducts.add(new Product(ProductHeaders.HEADER_TITLES[2], 0, "", new BigDecimal(0), 0));

        CartManger cartManger = new CartManger(this);
        mProducts.addAll(cartManger.getCartItems());
    }

    private void initRecyclerView(){
        // assoc cartItemHelperCallback with ItemTouchHelperAdapter interface
        mAdapter = new CartRecyclerViewAdapter(this, mProducts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // setup to use item touch helper for rv
        ItemTouchHelper.Callback callback = new CartItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        mAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        //wait for the layout and recyclerview to finish loading the views
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                    mRecyclerView.setOnScrollListener(new CartScrollListener());
                }
                else{
                    mRecyclerView.addOnScrollListener(new CartScrollListener());
                }
            }
        });
    }


    /**
     * fun used to show or hide fab
     */
    private void setFABVisibility(boolean isVisible){
        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        if(isVisible){    // true when user scrolls to btm of list
            mFab.setAnimation(animFadeIn);
            mFab.setVisibility(View.VISIBLE);
        }
        else{
            mFab.setAnimation(animFadeOut);
            mFab.setVisibility(View.INVISIBLE);
        }
    }

    /***
     * checks if recyclerView is scrollable
     * @return true if scrollable, else false
     */
    public boolean isRecyclerScrollable() {
        return mRecyclerView.computeVerticalScrollRange() > mRecyclerView.getHeight();
    }

    public void setIsScrolling(boolean isScrolling){
        mIsScrolling = isScrolling;
    }

    public boolean isScrolling(){
        return mIsScrolling;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fab){
            mRecyclerView.smoothScrollToPosition(0);  // scrolls the RV to the first pos
        }
    }


    // custom cartScroll listener class
    class CartScrollListener extends RecyclerView.OnScrollListener{

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                Log.d(TAG, "onScrollStateChanged: stopped...");
            }
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                Log.d(TAG, "onScrollStateChanged: fling.");
            }
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                Log.d(TAG, "onScrollStateChanged: touched.");
            }
            setIsScrolling(true);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(isRecyclerScrollable()){
                if(!recyclerView.canScrollVertically(1)){   // true if Rv can be scrolled downwards
                    setFABVisibility(true);
                }
                else{
                    setFABVisibility(false);
                }
            }
            setIsScrolling(true);
        }
    }

}



















