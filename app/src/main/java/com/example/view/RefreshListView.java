package com.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dell.listviewrefreshdemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DELL on 2016/5/17.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private View headerView;
    private int headerHeight;
    private boolean flag;
    private int lastY = -1;
    private int topPadding ;
    private int scrollState;
    private int firstVisibleItem;
    private RefreshListener listener;

    private int state;
    private static final int NORMAL = 0;
    private static final int PULL = 1;
    private static final int RELEASE = 2;
    private static final int RELEASING = 3;

    public RefreshListView(Context context) {
        super(context);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        headerView = inflater.inflate(R.layout.list_header_layout, null);
        measureHeaderView(headerView);
        headerHeight = headerView.getMeasuredHeight();
        topPadding = -headerHeight;
        Log.i("tag", "headerHeight = " + headerHeight);
        setHeaderTopPadding(topPadding);
        addHeaderView(headerView);
        setOnScrollListener(this);
    }

    private void measureHeaderView(View view) {
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }

    private void setHeaderTopPadding(int topPadding) {
        headerView.setPadding(headerView.getPaddingLeft(), topPadding,
                headerView.getPaddingRight(), headerView.getPaddingBottom());
        headerView.invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.i("tag", "onScrollStateChanged scrollState:"+scrollState);
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.i("tag", "onScroll firstVisibleItem:"+firstVisibleItem);
        this.firstVisibleItem = firstVisibleItem;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (firstVisibleItem == 0) {
                    flag = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                if (state == RELEASE) {
                    state = RELEASING;
                    refreshViewByState();
                    if(listener != null){
                        listener.onRefresh();
                    }
                } else if (state == PULL) {
                    state = NORMAL;
                    flag = false;
                    refreshViewByState();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onMove(MotionEvent ev) {
        if (!flag) {
            return;
        }
        int currentY = (int) ev.getRawY();
        int deltaY;
        if(lastY == -1){
            deltaY = 0;
        }else{
            deltaY = currentY - lastY;
        }

        Log.i("tag", "onMove, currentY;"+currentY+",lastY:"+lastY+",deltaY:"+deltaY);
        lastY = currentY;
        if(deltaY > 0){
            //down
            topPadding = topPadding + deltaY;
            Log.i("tag", "deltaY > 0, topPadding:"+topPadding);
            if(topPadding > (int)getResources().getDimension(R.dimen.header_view_max_top_padding)){
                topPadding = (int)getResources().getDimension(R.dimen.header_view_max_top_padding);
            }
        }else
        if(deltaY < 0){
            //up
            topPadding = topPadding + deltaY;
            Log.i("tag", "deltaY < 0, topPadding:"+topPadding);
            if(topPadding < -headerHeight){
                topPadding = -headerHeight;
            }
        }

        switch (state) {
            case NORMAL:
                if (deltaY > 0) {
                    state = PULL;
                    refreshViewByState();
                }
                break;
            case PULL:
                setHeaderTopPadding(topPadding);
                Log.i("tag","onMove pull, deltaY:"+deltaY + ",scrollState"+scrollState);
                if (topPadding == (int)getResources().getDimension(R.dimen.header_view_max_top_padding)
                        && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    Log.i("tag","update state to release");
                    state = RELEASE;
                    refreshViewByState();
                }
                break;
            case RELEASE:
                setHeaderTopPadding(topPadding);
                Log.i("tag","onMove release, deltaY:"+deltaY );
                if (topPadding < (int)getResources().getDimension(R.dimen.header_view_max_top_padding) && topPadding > -headerHeight) {
                    state = PULL;
                    refreshViewByState();
                } else if (topPadding <= -headerHeight) {
                    state = NORMAL;
                    flag = false;
                    refreshViewByState();
                }
                break;
        }
    }

    private void refreshViewByState() {
        TextView tip = (TextView) headerView.findViewById(R.id.tip);
        ImageView arrow = (ImageView) headerView.findViewById(R.id.arrow);
        ProgressBar progress = (ProgressBar) headerView.findViewById(R.id.progress);
        RotateAnimation anim = new RotateAnimation(0, 180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(250);
        anim.setFillAfter(true);
        RotateAnimation anim1 = new RotateAnimation(180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim1.setDuration(250);
        anim1.setFillAfter(true);
        switch (state) {
            case NORMAL:
                arrow.clearAnimation();
                setHeaderTopPadding(-headerHeight);
                break;

            case PULL:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("下拉可以刷新！");
                arrow.clearAnimation();
                arrow.setAnimation(anim1);
                break;
            case RELEASE:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("松开可以刷新！");
                arrow.clearAnimation();
                arrow.setAnimation(anim);
                break;
            case RELEASING:
                setHeaderTopPadding(50);
                arrow.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                tip.setText("正在刷新...");
                arrow.clearAnimation();
                break;
        }
    }

    public void refreshComplete() {
        state = NORMAL;
        flag = false;
        refreshViewByState();
        TextView lastUpdateTime = (TextView) headerView
                .findViewById(R.id.lastupdate_time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = "上次更新于: " + format.format(date);
        lastUpdateTime.setText(time);
    }

    public void setListener(RefreshListener listener) {
        this.listener = listener;
    }
}
