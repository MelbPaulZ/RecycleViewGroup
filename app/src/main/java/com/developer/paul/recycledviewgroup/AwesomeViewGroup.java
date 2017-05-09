package com.developer.paul.recycledviewgroup;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Paul on 9/5/17.
 */

public class AwesomeViewGroup extends ViewGroup {
    private int orgLeft, orgRight;
    private String TAG = "AwesomeViewGroup";

    public AwesomeViewGroup(Context context) {
        super(context);
    }

    public AwesomeViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }



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

        if (lp.top <0){
            return true;
        }

        if (lp.bottom > parent.getHeight()){
            return true;
        }

        return false;
    }

    public void reLayoutByLp(){
        AwesomeLayoutParams lp = (AwesomeLayoutParams) getLayoutParams();
        layout(lp.left, lp.top, lp.right, lp.bottom);
    }


    public void setOrgLeft(int orgLeft) {
        this.orgLeft = orgLeft;
    }

    public void setOrgRight(int orgRight) {
        this.orgRight = orgRight;
    }

    public int getOrgLeft() {
        return orgLeft;
    }

    public int getOrgRight() {
        return orgRight;
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

        public int left;
        public int top;
        public int right;
        public int bottom;


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
