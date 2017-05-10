package com.developer.paul.recycledviewgroup;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import com.developer.paul.recycledviewgroup.AwesomeViewGroup.AwesomeLayoutParams;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Paul on 9/5/17.
 */

public class RecycledViewGroup extends ViewGroup{
    private String TAG = "RecycledViewGroup";
    private int mTouchSlop;
    private List<AwesomeViewGroup> awesomeViewGroups;
    int[] colors =new int[]{Color.RED, Color.BLUE, Color.GRAY, Color.YELLOW, Color.GREEN};
    private int width,height;
    private float preX, preY;

    public final int NUM_LAYOUTS = 3;

    public final static int NON_SCROLL = 0;
    public final static int SCROLL_LEFT = 1;
    public final static int SCROLL_RIGHT = -1;

    public final static int SCROLL_UP = 2;
    public final static int SCROLL_DOWN = -2;

    public final static int SCROLL_VERTICAL = 1001;
    public final static int SCROLL_HORIZONTAL = 1002;

    private int curScrollDir = 0; // {SCROLL_LEFT, SCROLL_RIGHT, SCROLL_UP, SCROLL_DOWN}

    private boolean hasDecideScrollWay = false; // if scroll too small, cannot decide whether is vertical or horizontal
    private int curScrollWay = 0; // {SCROLL_VERTICAL, SCROLL_HORIZONTAL} only one

    public CalendarInterface calendarInterface;


    public RecycledViewGroup(Context context) {
        super(context);
        init();
    }

    public RecycledViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setCalendarInterface(CalendarInterface calendarInterface) {
        this.calendarInterface = calendarInterface;
        setCalendars();
    }


    public float getFirstVisibleLeftOffset(){
        for (AwesomeViewGroup awesomeViewGroup: awesomeViewGroups){
            if (awesomeViewGroup.isVisibleInParent()){
                AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
                return lp.left;
            }
        }

        return 0.0f;
    }

    private void setCalendars(){
        int size = awesomeViewGroups.size();
        for (int i = 0 ; i < size ; i ++){
            Calendar c = calendarInterface.getFirstDay();
            c.add(Calendar.DATE, i);
            String dateString = formatCalendar(c);
            awesomeViewGroups.get(i).setCalendar(c);
            awesomeViewGroups.get(i).setTopText(dateString);
        }
    }

    private String formatCalendar(Calendar c){
        DateFormat dateformat = DateFormat.getDateInstance();
        return dateformat.format(c.getTime());
    }

    private void init(){
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        awesomeViewGroups = new ArrayList<>();
        int num_total_layouts = NUM_LAYOUTS + 2;
        for (int i = 0 ; i < num_total_layouts ; i ++){
            AwesomeViewGroup awesomeViewGroup = new AwesomeViewGroup(getContext());
            awesomeViewGroup.setLayoutParams(new AwesomeLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            awesomeViewGroup.setBackgroundColor(colors[i]);
            awesomeViewGroup.setId(i);
            addView(awesomeViewGroup);
            awesomeViewGroups.add(awesomeViewGroup);
        }

    }

    private void moveChildX(float x){
        int childCount = getChildCount();
        for(int i = 0 ; i < childCount; i ++ ){
            AwesomeViewGroup awesomeViewGroup = awesomeViewGroups.get(i);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();

            lp.left = lp.left - x;
            lp.right = lp.right - x;
            awesomeViewGroup.reLayoutByLp();
        }
        postCheck();
    }

    private void moveChildY(float y){
        int childCount = getChildCount();

        for (int i = 0 ; i < childCount ; i ++ ){
            AwesomeViewGroup awesomeViewGroup = awesomeViewGroups.get(i);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();

            float top = lp.top - y;
            float bottom = lp.bottom - y;

            preCheck(top, bottom, lp); // if scroll too much...
            awesomeViewGroup.reLayoutByLp();
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
                Log.i(TAG, "postCheck: SCROLL_LEFT: " + awesomeViewGroups.get(0).getId() + " " +
                    awesomeViewGroups.get(1).getId() + " " +
                    awesomeViewGroups.get(2).getId() + " " +
                    awesomeViewGroups.get(3).getId() + " " +
                    awesomeViewGroups.get(4).getId() );
                updateNewLastCalendar(awesomeViewGroups.get(viewGroupSize-1));
            }
        }else if(curScrollDir == SCROLL_RIGHT){
            // scroll right only check the last one
            AwesomeViewGroup rightViewGroup = awesomeViewGroups.get(viewGroupSize-2);
            if (rightViewGroup.isOutOfParent()){
                moveLastViewToFirst(awesomeViewGroups);
                reDrawViewGroupToFirst(awesomeViewGroups.get(1), awesomeViewGroups.get(0));
                updateNewFirstCalendar(awesomeViewGroups.get(0));
                Log.i(TAG, "postCheck: SCROLL_RIGHT: " + awesomeViewGroups.get(0).getId() + " " +
                        awesomeViewGroups.get(1).getId() + " " +
                        awesomeViewGroups.get(2).getId() + " " +
                        awesomeViewGroups.get(3).getId() + " " +
                        awesomeViewGroups.get(4).getId() );
            }
        }
    }

    private void preCheck(float top, float bottom,AwesomeLayoutParams lp){
        if (curScrollDir == SCROLL_DOWN){
            if (bottom < lp.parentHeight){
                // reach bottom, stop scrolling
                lp.bottom = lp.parentHeight;
                lp.top = lp.bottom - lp.height;
                return;
            }
        }else if (curScrollDir == SCROLL_UP){
            if (top > 0 ){
                // reach top, stop scrolling
                lp.top = 0;
                lp.bottom = lp.top + lp.height;
                return;
            }
        }

        lp.bottom = bottom;
        lp.top = top;

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
        float preLpRight = preLp.right;

        AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
        lp.left = preLpRight;
        lp.right = lp.left + lp.width;

        awesomeViewGroup.reLayoutByLp();
    }

    private void reDrawViewGroupToFirst(AwesomeViewGroup postAwesomeViewGroup, AwesomeViewGroup awesomeViewGroup){
        AwesomeLayoutParams postLp = (AwesomeLayoutParams) postAwesomeViewGroup.getLayoutParams();
        float postLeft = postLp.left;

        AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
        lp.right = postLeft;
        lp.left = postLeft - lp.width;

        awesomeViewGroup.reLayoutByLp();
    }

    private void updateNewFirstCalendar(AwesomeViewGroup awesomeViewGroup){
        updateCalendarForAwesomeViewGroup(awesomeViewGroup, -5);
    }

    private void updateNewLastCalendar(AwesomeViewGroup awesomeViewGroup){
        updateCalendarForAwesomeViewGroup(awesomeViewGroup, 5);
    }

    private void updateCalendarForAwesomeViewGroup(AwesomeViewGroup awesomeViewGroup, int delta){
        Calendar c = awesomeViewGroup.getCalendar();
        if (c==null){
            return;
        }

        c.add(Calendar.DATE, delta);
        String dateString = formatCalendar(c);
        awesomeViewGroup.setTopText(dateString);
        awesomeViewGroup.setCalendar(c);
    }


    private void recordHorizontalScrollDir(float x){
         curScrollDir = x > 0 ? SCROLL_LEFT: SCROLL_RIGHT;
    }

    private void recordVerticalScrollDir(float y){
        curScrollDir = y > 0 ? SCROLL_DOWN : SCROLL_UP;
    }


    private void resetScrollDir(){
        curScrollDir = 0;
    }

    private void resetScrollWay(){
        curScrollWay = 0;
        hasDecideScrollWay = false;
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
                float newY = event.getY();
                float newX = event.getX();

                if (!hasDecideScrollWay) {
                    if (mTouchSlop < Math.abs(newY - preY)) {
                        // vertical scroll
                        hasDecideScrollWay = true;
                        curScrollWay = SCROLL_VERTICAL;
                    } else if (mTouchSlop < Math.abs(newX - preX)) {
                        // horizontal scroll
                        hasDecideScrollWay = true;
                        curScrollWay = SCROLL_HORIZONTAL;
                    }
                }

                if (hasDecideScrollWay){
                    if (curScrollWay == SCROLL_HORIZONTAL) {
                        float moveX = preX - newX;
                        recordHorizontalScrollDir(moveX);
                        moveChildX(moveX);
                        preX = newX;
                    }else if (curScrollWay == SCROLL_VERTICAL){
                        float moveY = preY - newY;
                        recordVerticalScrollDir(moveY);
                        moveChildY(moveY);
                        preY = newY;
                    }
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_DOWN:
                preX = event.getX();
                preY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onTouchEvent: " + "action up");
//                resetScrollDir();
                resetScrollWay();
                break;
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        int childWidth = width/NUM_LAYOUTS;
        int childHeight = height * 2;
        int childCount = getChildCount();
        Log.i(TAG, "onMeasure: " + "on Measure called");

        for (int i = 0 ; i < childCount ; i++){
            AwesomeViewGroup awesomeViewGroup = awesomeViewGroups.get(i);
            measureChild(awesomeViewGroup, widthMeasureSpec, heightMeasureSpec);
            AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();

            lp.parentHeight = height;
            lp.width = childWidth;
            lp.height = childHeight;

//            lp.top = 0;
            if (curScrollDir == SCROLL_LEFT){
                Log.i(TAG, "onMeasure: " + "left :" + awesomeViewGroup.getId());
                lp.left = getFirstVisibleLeftOffset() + i * childWidth;
            }else if (curScrollDir == SCROLL_RIGHT){
                Log.i(TAG, "onMeasure: " + "right : " + awesomeViewGroup.getId());
                lp.left = getFirstVisibleLeftOffset() + (i-1) * childWidth;
            }else if (curScrollDir == NON_SCROLL){
                Log.i(TAG, "onMeasure: " + "non scroll : " + awesomeViewGroup.getId());
                lp.left = getFirstVisibleLeftOffset() + (i-1) * childWidth;
            }

            lp.right = lp.left + childWidth;
            lp.bottom = lp.top + lp.height;
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // start to layout children
        int childCount = getChildCount();
        for (int i = 0 ; i < childCount; i ++){
            AwesomeViewGroup child = awesomeViewGroups.get(i);
            child.reLayoutByLp();
        }
    }

    public interface CalendarInterface{
        Calendar getFirstDay();
    }
}
