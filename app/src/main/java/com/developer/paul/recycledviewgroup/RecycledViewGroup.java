package com.developer.paul.recycledviewgroup;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.developer.paul.recycledviewgroup.AwesomeViewGroup.AwesomeLayoutParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 9/5/17.
 */

public class RecycledViewGroup extends ViewGroup{
    private String TAG = "RecycledViewGroup";
    private int mTouchSlop;
    private List<AwesomeViewGroup> awesomeViewGroups;
    int[] colors =new int[]{Color.BLACK, Color.BLUE, Color.GRAY, Color.YELLOW, Color.GREEN};
    private int width,height;
    private float preX;
    private int orgLeft,orgRight;
    private boolean isOnScroll = false;


    public final int NUM_LAYOUTS = 3;
    private int num_total_layouts; // should be NUM_LAYOUTS + 2

    public final static int SCROLL_LEFT = 1;
    public final static int SCROLL_RIGHT = -1;
    private int curScrollDir = 0;



    public RecycledViewGroup(Context context) {
        super(context);
        init();
    }

    public RecycledViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        awesomeViewGroups = new ArrayList<>();
        num_total_layouts = NUM_LAYOUTS + 2;
        for (int i = 0 ; i < num_total_layouts ; i ++){
            AwesomeViewGroup linearLayout = new AwesomeViewGroup(getContext());
            linearLayout.setLayoutParams(new AwesomeLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            linearLayout.setBackgroundColor(colors[i]);
            linearLayout.setId(i);
            addView(linearLayout);
            awesomeViewGroups.add(linearLayout);
        }

    }

    private void moveChildX(float x){
        int childCount = getChildCount();
        for(int i = 0 ; i < childCount; i ++ ){
            AwesomeViewGroup linearLayout = awesomeViewGroups.get(i);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) linearLayout.getLayoutParams();

            lp.left = (int) (lp.left - x);
            lp.right = (int) (lp.right - x);

            linearLayout.reLayoutByLp();
        }
        postCheck();
    }

    private void recordOrgPos(){
        int childCount = getChildCount();
        for (int i = 0 ; i < childCount ; i ++){
            AwesomeViewGroup layout = awesomeViewGroups.get(i);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) layout.getLayoutParams();
            layout.setOrgLeft(lp.left);
            layout.setOrgRight(lp.right);
        }
    }

    private void postCheck(){
        int viewGroupSize = awesomeViewGroups.size();
        if (curScrollDir == SCROLL_LEFT){
            // scroll left only check the first one
            AwesomeViewGroup leftViewGroup = awesomeViewGroups.get(1);
            if (leftViewGroup.isOutOfParent()){
                moveFirstViewToLast(awesomeViewGroups);
                reDrawViewGroupToLast(awesomeViewGroups.get(viewGroupSize - 2), awesomeViewGroups.get(viewGroupSize -1));
            }
        }else if(curScrollDir == SCROLL_RIGHT){
            // scroll right only check the last one
            AwesomeViewGroup rightViewGroup = awesomeViewGroups.get(viewGroupSize-2);
            if (rightViewGroup.isOutOfParent()){
                moveLastViewToFirst(awesomeViewGroups);
                reDrawViewGroupToFirst(awesomeViewGroups.get(1), awesomeViewGroups.get(0));
            }
        }

        Log.i(TAG, "postCheck: " + awesomeViewGroups.get(0).getId() + " " +
            awesomeViewGroups.get(1).getId() + " " +
            awesomeViewGroups.get(2).getId() + " " +
            awesomeViewGroups.get(3).getId() + " " +
            awesomeViewGroups.get(4).getId() + " ");
    }

    private void moveFirstViewToLast(List<AwesomeViewGroup> viewGroupList){
        AwesomeViewGroup first = viewGroupList.get(0);
        viewGroupList.remove(0);
        viewGroupList.add(first);
    }

    private void moveLastViewToFirst(List<AwesomeViewGroup> viewGroupList){
        AwesomeViewGroup last = viewGroupList.get(viewGroupList.size() -1);
        viewGroupList.remove(last);
        viewGroupList.add(0, last);
    }

    private void reDrawViewGroupToLast(AwesomeViewGroup preAwesomeViewGroup, AwesomeViewGroup awesomeViewGroup){
        AwesomeLayoutParams preLp = (AwesomeLayoutParams) preAwesomeViewGroup.getLayoutParams();
        int preLpRight = preLp.right;

        AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
        lp.left = preLpRight;
        lp.right = lp.left + lp.width;

        awesomeViewGroup.reLayoutByLp();
    }

    private void reDrawViewGroupToFirst(AwesomeViewGroup postAwesomeViewGroup, AwesomeViewGroup awesomeViewGroup){
        AwesomeLayoutParams postLp = (AwesomeLayoutParams) postAwesomeViewGroup.getLayoutParams();
        int postLeft = postLp.left;

        AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
        lp.right = postLeft;
        lp.left = postLeft - lp.width;

        awesomeViewGroup.reLayoutByLp();
    }


    private void recordScrollDir(float x){
         curScrollDir = x > 0 ? SCROLL_LEFT: SCROLL_RIGHT;
    }

    private void resetScrollDir(){
        curScrollDir = 0;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false; //false means pass to child
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action){
            case MotionEvent.ACTION_MOVE:
                float newX = event.getX();
                float moveX = preX - newX;
                recordScrollDir(moveX);
                moveChildX(moveX);
                preX = newX;
                break;
            case MotionEvent.ACTION_DOWN:
                isOnScroll = true;
                preX = event.getX();
                recordOrgPos(); // every time it starts scrolling, record current positions
                break;
            case MotionEvent.ACTION_UP:
                isOnScroll = false;
                resetScrollDir();
                break;
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = width/NUM_LAYOUTS;
        int childHeight = height;

        int childCount = getChildCount();
        for (int i = 0 ; i < childCount ; i++){
            AwesomeViewGroup awesomeViewGroup = awesomeViewGroups.get(i);
            measureChild(awesomeViewGroup, widthMeasureSpec, heightMeasureSpec);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) getChildAt(i).getLayoutParams();
            lp.width = childWidth;
            lp.height = childHeight;

            lp.left = (i-1) * childWidth;
            lp.top = 0;
            lp.right = (i) * childWidth;
            lp.bottom = lp.top + lp.height;
        }
        Log.i(TAG, "onMeasure: " + "onMeasure");

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // start to layout children
        int childCount = getChildCount();
        for (int i = 0 ; i < childCount; i ++){
            AwesomeViewGroup child = awesomeViewGroups.get(i);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) child.getLayoutParams();
            child.layout(lp.left, lp.top, lp.right, lp.bottom);
        }

        Log.i(TAG, "onLayout: " + "onLayout");
    }
}
