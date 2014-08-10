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
public class ElasticListView2 extends ScrollView {

	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	private final static int RATIO = 3;

	private LinearLayout innerLayout, headerViewLayout;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private OnRefreshListener onRefreshListener;
	// /
	private int headerViewHeight;
	private int headerViewWidth;
	// /
	private RotateAnimation animation; // 箭头的动画
	private RotateAnimation reverseAnimation;

	private boolean isRefreshable;
	private int state;
	private boolean isBack;

	private boolean canReturn;
	private boolean isRecored;
	private int startY;

	public ElasticListView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		initView(context);

	}

	public ElasticListView2(Context context) {
		super(context);
		initView(context);
		// TODO Auto-generated constructor stub
	}

	private void initView(Context context) {
		// TODO Auto-generated method stub
		innerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		innerLayout.setLayoutParams(params);
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		// /
		LayoutInflater inflater = LayoutInflater.from(context);
		headerViewLayout = (LinearLayout) inflater.inflate(
				R.layout.mylistview_head2, null);
		arrowImageView = (ImageView) headerViewLayout
				.findViewById(R.id.head_arrowImageView2);
		progressBar = (ProgressBar) headerViewLayout
				.findViewById(R.id.head_progressBar2);

		/**
		 * 提示下拉 松开 刷新的文字
		 */
		tipsTextview = (TextView) headerViewLayout
				.findViewById(R.id.head_tipsTextView2);
		lastUpdatedTextView = (TextView) headerViewLayout
				.findViewById(R.id.head_lastUpdatedTextView2);

		/**
		 * 计算headerview大小
		 */
		measureView(headerViewLayout);
		// /
		headerViewHeight = headerViewLayout.getMeasuredHeight();
		headerViewWidth = headerViewLayout.getMeasuredWidth();
		// /
		headerViewLayout.setPadding(0, -headerViewHeight, 0, 0);
		headerViewLayout.invalidate();
		// //
		innerLayout.addView(headerViewLayout);
		// //
		addView(innerLayout);

		// //
		// /动画
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(250);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		// //
		state = DONE;
		isRefreshable = false;
		canReturn = false;

	}

	private void measureView(LinearLayout childView) {
		// TODO Auto-generated method stub
		ViewGroup.LayoutParams params = childView.getLayoutParams();
		int childHeightSpec = 0;
		int childWidthSpec = 0;
		int tempHeight = 0;
		if (params == null) {
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT); 
		}

		childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, params.width);
		tempHeight = params.height;
		if (tempHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.UNSPECIFIED);
		}

		// //
		headerViewLayout.measure(childWidthSpec, childHeightSpec);

	}

	/**
	 * 处理滑动事件
	 */

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		int code = ev.getAction();
		switch (code) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			int tempY = (int) ev.getY();

			if (isRefreshable) {

				if (getScrollY() == 0 && !isRecored) {
					startY = tempY;
					isRecored = true;
				}

				/**
				 * 
				 */
				if (state == DONE) {
					if (tempY - startY > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
					}
				}

				/**
				 * 
				 */
				if (state == PULL_To_REFRESH) {
					
					canReturn = true;
					if ((tempY - startY) / RATIO > headerViewHeight) {
						state = RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();
					} else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}
				}

				/**
				 * 
				 */
				if (state != REFRESHING && isRecored) {

					if (state == RELEASE_To_REFRESH) {

						canReturn = true;

						if ((tempY - startY) / RATIO < headerViewHeight
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						} else if ((tempY - startY) <= 0) {
							state = DONE;
							changeHeaderViewByState();

						} else {

						}

					}

					// //
					// 动态更新headView的size
					if (state == PULL_To_REFRESH) {
						headerViewLayout.setPadding(0, -1 * headerViewHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headerViewLayout.setPadding(0, (tempY - startY) / RATIO
								- headerViewHeight, 0, 0);
					}

					if (canReturn) {

						canReturn = false;
						return true;

					}

				}

			}

			break;

		case MotionEvent.ACTION_UP:

			if (state != REFRESHING && isRecored) {
				if (state == RELEASE_To_REFRESH) {
					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();
				} else if (state == PULL_To_REFRESH) {
					state = DONE;
					changeHeaderViewByState();
				} else if (state == DONE) {

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

	/**
	 * 对状态的判断显示
	 */
	private void changeHeaderViewByState() {
		// TODO Auto-generated method stub

		switch (state) {
		case DONE:
			headerViewLayout.setPadding(0, -1 * headerViewHeight, 0, 0);
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
			 * isback 判断是否直接向上啦了 用来设置箭头图片
			 */
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("下拉刷新");
			} else {
				tipsTextview.setText("下拉刷新");
			}
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
			headerViewLayout.setPadding(0, 0, 0, 0);
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

	private void onRefresh() {
		// TODO Auto-generated method stub
		if (onRefreshListener != null)
			onRefreshListener.onRefresh();

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

	
	public interface OnRefreshListener{
		
		public void onRefresh();
		
	}


	public OnRefreshListener getOnRefreshListener() {
		return onRefreshListener;
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
		isRefreshable = true;
	}
	 
	
}
