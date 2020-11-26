package com.example.maliclient.nav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

public class DrawerLayoutHorizontalSupport extends DrawerLayout {

    private RecyclerView mRecyclerView;
    private NavigationView mNavigationView;

    public DrawerLayoutHorizontalSupport(Context context) {
        super(context);
    }

    public DrawerLayoutHorizontalSupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutHorizontalSupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isInside(ev) && isDrawerOpen(mNavigationView)){
            //mRecyclerView.onInterceptTouchEvent(ev);
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isInside(MotionEvent ev) { //check whether user touch recylerView or not
        return ev.getX() >= mRecyclerView.getLeft() && ev.getX() <= mRecyclerView.getRight() &&
                ev.getY() >= mRecyclerView.getTop() && ev.getY() <= mRecyclerView.getBottom();
    }

    public void set(NavigationView navigationView, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mNavigationView = navigationView;
    }


}