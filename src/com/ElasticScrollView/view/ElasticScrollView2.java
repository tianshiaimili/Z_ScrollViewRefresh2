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
	// ʵ�ʵ�padding�ľ����������ƫ�ƾ���ı���
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

	private RotateAnimation animation; //��ͷ�Ķ���
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
		 * ��ʾ���� �ɿ� ˢ�µ�����
		 */
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView2);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView2);
		
		/**
		 * ����headerview��С
		 */
		measureView(headView);
		/**
		 * ��ȡ����headerview�Ĵ�С�Ϳ�Ȼ������paddingֵ��Ȼ����һ��ʼ
		 * ����ʾ��screen
		 */
		headContentWidth = headView.getMeasuredWidth();
		headContentHeight = headView.getMeasuredHeight();
		headView.setPadding(0, -headContentHeight, 0, 0);
		headView.invalidate();
		///
		innerLayout.addView(headView);
		/**
		 * ��Linearlayout��ӵ���ǰ��ScrollView��
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
	 * ���������Ľӿ�
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
				LogUtils2.i("��downʱ���¼��ǰλ�á�==startY="+startY);
				startY = (int) ev.getY();
				isRecored = true;
				
			}*/
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			int tempY = (int) ev.getY();
			
			LogUtils2.w("ACTION_MOVE:  isRecored ="+isRecored+"   getScaleY()="+getScaleY());
			if(isRefreshable){
				
				LogUtils2.e("��ʱ��isRecored=="+isRecored);
				
				if(!isRecored && getScrollY() == 0 ){
					LogUtils2.i("ACTION_MOVE:  isRecored ="+isRecored+"   getScaleY()="+getScaleY());
					Log.i(TAG, "��moveʱ���¼��λ��");
					LogUtils2.i("��moveʱ���¼��λ��");
					isRecored = true;
					startY = tempY;
				}
				
			/*	if (!isRecored && getScrollY() == 0) {
					Log.i(TAG, "��moveʱ���¼��λ��");
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
						LogUtils2.i("��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
					}else if(tempY - startY <= 0){
						
						state = DONE;
						changeHeaderViewByState();
						LogUtils2.i("��DOne��������ˢ��״̬ת�䵽done״̬");
					}
				}
				//////////////////////////////////
				
				//**
				//���������õ���ָ������ȥˢ�µ�ʱ��
				 
				if (state != REFRESHING && isRecored && state != LOADING) {
					// ��������ȥˢ����
					LogUtils2.d("lalalallalala");
					if (state == RELEASE_To_REFRESH) {
						LogUtils2.i("*******************");
						canReturn = true;
						LogUtils2.d("tempY - startY=="+tempY  + startY+"  "+(tempY - startY));
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
							Log.i(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
							LogUtils2.i("���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
						}
						// һ�����Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();
							Log.i(TAG, "���ɿ�ˢ��״̬ת�䵽done״̬");
							LogUtils2.i("���ɿ�ˢ��״̬ת�䵽done״̬");
						} else {
							// ���ý����ر�Ĳ�����ֻ�ø���paddingTop��ֵ������
						}
					}

					// ����headView��size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// ����headView��paddingTop
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
					Log.i(TAG, "���ɿ�ˢ��״̬����done״̬");
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
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
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
			tipsTextview.setText("����ˢ��");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			LogUtils2.i("��ǰ״̬��done......");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// ����RELEASE_To_REFRESH״̬ת������
			/**
			 * isback �ж��Ƿ�ֱ���������� 
			 * �������ü�ͷͼƬ
			 */
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("����ˢ��");
			} else {
				tipsTextview.setText("����ˢ��");
			}
			Log.i(TAG, "��ǰ״̬������ˢ��");
			LogUtils2.d("��ǰ״̬������ˢ��...");
			break;
		case RELEASE_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			arrowImageView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(reverseAnimation);
			tipsTextview.setText("�ɿ�ˢ��");

			LogUtils2.i("��ǰ״̬���ɿ�ˢ��*******");
			break;
		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			tipsTextview.setText("����ˢ��...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			LogUtils2.d("��ǰ״̬,����ˢ��...");
			break;
		default:
			break;
		}
		
		
	}
	
	
}
