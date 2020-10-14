package com.neo.androidgesturespluralsight;


import android.content.Context;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neo.androidgesturespluralsight.models.Product;
import com.neo.androidgesturespluralsight.touchhelpers.ItemTouchHelperAdapter;
import com.neo.androidgesturespluralsight.util.BigDecimalUtil;
import com.neo.androidgesturespluralsight.util.CartManger;

import java.util.ArrayList;

public class CartRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ItemTouchHelperAdapter,
        GestureDetector.OnGestureListener
{

    private static final String TAG = "CartRecyclerViewAd";

    private static final int PRODUCT_TYPE = 1;
    private static final int HEADER_TYPE = 2;

    //vars
    private ArrayList<Product> mProducts = new ArrayList<>();
    private Context mContext;
    private ItemTouchHelper mTouchHelper;   // listener for the CartItemTouchHelperClass
    private GestureDetector mGestureDetector;
    private ViewHolder mSelectedHolder;     // ref to ViewHolder or view user tries to move



    public CartRecyclerViewAdapter(Context context, ArrayList<Product> products) {
        mContext = context;
        mProducts = products;
        mGestureDetector = new GestureDetector(mContext, this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case HEADER_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_cart_section_header, parent, false);
                return new SectionHeaderViewHolder(view);
            case PRODUCT_TYPE:
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_cart_list_item, parent, false);
                return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        int itemViewType = getItemViewType(position);
        if (itemViewType == PRODUCT_TYPE) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background);

            Glide.with(mContext)
                    .setDefaultRequestOptions(requestOptions)
                    .load(mProducts.get(position).getImage())
                    .into(((ViewHolder)holder).image);

            ((ViewHolder)holder).title.setText(mProducts.get(position).getTitle());
            ((ViewHolder)holder).price.setText(BigDecimalUtil.getValue(mProducts.get(position).getPrice()));

            ((ViewHolder)holder).parentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    ((ViewCartActivity)mContext).setIsScrolling(false);

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mSelectedHolder = ((ViewHolder)holder);
                        mGestureDetector.onTouchEvent(event);
                    }

                    return true;
                }
            });
        }
        else{
            SectionHeaderViewHolder headerViewHolder = (SectionHeaderViewHolder) holder;
            headerViewHolder.sectionTitle.setText(mProducts.get(position).getTitle());
        }


    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    @Override
    public int getItemViewType(int position) {          // ret the int view type to OnCreateView Holder
        if(TextUtils.isEmpty(mProducts.get(position).getType())){
            return HEADER_TYPE;
        }
        else{
            return PRODUCT_TYPE;
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // gets product using "fromPosition" and assigns a new place in the product list using same product
        Product fromProduct = mProducts.get(fromPosition);
        Product product = new Product(fromProduct);
        mProducts.remove(fromPosition);
        mProducts.add(toPosition, product);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemSwiped(int position) {
        // removes item from RV list and in the shopping cart
        CartManger cartManger = new CartManger(mContext);
        cartManger.removeItemFromCart(mProducts.get(position));

        mProducts.remove(mProducts.get(position));
        notifyItemRemoved(position);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {

        mTouchHelper = touchHelper;
    }


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
        if(!((ViewCartActivity)mContext).isScrolling()){
            mTouchHelper.startDrag(mSelectedHolder);
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, price;
        RelativeLayout parentView;      // the relativeLayout housing the ViewHolder items

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            parentView = itemView.findViewById(R.id.parent);
        }
    }

    public class SectionHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView sectionTitle;

        public SectionHeaderViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.cart_section_header);
        }
    }
}

















