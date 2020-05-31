package com.example.audiomemo;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

// basis of this class/interface for the click listener adapted from:
// https://medium.com/@harivigneshjayapalan/android-recyclerview-implementing-single-item-click-and-long-press-part-ii-b43ef8cb6ad8

public class RecyclerViewListener implements RecyclerView.OnItemTouchListener {

    private ClickListener clicklistener;
    private GestureDetector gestureDetector;

    public RecyclerViewListener(Context context, final RecyclerView recyclerView, final ClickListener clicklistener) {
            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) { // single click, "short" click
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY()); // determine location
                    if (child != null && clicklistener != null) { // check it's a valid event
                        clicklistener.onLongClick(child, recyclerView.getChildAdapterPosition(child)); // handle long click
                    }
                }
            });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
        View child = recyclerView.findChildViewUnder(e.getX(), e.getY()); // get location of click
        if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) { // ensure event is not null
            clicklistener.onClick(child, recyclerView.getChildAdapterPosition(child)); // send position
        }
            return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent e) { // prevent default

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    // interface, used to ensure calling class implements below click handlers
    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }
}