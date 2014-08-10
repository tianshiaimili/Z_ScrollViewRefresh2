package com.ElasticScrollView.view;

import java.util.Date;

import com.ElasticScrollView.cjy.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ElasticScrollView2 extends ScrollView {
	private static final String TAG = "ElasticScrollView";
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;

	private int headContentWidth;
	private int headContentHeight;

	private LinearLayout innerLayout;
	private LinearLayout headView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private OnRefreshListener2 refreshListener;
	private boolean isRefreshable;
	private int state;
	private boolean isBack;

	private RotateAnimation animation; //箭头的动画
	private RotateAnimation reverseAnimation;

	private boolean canReturn;
	private boolean isRecored;
	private int startY;

	public ElasticScrollView2(Context context) {
		super(context);
		init(context);
	}

	public ElasticScrollView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	/**
	 * 
	 * @param context
	 */
	private void init(Context context) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		innerLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		
		innerLayout.setLayoutParams(params);
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		///
		headView = (LinearLayout) inflater.inflate(R.layout.mylistview_head2, null);
		
		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView2);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar2);
		
		/**
		 * 提示下拉 松开 刷新的文字
		 */
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView2);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView2);
		
		/**
		 * 计算headerview大小
		 */
		measureView(headView);
		/**
		 * 获取得了headerview的大小和宽，然后设置padding值，然他们一开始
		 * 不显示在screen
		 */
		headContentWidth = headView.getMeasuredWidth();
		headContentHeight = headView.getMeasuredHeight();
		headView.setPadding(0, -headContentHeight, 0, 0);
		headView.invalidate();
		///
		innerLayout.addView(headView);
		/**
		 * 把Linearlayout添加到当前的ScrollView中
		 */
		addView(innerLayout);
		
		///
		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF,
				0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(250);
		
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		
		state = DONE;
		isRefreshable = false;
		canReturn = false;
		
	}
	
	private void measureView(LinearLayout child) {
		// TODO Auto-generated method stub
		ViewGroup.LayoutParams params = child.getLayoutParams();
		if(params == null){
			
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT); 
			
		}
		//
		LogUtils2.d("p.width=="+params.width+"   p.height=="+params.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0+0, params.width);
		int lpHeight = params.height;
		int childHeightSpec;
		///
		if(lpHeight > 0){
			
			childHeightSpec = new MeasureSpec().makeMeasureSpec(lpHeight, 
					MeasureSpec.EXACTLY);
			
		}else {
			
			childHeightSpec = new MeasureSpec().makeMeasureSpec(lpHeight, 
					MeasureSpec.UNSPECIFIED);
			
		}
		
		///
		LogUtils2.i("childWidthSpec=="+childWidthSpec+"   childHeightSpec="+childHeightSpec);
		headView.measure(childWidthSpec, childHeightSpec);
		
		
	}

	public void setonRefreshListener(OnRefreshListener2 refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	/**
	 * 创建监听的接口
	 */
	public interface OnRefreshListener2{
		
		public void onRefresh();
		
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
	
		int code = ev.getAction();
		LogUtils2.w("++++++++++++++++++++++++++++++++++++");
		switch (code) {
		case MotionEvent.ACTION_DOWN:
			
			LogUtils2.w("ACTION_DOWN:  isRecored="+isRecored+"   getScaleY()="+getScaleY());
			/*if(getScaleY() == 0 && !isRecored){
				LogUtils2.i("在down时候记录当前位置‘==startY="+startY);
				startY = (int) ev.getY();
				isRecored = true;
				
			}*/
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			int tempY = (int) ev.getY();
			
			LogUtils2.w("ACTION_MOVE:  isRecored ="+isRecored+"   getScaleY()="+getScaleY());
			if(isRefreshable){
				
				LogUtils2.e("此时的isRecored=="+isRecored);
				
				if(!isRecored && getScrollY() == 0 ){
					LogUtils2.i("ACTION_MOVE:  isRecored ="+isRecored+"   getScaleY()="+getScaleY());
					Log.i(TAG, "在move时候记录下位置");
					LogUtils2.i("在move时候记录下位置");
					isRecored = true;
					startY = tempY;
				}
				
			/*	if (!isRecored && getScrollY() == 0) {
					Log.i(TAG, "在move时候记录下位置");
					isRecored = true;
					startY = tempY;
				}*/
				
				if(state == DONE){
					
					if((tempY - startY) > 0 ){
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
					}
					
				}
				
				if(state == PULL_To_REFRESH){
					canReturn = true;
					
					if((tempY - startY) /RATIO >= headContentHeight){
						state = RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();
						LogUtils2.i("由done或者下拉刷新状态转变到松开刷新");
					}else if(tempY - startY <= 0){
						
						state = DONE;
						changeHeaderViewByState();
						LogUtils2.i("由DOne或者下拉刷新状态转变到done状态");
					}
				}
				//////////////////////////////////
				
				//**
				//这里是设置当手指拖拉脱去刷新的时候
				 
				if (state != REFRESHING && isRecored && state != LOADING) {
					// 可以松手去刷新了
					LogUtils2.d("lalalallalala");
					if (state == RELEASE_To_REFRESH) {
						LogUtils2.i("*******************");
						canReturn = true;
						LogUtils2.d("tempY - startY=="+tempY  + startY+"  "+(tempY - startY));
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
							Log.i(TAG, "由松开刷新状态转变到下拉刷新状态");
							LogUtils2.i("由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();
							Log.i(TAG, "由松开刷新状态转变到done状态");
							LogUtils2.i("由松开刷新状态转变到done状态");
						} else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}

					// 更新headView的size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}
					
					
					if (canReturn) {
						canReturn = false;
						return true;
					}
				}
			}
			
			break;
		
		case MotionEvent.ACTION_UP:
			
			if(state !=REFRESHING && state != LOADING){
				
				if(state == DONE){
					
				}else if(state == PULL_To_REFRESH) {
					
					state = DONE;
					changeHeaderViewByState();
					
				}else if(state == RELEASE_To_REFRESH){
					
					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();
					Log.i(TAG, "由松开刷新状态，到done状态");
				}
			}
			isRecored = false;
			isBack = false;
			break;
		default:
			break;
		}
		
		return super.onTouchEvent(ev);
	}

	
		
	private void onRefresh() {
		// TODO Auto-generated method stub
		if(refreshListener != null)
		refreshListener.onRefresh();
		
	}

	public void addChild(View child) {
		innerLayout.addView(child);
	}

	public void addChild(View child, int position) {
		innerLayout.addView(child, position);
	}
	
	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
		invalidate();
		scrollTo(0, 0);
	}
	
	private void changeHeaderViewByState() {
		// TODO Auto-generated method stub
		
		switch (state) {
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);
			arrowImageView.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.goicon);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setText("下拉刷新");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			LogUtils2.i("当前状态，done......");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			/**
			 * isback 判断是否直接向上啦了 
			 * 用来设置箭头图片
			 */
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("下拉刷新");
			} else {
				tipsTextview.setText("下拉刷新");
			}
			Log.i(TAG, "当前状态，下拉刷新");
			LogUtils2.d("当前状态，下拉刷新...");
			break;
		case RELEASE_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			arrowImageView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(reverseAnimation);
			tipsTextview.setText("松开刷新");

			LogUtils2.i("当前状态，松开刷新*******");
			break;
		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			tipsTextview.setText("正在刷新...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			LogUtils2.d("当前状态,正在刷新...");
			break;
		default:
			break;
		}
		
		
	}
	
	
}
