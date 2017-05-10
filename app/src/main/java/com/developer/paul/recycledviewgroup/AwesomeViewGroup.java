package com.developer.paul.recycledviewgroup;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Paul on 9/5/17.
 */

public class AwesomeViewGroup extends ViewGroup {
    private String TAG = "AwesomeViewGroup";
    private TextView textView;
    private TextView bottomTextView;

    private int width, height;
    private Calendar calendar;

    public AwesomeViewGroup(Context context) {
        super(context);
        initView();
    }

    public AwesomeViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * for test only
     */
    private void initView(){
        textView = new TextView(getContext());
        textView.setText("this is a text");
        textView.setTextSize(20);
        addView(textView);

        bottomTextView = new TextView(getContext());
        bottomTextView.setText("this is bottom");
        bottomTextView.setTextSize(20);
        addView(bottomTextView);
    }

    public void setTopText(String text){
        textView.setText(text);
    }

    public void setCalendar(Calendar calendar){
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        textView.layout(0, 0, 300, 300);


        bottomTextView.layout(0, height - 300, 300, height);
    }


    /**
     * if left scroll or right scroll make the view out of its parent
     * @return
     */
    public boolean isOutOfParent(){
        View parent = (View) getParent();
        if (parent==null){
            return false;
        }

        AwesomeLayoutParams lp = (AwesomeLayoutParams) getLayoutParams();
        if (lp.left < 0){
            return true;
        }

        if (lp.right > parent.getWidth()){
            return true;
        }

        return false;
    }

    public boolean isVisibleInParent(){
        View parent = (View) getParent();
        if (parent == null){
            Log.i(TAG, "isVisibleInParent: " + getId() + ": parent null");
            return false;
        }

        AwesomeLayoutParams lp = (AwesomeLayoutParams) getLayoutParams();
        if (lp.right < 0){
            Log.i(TAG, "isVisibleInParent: " + getId() +": lp.right < 0 ," + lp.right );
            return false;
        }

        if (lp.left > parent.getWidth()){
            Log.i(TAG, "isVisibleInParent: " + getId() + ": lp. left>parent.getWidth");
            return false;
        }

        return true;
    }

    /**
     * this method will automatically re-layout based on its layout params
     */
    public void reLayoutByLp(){
        AwesomeLayoutParams lp = (AwesomeLayoutParams) getLayoutParams();
        layout((int)lp.left, (int)lp.top, (int)lp.right, (int)lp.bottom);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AwesomeLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new AwesomeLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new AwesomeLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof AwesomeLayoutParams;
    }

    public static class AwesomeLayoutParams extends LayoutParams{

        public float left;
        public float top;
        public float right;
        public float bottom;

        public float parentHeight;


        public AwesomeLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public AwesomeLayoutParams(int width, int height) {
            super(width, height);
        }

        public AwesomeLayoutParams(LayoutParams source) {
            super(source);
        }
    }
}
