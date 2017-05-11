package com.developer.paul.recycledviewgroup;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Scroller;

import com.developer.paul.recycledviewgroup.AwesomeViewGroup.AwesomeLayoutParams;

import java.sql.Time;
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
    private int width,height, childWidth, childHeight;
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

    public int scrollThreshold;

    // for fling
    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;
    private float mVelocityX, mVelocityY;
    private Scroller mScroller;
    private float mAccelerator, mScrollTime;


    // for fling thread
    private boolean canFling = false;
    private int mSlots = 0;



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

        mMaxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mScroller = new Scroller(getContext());

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

    private void resetScrollWay(){
        curScrollWay = 0;
        hasDecideScrollWay = false;
    }

    private void noFlingEndCheck(){
        float needScrollDistance = childNeedsScrollToNearestPosition(awesomeViewGroups);
        if (needScrollDistance != 0.0){
            smoothMoveChildX(needScrollDistance);
        }
    }


    private void smoothMoveChildX(float x){
        curScrollDir = x < 0 ? SCROLL_RIGHT : SCROLL_LEFT; // animation scroll direction

        for (AwesomeViewGroup awesomeViewGroup: awesomeViewGroups){
            applyAnimation(x, awesomeViewGroup);
        }
        moveChildX(x);
    }

    private void smoothMoveChildY(float y){

    }

    private void applyAnimation(float x, AwesomeViewGroup awesomeViewGroup){
        AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
        Animation ani = new TranslateAnimation(
                x,  0, 0.0f, 0.0f);
        Log.i(TAG, "applyAnimation: " + lp.left + ", " + (lp.left -x) + " : " + x);
        ani.setDuration(500);
        ani.setInterpolator(new DecelerateInterpolator());
        ani.setFillAfter(false);
        awesomeViewGroup.startAnimation(ani);
    }

    private float childNeedsScrollToNearestPosition(List<AwesomeViewGroup> awesomeViewGroups){
        for (AwesomeViewGroup awesomeViewGroup : awesomeViewGroups){
            if (awesomeViewGroup.isVisibleInParent() && awesomeViewGroup.isOutOfParent()){
                AwesomeLayoutParams lp = (AwesomeLayoutParams) awesomeViewGroup.getLayoutParams();
                return Math.abs(lp.right) > Math.abs(lp.left) ? lp.left : lp.right;
            }
        }
        return 0;
    }

    /**
     * handler receive message from awesome thread, and continuously drawing new position
     */
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what ==0 ) {
                float moveDis = (float) msg.obj;
                moveChildX(moveDis);
            }
        }
    };

    /**
     * fling thread is for when doing fling, it continuously sending new moving
     * distance to handler.
     */
    private AwesomeThread flingThread = new AwesomeThread();


    private void checkFling(float velocityX, float velocityY, int scrollDir){
        int[] scrollPos = ScrollHelper.calculateScrollDistance(mScroller, (int)velocityX, (int)velocityY);
        switch (scrollDir){
            case SCROLL_LEFT:
            case SCROLL_RIGHT:
                if (shouldFling(velocityX)){

                    mScrollTime = ScrollHelper.calculateScrollTime(velocityX);
                    float distance = scrollPos[0];
                    float offset = scrollDir == SCROLL_LEFT ? getFirstVisibleLeftOffset() : -getFirstVisibleLeftOffset();
                    distance = ScrollHelper.findRightPosition(distance, offset,childWidth);

                    mAccelerator = ScrollHelper.calculateAccelerator(distance, mScrollTime);
                    mSlots = (int) (Math.abs(mScrollTime) * 16);

                    canFling = true;
                    if (flingThread.getState()!= Thread.State.NEW){
                        flingThread = new AwesomeThread();
                    }
                    flingThread.start();

                }else{
                    // not fling, only do post check
                    noFlingEndCheck();
                }
                break;
            case SCROLL_UP:
            case SCROLL_DOWN:
                if (shouldFling(velocityY)){
                    smoothMoveChildY(scrollPos[1]);
                }
                break;
        }
    }

    // 200 is a threshold , if more than 200, then fling, otherwise not
    private boolean shouldFling(float velocity){
        return Math.abs(velocity) > 200;
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
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    mVelocityX = mVelocityTracker.getXVelocity();
                    mVelocityY = mVelocityTracker.getYVelocity();
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker==null){
                    mVelocityTracker = VelocityTracker.obtain();
                }else{
                    mVelocityTracker.clear();
                }
                mVelocityX = 0;
                mVelocityY = 0;
                preX = event.getX();
                preY = event.getY();
                if (canFling){
                    // view group is flinging, then can only do horizontal scroll
                    hasDecideScrollWay = true;
                    curScrollWay = SCROLL_HORIZONTAL;
                }
                canFling = false; // canFling -> false, stop flinging
                break;
            case MotionEvent.ACTION_UP:
                resetScrollWay();
                checkFling(mVelocityX, mVelocityY, curScrollDir);
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

        childWidth = width/NUM_LAYOUTS;
        childHeight = height * 2;
        int childCount = getChildCount();
        scrollThreshold = childHeight/2;
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




    private class AwesomeThread extends Thread{

        @Override
        public void run() {
            int index = 0;
            while(canFling) {
                Message msg = new Message();
                msg.what = 0;
                float curTime = mScrollTime * (mSlots - index - 1) / mSlots;
                float nextTime = index+1 == mSlots? curTime : mScrollTime * (mSlots - index) / mSlots;
                float curDistance = ScrollHelper.getCurrentDistance(mAccelerator, curTime);
                float nextDistance = ScrollHelper.getCurrentDistance(mAccelerator, nextTime);

                float shouldMoveDis = curDistance - nextDistance;

                Log.i(TAG, "run: " + shouldMoveDis);

                msg.obj = shouldMoveDis;
                mHandler.sendMessage(msg);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                index ++;
                if (index >= mSlots){
                    canFling = false;
                }

            }
        }
    }
}
